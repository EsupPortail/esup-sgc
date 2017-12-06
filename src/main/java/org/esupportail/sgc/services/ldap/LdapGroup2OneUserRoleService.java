package org.esupportail.sgc.services.ldap;

import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LdapGroup2OneUserRoleService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public boolean removeRole(String eppn, String role) {
		User user = User.findUser(eppn);
		if(LdapGroup2UserRoleService.ROLE_USER_NO_EDITABLE.equals(role)) {
			user.setEditable(true);
		} else if(LdapGroup2UserRoleService.ROLE_USER_RENEWAL_PAYED.equals(role)) {
			user.setRequestFree(true);
		}
		user.getRoles().remove(role);
		return true;
	}
	
	public boolean addRole(String eppn, String role) {
		User user = User.findUser(eppn);
		if(LdapGroup2UserRoleService.ROLE_USER_NO_EDITABLE.equals(role)) {
			user.setEditable(false);
		} else if(LdapGroup2UserRoleService.ROLE_USER_RENEWAL_PAYED.equals(role)) {
			user.setRequestFree(false);
		}
		if(!user.getRoles().contains(role)) {
			user.getRoles().add(role);
		}
		return true;
	}
	
    
}
