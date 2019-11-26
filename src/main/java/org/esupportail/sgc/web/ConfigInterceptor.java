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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.esupportail.sgc.domain.NavBarApp;
import org.esupportail.sgc.domain.NavBarApp.VisibleRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class ConfigInterceptor extends HandlerInterceptorAdapter {
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		// we want to add usual model in modelAndView only when needed, ie with
		// direct html view :
		// not for download response (for example) because we don't need it
		// not for redirect view because we don't need it and we don't want that
		// they appears in the url

		if (modelAndView != null && modelAndView.hasView()) {

			boolean isViewObject = modelAndView.getView() == null;

			boolean isRedirectView = !isViewObject && modelAndView.getView() instanceof RedirectView;

			boolean viewNameStartsWithRedirect = isViewObject && modelAndView.getViewName().startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX);

			if (!isRedirectView && !viewNameStartsWithRedirect) {		
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
				    	modelAndView.addObject("navBarApps", NavBarApp.findNavBarAppsByVisible4role(roles));
				    }
				}	
			}
		
			if(request.getParameter("size")!=null) {
				Integer size = Integer.valueOf(request.getParameter("size"));
				request.getSession().setAttribute("size_in_session", size);
			} else if(request.getSession(false)!=null && request.getSession().getAttribute("size_in_session") == null) {
				request.getSession().setAttribute("size_in_session", new Integer(10));
			}
		
		}
	}

}
