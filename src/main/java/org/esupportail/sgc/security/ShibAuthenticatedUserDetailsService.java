package org.esupportail.sgc.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
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

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	protected Map<String, String> mappingGroupesRoles;
	
	protected LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	@Resource
	RoleHierarchy sgcRoleHierarchy;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}

	public void setLdapGroup2UserRoleService(LdapGroup2UserRoleService ldapGroup2UserRoleService) {
		this.ldapGroup2UserRoleService = ldapGroup2UserRoleService;
	}

	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws AuthenticationException {
		Set<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>();
		String credentials = (String)token.getCredentials();
		for(String credential : StringUtils.split(credentials, ";")) {
			if(mappingGroupesRoles != null && mappingGroupesRoles.containsKey(credential)) { 
				for(String role : mappingGroupesRoles.get(credential).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
					authorities.add(new SimpleGrantedAuthority(role));
				}
			}
		}
		List <String> ldapGroups = ldapGroup2UserRoleService.getGroupsForEppn(token.getName());
		for(String roleFromLdap : ldapGroup2UserRoleService.getRoles(ldapGroups)) {
			authorities.add(new SimpleGrantedAuthority(roleFromLdap));
		}
		log.debug("Shib & Ldap Credentials for " + token.getName() + " -> " + authorities);
		
		List<GrantedAuthority> reachableAuthorities = new ArrayList<GrantedAuthority>(sgcRoleHierarchy.getReachableGrantedAuthorities(authorities));
		log.info("Shib & Ldap Reachable (adding sgcRoleHierarchy) Credentials for " + token.getName() + " -> " + reachableAuthorities);
		
		return createUserDetails(token, ldapGroups, reachableAuthorities);
	}

	protected UserDetails createUserDetails(Authentication token, List <String> ldapGroups,  Collection<? extends GrantedAuthority> authorities) {
		return new ShibUser(token.getName(), "N/A", true, true, true, true, authorities, ldapGroups);
	}
	
	public List<String> getManagerGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(group).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
				if("ROLE_SUPER_MANAGER".equals(role) || "ROLE_MANAGER".equals(role) || role.startsWith("ROLE_MANAGER_")) {
					groups.add(group);
				}
			}
		}		
		return groups;
	}
	
	public List<String> getConsultManagerGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(group).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
				if("ROLE_SUPER_MANAGER".equals(role) || "ROLE_MANAGER".equals(role) || "ROLE_CONSULT".equals(role) || role.startsWith("ROLE_MANAGER_") || role.startsWith("ROLE_CONSULT_")) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	public List<String> getVersoGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(group).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
				if("ROLE_SUPER_MANAGER".equals(role) || "ROLE_MANAGER".equals(role) || "ROLE_CONSULT".equals(role) || "ROLE_VERSO".equals(role) || role.startsWith("ROLE_MANAGER_") || role.startsWith("ROLE_CONSULT_")) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	public List<String> getUpdaterGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(group).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
				if("ROLE_UPDATER".equals(role)) {
					groups.add(group);
				}
			}
		}
		return groups;
	}	
	
	public List<String> getLivreurGroups() {
		List<String> groups = new ArrayList<String>();
		for(String group : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(group).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
				if("ROLE_LIVREUR".equals(role)) {
					groups.add(group);
				}
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
