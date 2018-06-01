package org.esupportail.sgc.web.admin;
import javax.annotation.Resource;

import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/prefses")
@Controller
@RooWebScaffold(path = "admin/prefses", formBackingObject = Prefs.class)
public class PrefsController {
	
	@Resource
	AppliConfigService appliConfigService;	
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "prefs";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
}
