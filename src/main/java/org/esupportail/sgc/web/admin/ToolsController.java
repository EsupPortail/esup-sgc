package org.esupportail.sgc.web.admin;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.*;
import org.esupportail.sgc.domain.*;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.crous.CrousPatchIdentifierService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

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

	@Autowired
	List<ApiEscService> apiEscServices;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    CrousPatchIdentifierDaoService crousPatchIdentifierDaoService;

    @Resource
    LogDaoService logDaoService;

    @Resource
    PayboxTransactionLogDaoService payboxTransactionLogDaoService;

    @Resource
    UserDaoService userDaoService;

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
		return userDaoService.countFindUsersByCrous(false);
	}
	
	@ModelAttribute("usersWithCrousCount")
	public Long countFindUsersWithCrousCount() {
		return userDaoService.countFindUsersWithCrousAndWithCardEnabled();
	}
	
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index() {
        return "templates/admin/tools";
	}

	
	@RequestMapping(value = "/replayAllActivationDesactivation", method = RequestMethod.POST, produces = "text/html")
	public String replayAllActivationDesactivation(RedirectAttributes redirectAttrs, @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro) {
		// Card.findAllCardIds() return ids with specific orders : enable at last -  replayValidationOrInvalidation is synchronized on eppn 
		// with thatn, we avoid parallel modifications on ldap (and avoid to add /remove ldap_value %secondary_id% that is same for multiples cards of one user !)
		for(Long cardId : cardDaoService.findAllCardIds()) {
			cardEtatService.replayValidationOrInvalidation(cardId, validateServicesNames, resynchro);
		}
		redirectAttrs.addFlashAttribute("messageSuccess", "admin.msg.succes.replayAllActivationDesactivation");
		return "redirect:/admin/tools";
	}

	@Transactional
	@RequestMapping(value = "/patchEsupSgcEppn", method = RequestMethod.POST, produces = "text/html")
	public String patchEsupSgcEppn(RedirectAttributes redirectAttrs, @RequestParam String oldEppn, @RequestParam String newEppn) {
		if(!oldEppn.isEmpty() && !newEppn.isEmpty()) {
			User user = userDaoService.findUser(oldEppn);
			if(user.getEppn().equals(user.getCrousIdentifier())) {
				CrousPatchIdentifier crousPatchIdentifier = new CrousPatchIdentifier();
				crousPatchIdentifier.setOldId(user.getEppn());
				crousPatchIdentifier.setMail(user.getEmail());
				crousPatchIdentifier.setEppnNewId(newEppn);
				crousPatchIdentifierDaoService.persist(crousPatchIdentifier);
				crousPatchIdentifierService.patchIdentifier(crousPatchIdentifier);
			}
			user.setEppn(newEppn);
			for(Card card : user.getCards()) {
				card.setEppn(newEppn);
			}
			for(PayboxTransactionLog payboxTransactionLog : payboxTransactionLogDaoService.findPayboxTransactionLogsByEppnEquals(oldEppn).getResultList()) {
				payboxTransactionLog.setEppn(newEppn);
			}
			for(Log log : logDaoService.findLogsByEppnEquals(oldEppn).getResultList()) {
				log.setEppn(newEppn);
			}
			for(Log log : logDaoService.findLogsByEppnCibleEquals(oldEppn).getResultList()) {
				log.setEppnCible(newEppn);
			}
			redirectAttrs.addFlashAttribute("messageSuccess", "admin.msg.succes.patchEsupSgcEppn");
		}
		return "redirect:/admin/tools";
	}
	
	@RequestMapping(value = "/checkCrousDisabledExistingInApiCrous", method = RequestMethod.POST, produces = "text/html")
	public String checkCrousDisabledExistingInApiCrous(Model uiModel) {
		List<String> usersCrousDisabledExistingInApiCrous = new ArrayList<String>();
		for(User user : userDaoService.findUsersByCrous(false).getResultList()) {
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
        return "templates/admin/tools";
	}
	
	
	@RequestMapping(value = "/forceSendEscrApiCrous", method = RequestMethod.POST, produces = "text/html")
	public String forceSendEscrApiCrous(Model uiModel) {
		log.info("forceSendEscrApiCrous called");
		int nbCardSendedInEscr = 0;
		for(User user : userDaoService.findUsersByEuropeanStudentCard(true).getResultList()) {
			try {
				for (ApiEscService apiEscService : apiEscServices) {
					if (apiEscService.validateESCenableCard(user.getEppn())) {
						nbCardSendedInEscr++;
					}
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
	
	@RequestMapping(value = "/forcePostOrUpdateRightHolderCrous", method = RequestMethod.POST, produces = "text/html")
	public String forcePostOrUpdateRightHolderCrous(Model uiModel) {
		log.info("forcePostOrUpdateRightHolderCrous called");
		int nbRightHolderPutinCrous = 0;
		for(User user : userDaoService.findUsersWithCrousAndWithCardEnabled()) {
			try {
				if(crousService.postOrUpdateRightHolder(user.getEppn(), EsupSgcOperation.SYNC)) {
					nbRightHolderPutinCrous++;
				}		
			} catch(Exception e) {
				log.warn(String.format("Exception on forcePostOrUpdateRightHolderCrous for %s", user.getEppn()), e);
			}	
		}
		String infoMsg = String.format("%s rightHolder vérifiés et envoyés / mis à jour dans l'API CROUS", nbRightHolderPutinCrous);
		log.info(infoMsg);
		return "redirect:/admin/tools";
	}
	
	
}
