/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.sgc.security;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

public class SgcPermissionEvaluator implements PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		
		if(auth == null || auth.getName() == null || "".equals(auth.getName())) {
			return false;
		}
		
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		
		if(roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_MANAGER")) {
			return true;
		}
		
		String permissionKey = (String) permission;
		
		if("consult".equals(permissionKey) && roles.contains("ROLE_CONSULT")) {
			return true;
		}

        if(!(targetDomainObject instanceof List || targetDomainObject instanceof Long)) {
        	return false;
        }
        
        String eppn = auth.getName();
        User user = User.findUser(eppn);
        
        if(user != null) {
	        String userType = null;
	        if(targetDomainObject instanceof List) {
	        	List<Long> cardIds = (List<Long>)targetDomainObject;
	        	if(cardIds.isEmpty()) {
	        		return true;
	        	}
	        	List<String> userTypes = Card.findDistinctUserTypes(cardIds);
	        	userType = userTypes.size()==1 ? userTypes.get(0) : null;
	        } else {
	        	Card card = Card.findCard((Long)targetDomainObject);
	        	userType = card.getUserType()!=null ? card.getUserType() : null;
	        }
			if(userType!=null) {
				String managerRole = String.format("ROLE_MANAGER_%s", userType);
				String consultRole = String.format("ROLE_CONSULT_%s", userType);
				if("consult".equals(permissionKey)) {
					return roles.contains(managerRole) || roles.contains(consultRole);
				} else if("manage".equals(permissionKey)) {
					return roles.contains(managerRole);
				} 
			}
		
        }
        
        return false;
        
	}

	@Override
	public boolean hasPermission(Authentication arg0, Serializable arg1,
			String arg2, Object arg3) {
		// TODO 
		return false;
	}

}
