package org.esupportail.sgc.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
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

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		resynchronisationUserService.synchronizeUserInfo(targetUser.getEppn());
		ldapGroup2UserRoleService.syncUser(targetUser.getEppn());
		
		for(String role : targetUser.getReachableRoles()) {
			authorities.add(new SimpleGrantedAuthority(role));
		}		

		return new org.springframework.security.core.userdetails.User(targetUser.getEppn(), "dummy", 
				true, // enabled
				true, // account not expired
				true, // credentials not expired
				true, // account not locked
				authorities);

	}

}
