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
package org.esupportail.sgc.web;

import org.esupportail.sgc.dao.NavBarAppDaoService;
import org.esupportail.sgc.domain.NavBarApp;
import org.esupportail.sgc.domain.NavBarApp.VisibleRole;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ConfigInterceptor implements WebRequestInterceptor{

    @Resource
    NavBarAppDaoService navBarAppDaoService;


    @Override
    public void preHandle(WebRequest request) throws Exception {
    }

    @Override
    public void postHandle(WebRequest request, @Nullable ModelMap model) throws Exception {

		// we want to add usual model in model only when needed, ie with
		// direct html view :
		// not for download response (for example) because we don't need it
		// not for redirect view because we don't need it and we don't want that
		// they appears in the url

		if (model != null) {

		        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if(authentication != null) {
					List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
					Collection<VisibleRole> roles = new HashSet<NavBarApp.VisibleRole>();
				    if(authorities.contains(authorities.contains(new SimpleGrantedAuthority("ROLE_CONSULT")))) {
				    	roles.add(VisibleRole.CONSULT);
				    }
				    if(authorities.contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
				    	roles.add(VisibleRole.MANAGER);
				    }
				    if(authorities.contains(new SimpleGrantedAuthority("ROLE_UPDATER"))) {
				    	roles.add(VisibleRole.UPDATER);
				    }
				    if(authorities.contains(new SimpleGrantedAuthority("ROLE_VERSO"))) {
				    	roles.add(VisibleRole.VERSO);
				    }
				    if(authorities.contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
				    	roles.add(VisibleRole.LIVREUR);
				    }
				    if(!roles.isEmpty()) {
				    	model.addAttribute("navBarApps", navBarAppDaoService.findNavBarAppsByVisible4role(roles));
				    }
				}

                String active = (String) model.getAttribute("active");
                String role;
                if ("user".equals(active)) {
                    role = "user";
                } else if ("manager".equals(active) || "stats".equals(active)) {
                    role = "manager";
                } else if ("su".equals(active)) {
                    role = "supervisor";
                } else if (Arrays.asList(
                        "admin", "sessions", "paybox", "logs", "nfc", "crousError", "crouspatchids",
                        "configs", "crous", "import", "actionmsgs", "tools", "prefs", "template",
                        "navbar", "crousrules", "logmails", "purge", "printers", "userinfos",
                        "groupsroles", "locations", "javaperf"
                ).contains(active)) {
                    role = "admin";
                } else {
                    role = "user";
                }
                model.addAttribute("role", role);
			}
		
		}

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {

    }

}
