package org.esupportail.sgc.web.admin;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.EscrCard;
import org.esupportail.sgc.domain.EscrStudent;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.crous.CrousPatchIdentifierService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
	
	@Resource
	CrousPatchIdentifierService crousPatchIdentifierService;
	
	@Resource
	ApiEscrService apiEscrService;
	
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
	
	@ModelAttribute("usersWithCrousDisabledInDbCount")
	public Long countFindUsersWithCrousDisabledInDb() {
		return User.countFindUsersByCrous(false);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index() {
		return "admin/tools";
	}

	
	@RequestMapping(value = "/replayAllActivationDesactivation", method = RequestMethod.POST, produces = "text/html")
	public String replayAllActivationDesactivation(RedirectAttributes redirectAttrs, @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro) {
		// Card.findAllCardIds() return ids with specific orders : enable at last -  replayValidationOrInvalidation is synchronized on eppn 
		// with thatn, we avoid parallel modifications on ldap (and avoid to add /remove ldap_value %secondary_id% that is same for multiples cards of one user !)
		for(BigInteger cardId : Card.findAllCardIds()) {
			cardEtatService.replayValidationOrInvalidation(cardId.longValue(), validateServicesNames, resynchro);
		}
		redirectAttrs.addFlashAttribute("messageSuccess", "success_replayAllActivationDesactivation");
		return "redirect:/admin/tools";
	}

	@Transactional
	@RequestMapping(value = "/patchEsupSgcEppn", method = RequestMethod.POST, produces = "text/html")
	public String patchEsupSgcEppn(RedirectAttributes redirectAttrs, @RequestParam String oldEppn, @RequestParam String newEppn) {
		if(!oldEppn.isEmpty() && !newEppn.isEmpty()) {
			User user = User.findUser(oldEppn);
			if(user.getEppn().equals(user.getCrousIdentifier())) {
				CrousPatchIdentifier crousPatchIdentifier = new CrousPatchIdentifier();
				crousPatchIdentifier.setOldId(user.getEppn());
				crousPatchIdentifier.setMail(user.getEmail());
				crousPatchIdentifier.setEppnNewId(newEppn);
				crousPatchIdentifier.persist();
				crousPatchIdentifierService.patchIdentifier(crousPatchIdentifier);
			}
			user.setEppn(newEppn);
			for(Card card : user.getCards()) {
				card.setEppn(newEppn);
			}
			for(PayboxTransactionLog payboxTransactionLog : PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(oldEppn).getResultList()) {
				payboxTransactionLog.setEppn(newEppn);
			}
			for(Log log : Log.findLogsByEppnEquals(oldEppn).getResultList()) {
				log.setEppn(newEppn);
			}
			for(Log log : Log.findLogsByEppnCibleEquals(oldEppn).getResultList()) {
				log.setEppnCible(newEppn);
			}
			for(EscrStudent  escrStudent: EscrStudent.findEscrStudentsByEppnEquals(oldEppn).getResultList()) {
				escrStudent.setEppn(newEppn);
			}
			redirectAttrs.addFlashAttribute("messageSuccess", "success_patchEsupSgcEppn");
		}
		return "redirect:/admin/tools";
	}
	
	@RequestMapping(value = "/checkCrousDisabledExistingInApiCrous", method = RequestMethod.POST, produces = "text/html")
	public String checkCrousDisabledExistingInApiCrous(Model uiModel) {
		List<String> usersCrousDisabledExistingInApiCrous = new ArrayList<String>();
		for(User user : User.findUsersByCrous(false).getResultList()) {
			try {
				if(crousService.getRightHolder(user) != null) {
					usersCrousDisabledExistingInApiCrous.add(user.getEppn());
				}
			} catch(CrousAccountForbiddenException e) {
				log.warn(String.format("Forbidden Exception on crous api getting RightHolder for %s", user.getEppn()), e);
			}
		}
		log.info("Users with crous disabled but existing in api crous : " + usersCrousDisabledExistingInApiCrous);
		uiModel.addAttribute("usersCrousDisabledExistingInApiCrous", usersCrousDisabledExistingInApiCrous);
		return "admin/tools";
	}
	
	
	@RequestMapping(value = "/forceSendEscrApiCrous", method = RequestMethod.POST, produces = "text/html")
	public String forceSendEscrApiCrous(Model uiModel) {
		log.info("forceSendEscrApiCrous called");
		int nbCardSendedInEscr = 0;
		for(User user : User.findUsersByEuropeanStudentCard(true).getResultList()) {
			try {
				if(apiEscrService.validateESCenableCard(user.getEppn())) {
					nbCardSendedInEscr++;
				}		
			} catch(Exception e) {
				log.warn(String.format("Exception on forceSendEscrApiCrous for %s", user.getEppn()), e);
			}	
		}
		String infoMsg = String.format("%s cartes envoyées dans l'API ESCR", nbCardSendedInEscr);
		uiModel.addAttribute("forceSendEscrApiCrousResult", infoMsg);
		log.info(infoMsg);
		return "redirect:/admin/tools";
	}
	
}
