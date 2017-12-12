package org.esupportail.sgc.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class ShibAuthenticatedUserDetailsService
implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	protected Map<String, String> mappingGroupesRoles;
	
	protected LdapGroup2UserRoleService ldapGroup2UserRoleService;;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}

	public void setLdapGroup2UserRoleService(LdapGroup2UserRoleService ldapGroup2UserRoleService) {
		this.ldapGroup2UserRoleService = ldapGroup2UserRoleService;
	}

	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws AuthenticationException {
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		String credentials = (String)token.getCredentials();
		for(String credential : StringUtils.split(credentials, ";")) {
			if(mappingGroupesRoles != null && mappingGroupesRoles.containsKey(credential)){ 
				authorities.add(new SimpleGrantedAuthority(mappingGroupesRoles.get(credential)));
			}
		}
		for(String roleFromLdap : ldapGroup2UserRoleService.getRoles(token.getName())) {
			authorities.add(new SimpleGrantedAuthority(roleFromLdap));
		}
		return createUserDetails(token, authorities);
	}

	protected UserDetails createUserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
		return new User(token.getName(), "N/A", true, true, true, true, authorities);
	}
	
	public List<String> getManagerGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			if("ROLE_MANAGER".equals(mappingGroupesRoles.get(group))) {
				groups.add(group);
			}
		}		
		return groups;
	}
	
	public List<String> getConsultManagerGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			if("ROLE_MANAGER".equals(mappingGroupesRoles.get(group)) || "ROLE_CONSULT".equals(mappingGroupesRoles.get(group))) {
				groups.add(group);
			}
		}
		return groups;
	}
	
	public List<String> getUpdaterGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			if("ROLE_UPDATER".equals(mappingGroupesRoles.get(group))) {
				groups.add(group);
			}
		}
		return groups;
	}	
	
	public List<String> getLivreurGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			if("ROLE_LIVREUR".equals(mappingGroupesRoles.get(group))) {
				groups.add(group);
			}
		}
		return groups;
	}
	
	public boolean isPreviousAdmin(Authentication auth){
		boolean isPreviousAdmin = false;
		Object[] authArray = auth.getAuthorities().toArray();
		for(int i=0; i< authArray.length; i++){
			if(authArray[i].toString().contains("ROLE_PREVIOUS_ADMINISTRATOR")){
				isPreviousAdmin = true;
				break;
			}
		}
		return isPreviousAdmin;
	}

}
