package org.esupportail.sgc.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ShibRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private UserInfoService userInfoService;
	
	private LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	private ResynchronisationUserService resynchronisationUserService;
	
	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void setLdapGroup2UserRoleService(LdapGroup2UserRoleService ldapGroup2UserRoleService) {
		this.ldapGroup2UserRoleService = ldapGroup2UserRoleService;
	}

	public void setResynchronisationUserService(ResynchronisationUserService resynchronisationUserService) {
		this.resynchronisationUserService = resynchronisationUserService;
	}

	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        super.successfulAuthentication(request, response, authResult);
        String eppn = authResult.getName();
        User user = User.findUser(eppn);
	    if(user == null) {
	       	user = new User();
	       	user.setEppn(eppn);
	       	user.persist();
	    }
	    resynchronisationUserService.synchronizeUserInfo(eppn);
        userInfoService.updateUser(eppn, request);
        ldapGroup2UserRoleService.syncUser(eppn);
        
        log.info("User " + eppn + " authenticated");
    }

}
