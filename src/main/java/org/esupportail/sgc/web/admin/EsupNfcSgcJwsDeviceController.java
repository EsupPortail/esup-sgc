package org.esupportail.sgc.web.admin;
import javax.annotation.Resource;

import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/jwsdevices")
@Controller
@RooWebScaffold(path = "admin/jwsdevices", formBackingObject = EsupNfcSgcJwsDevice.class, create=false)
public class EsupNfcSgcJwsDeviceController {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "nfc";
	}
}
