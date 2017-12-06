package org.esupportail.sgc.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexController {

	@RequestMapping
	public String index(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	
		if(request.isUserInRole("ROLE_ADMIN")){
			return "redirect:/manager?index=first";
		} else if(auth.isAuthenticated()) {
			return "redirect:/user";
		} else {
			return "redirect:/login";
		}

	}
	
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		return "redirect:/";

	}
}
