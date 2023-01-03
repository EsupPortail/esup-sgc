package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@RequestMapping("/admin/printers")
@Controller
@RooWebScaffold(path = "admin/printers", formBackingObject = Printer.class, create=false, delete=true, update=true)
@RooWebFinder
public class PrinterController {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "printers";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}

}
