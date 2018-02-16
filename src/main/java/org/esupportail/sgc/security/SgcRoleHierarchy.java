package org.esupportail.sgc.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service("sgcRoleHierarchy")
public class SgcRoleHierarchy implements RoleHierarchy {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public Collection<GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities) {
		
		if (authorities == null || authorities.isEmpty()) {
            return AuthorityUtils.NO_AUTHORITIES;
        }

		List<GrantedAuthority> reachableRoles = new ArrayList<GrantedAuthority>();

		 if (authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER_MANAGER"))) {
			 reachableRoles.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
		 } else {
	        for (GrantedAuthority authority : authorities) {
	            if (authority.getAuthority().startsWith("ROLE_MANAGER_")) {
	                reachableRoles.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
	                break;
	            }
	        }
		 }
        
       
        
        reachableRoles.addAll(authorities);

        if (log.isDebugEnabled()) {
        	log.debug("getReachableGrantedAuthorities() - From the roles " + authorities
                    + " one can reach " + reachableRoles);
        }

        return reachableRoles;
	}

}

