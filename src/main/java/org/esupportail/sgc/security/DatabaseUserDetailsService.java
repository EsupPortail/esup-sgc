package org.esupportail.sgc.security;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;

	@Resource
	RoleHierarchy sgcRoleHierarchy;
	
	@Override
	public UserDetails loadUserByUsername(String eppn)
			throws UsernameNotFoundException {

		eppn = eppn.trim().toLowerCase();

		User user = User.findUser(eppn);
	    if(user == null) {
	       	user = new User();
	       	user.setEppn(eppn);
	       	user.persist();
	    }
		return loadUserByUser(user);				
	}

	public UserDetails loadUserByUser(User targetUser)
			throws UsernameNotFoundException {

		try {
			resynchronisationUserService.synchronizeUserInfo(targetUser.getEppn());
		} catch(Exception e) {
			log.error("Error when synchronizeUserInfo " + targetUser.getEppn(), e);
		}
		ldapGroup2UserRoleService.syncUser(targetUser.getEppn());

		List<GrantedAuthority> reachableAuthorities = new ArrayList<GrantedAuthority>();

		List <String> ldapGroups = ldapGroup2UserRoleService.getGroupsForEppn(targetUser.getEppn());

		for(String roleFromLdap : ldapGroup2UserRoleService.getRoles(ldapGroups)) {
			reachableAuthorities.add(new SimpleGrantedAuthority(roleFromLdap));
		}
		log.debug("Ldap Credentials for " + targetUser.getEppn() + " -> " + reachableAuthorities);

		reachableAuthorities.addAll(sgcRoleHierarchy.getReachableGrantedAuthorities(reachableAuthorities));
		log.info("Ldap Reachable (adding sgcRoleHierarchy) Credentials for " + targetUser.getEppn() + " -> " + reachableAuthorities);

		return new ShibUser(targetUser.getEppn(), "dummy",
				true, // enabled
				true, // account not expired
				true, // credentials not expired
				true, // account not locked
				reachableAuthorities,
				ldapGroups);

	}

}
