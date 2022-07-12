package org.esupportail.sgc.security;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;
	
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

		List<GrantedAuthority> authorities = ldapGroup2UserRoleService.getReachableRoles(targetUser.getEppn());

		return new org.springframework.security.core.userdetails.User(targetUser.getEppn(), "dummy", 
				true, // enabled
				true, // account not expired
				true, // credentials not expired
				true, // account not locked
				authorities);

	}

}
