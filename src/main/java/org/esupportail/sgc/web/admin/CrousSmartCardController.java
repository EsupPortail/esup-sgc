package org.esupportail.sgc.web.admin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousSmartCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/crouscards")
@Controller
@RooWebScaffold(path = "admin/crouscards", formBackingObject = CrousSmartCard.class, create=false, delete=false, update=false)
public class CrousSmartCardController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crous";
	}   
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@Resource
	CrousSmartCardService crousSmartCardService;
	
	@RequestMapping(value = "/addCrousCsvFile", method = RequestMethod.POST, produces = "text/html")
	public String addCrousCsvFile(MultipartFile file, @RequestParam(defaultValue="False") Boolean inverseCsn) throws IOException, ParseException {
		
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("CrousSmartCardController retrieving file " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			crousSmartCardService.consumeCsv(stream, inverseCsn);
		}

		return "redirect:/admin/crouscards";
	}

	
}
