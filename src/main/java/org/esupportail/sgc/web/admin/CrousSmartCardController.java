package org.esupportail.sgc.web.admin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.esupportail.sgc.dao.CrousSmartCardDaoService;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousSmartCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/crouscards")
@Controller
public class CrousSmartCardController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CrousSmartCardService crousSmartCardService;

    @Resource
    CrousSmartCardDaoService crousSmartCardDaoService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crous";
	}   
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("isInWorking")
	public Boolean isInWorking() {
		return crousSmartCardService.isInWorking();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
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

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("croussmartcard", crousSmartCardDaoService.findCrousSmartCard(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/crouscards/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "zdcCreationDate") Pageable pageable,
                       Model uiModel,
                       HttpServletRequest request
                       ) {
        Page<CrousSmartCard> crousSmartCards = crousSmartCardDaoService.findCrousSmartCardEntries(pageable);
        uiModel.addAttribute("croussmartcards", crousSmartCards);
        return "templates/admin/crouscards/list";
    }
}
