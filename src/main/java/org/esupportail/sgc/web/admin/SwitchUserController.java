package org.esupportail.sgc.web.admin;

import javax.annotation.Resource;

import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/manager/su")
@Controller
public class SwitchUserController {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	String getCurrentMenu() {
		return "su";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SWITCH_USER')")
	@RequestMapping
	public String index(Model uiModel) {		
		return "manager/su";
	}

}
