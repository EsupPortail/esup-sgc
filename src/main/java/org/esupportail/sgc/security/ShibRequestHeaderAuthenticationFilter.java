package org.esupportail.sgc.security;

import java.io.IOException;

import javax.servlet.ServletException;
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
	
	private String credentialsRequestHeader4thisClass;
	
	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public void setLdapGroup2UserRoleService(LdapGroup2UserRoleService ldapGroup2UserRoleService) {
		this.ldapGroup2UserRoleService = ldapGroup2UserRoleService;
	}

	public void setResynchronisationUserService(ResynchronisationUserService resynchronisationUserService) {
		this.resynchronisationUserService = resynchronisationUserService;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
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
	
	/* 
	 * Surcharge de la méthode initiale : si pas d'attributs correspondant à credentialsRequestHeader (shib) ; on continue  :
	 * 	credentials ldap suffisent (et pas de credentials du tout aussi ...). 
	 * 
	 * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter#getPreAuthenticatedCredentials(javax.servlet.http.HttpServletRequest)
	 */
	@Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		String credentials = null;
        if (credentialsRequestHeader4thisClass != null) {
        	credentials = request.getHeader(credentialsRequestHeader4thisClass);
        }
        if(credentials == null) {
        	return "N/A"; 
        } else {
        	return credentials;
        }
    }
	
    public void setCredentialsRequestHeader(String credentialsRequestHeader) {
        super.setCredentialsRequestHeader(credentialsRequestHeader);
        this.credentialsRequestHeader4thisClass = credentialsRequestHeader;
    }

}
