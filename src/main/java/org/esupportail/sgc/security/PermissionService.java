package org.esupportail.sgc.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

	@Resource 
	UserInfoService userInfoService;
	
	public List<String> getTypesTabs(Set<String> roles) {
		List<String> userTypes = userInfoService.getListExistingType();
		List<String> filteredTypes = new ArrayList<String>();
		if(roles.contains("ROLE_SUPER_MANAGER") || roles.contains("ROLE_CONSULT")) {
			filteredTypes.add("All");
		}
		for(String type: userTypes) {
			if(roles.contains("ROLE_SUPER_MANAGER") || roles.contains("ROLE_CONSULT") || roles.contains("ROLE_MANAGER_" + type) || roles.contains("ROLE_CONSULT_" + type) ) {
				filteredTypes.add(type);
			}
		}
		return filteredTypes;
	}

	public String getDefaultTypeTab(Set<String> roles) {
		String type = null;
		if(roles.contains("ROLE_SUPER_MANAGER")) {
			type = "All";
		} 
		if(type == null) {
			// ROLE_MANAGER_${userType} -> force userType
			for(String userType : userInfoService.getListExistingType()) {
				if(roles.contains("ROLE_MANAGER_" + userType)) {
					type = userType;
					break;
				}
			}
		}
		if(type == null) {
			if(roles.contains("ROLE_CONSULT")) {
				type = "";
			}
		} 
		if(type == null) {
			// ROLE_CONSULT_${userType} -> force userType
			for(String userType : userInfoService.getListExistingType()) {
				if(roles.contains("ROLE_CONSULT_" + userType)) {
					type = userType;
					break;
				}
			}
		}		
		return type;
	}

	public Boolean hasManagePermission(Set<String> roles, String userType) {
		if(roles.contains("ROLE_SUPER_MANAGER") || roles.contains("ROLE_MANAGER_" + userType)) {
			return true;
		}
		return false;
	}

	public Object hasConsultPermission(Set<String> roles, String userType) {
		if(roles.contains("ROLE_CONSULT") || roles.contains("ROLE_CONSULT_" + userType)) {
			return true;
		}
		return false;
	}
	
}

