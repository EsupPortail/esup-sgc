package org.esupportail.sgc.web;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.ExternalCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/")
@Controller
public class IndexController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ExternalCardService externalCardService;
	
	@Resource 
	CardService cardService;
	
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

