package org.esupportail.sgc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class SupervisorService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public String findCurrentSupervisorUsername() {
		String supervisorUsername = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// Switch User Par un Supervisor ?
		for (GrantedAuthority a : auth.getAuthorities()) {
			if (a instanceof SwitchUserGrantedAuthority) {
				supervisorUsername = ((SwitchUserGrantedAuthority)a).getSource().getName();
			}
		}
		return supervisorUsername;
	}

}
