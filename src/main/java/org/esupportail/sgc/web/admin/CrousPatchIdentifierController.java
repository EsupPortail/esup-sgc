package org.esupportail.sgc.web.admin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousPatchIdentifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/crouspatchids")
@Controller
@RooWebScaffold(path = "admin/crouspatchids", formBackingObject = CrousPatchIdentifier.class)
public class CrousPatchIdentifierController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CrousPatchIdentifierService crousPatchIdentifierService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crouspatchids";
	}   
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("isInWorking")
	public Boolean isInWorking() {
		return crousPatchIdentifierService.isInWorking();
	}
	
	@ModelAttribute("havePatchIdentifiersToProceed")
	public Boolean havePatchIdentifiersToProceed() {
		return CrousPatchIdentifier.countFindCrousPatchIdentifiersByPatchSuccessNotEquals(true) > 0;
	}
	
	
	@RequestMapping(value = "/addCsvFile", method = RequestMethod.POST, produces = "text/html")
	public String addCrousCsvFile(MultipartFile file, Model uiModel) throws IOException, ParseException {
		
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("CrousPatchIdentifierService retrieving file " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			crousPatchIdentifierService.consumeCsv(stream);
		}	
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids?page=1&size=10";
	}
	
	
	@RequestMapping(value = "/patchIdentifiers", method = RequestMethod.POST, produces = "text/html")
	public String patchIdentifiers(Model uiModel) {
		crousPatchIdentifierService.patchIdentifiers();
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids?page=1&size=10";
	}
	
}

