package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		return pathSegment;
	}
}
