package org.esupportail.sgc.web.admin;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/admin/tools")
@Controller
public class ToolsController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ImportExportService importExportService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CrousService crousService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "tools";
	}  
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	
	@ModelAttribute("validateServicesNames")
	public List<String> getValidateServicesNames() {
		return cardEtatService.getValidateServicesNames();
	}
	
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index() {
		return "admin/tools";
	}

	
	@RequestMapping(value = "/replayAllActivationDesactivation", method = RequestMethod.POST, produces = "text/html")
	public String replayAllActivationDesactivation(RedirectAttributes redirectAttrs, @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro) {
		for(BigInteger cardId : Card.findAllCardIds()) {
			cardEtatService.replayValidationOrInvalidation(cardId.longValue(), validateServicesNames, resynchro);
		}
		redirectAttrs.addFlashAttribute("messageSuccess", "success_replayAllActivationDesactivation");
		return "redirect:/admin/tools";
	}

	
	
	@RequestMapping(value = "/checkCrousDisabledExistingInApiCrous", method = RequestMethod.POST, produces = "text/html")
	public String checkCrousDisabledExistingInApiCrous(Model uiModel) {
		List<String> usersCrousDisabledExistingInApiCrous = new ArrayList<String>();
		for(User user : User.findUsersByCrous(false).getResultList()) {
			if(crousService.getRightHolder(user.getEppn()) != null) {
				usersCrousDisabledExistingInApiCrous.add(user.getEppn());
			}
		}
		log.info("Users with crous disabled but existing in api crous : " + usersCrousDisabledExistingInApiCrous);
		uiModel.addAttribute("usersCrousDisabledExistingInApiCrous", usersCrousDisabledExistingInApiCrous);
		return "admin/tools";
	}
	
}
