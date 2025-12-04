package org.esupportail.sgc.web.admin;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.PurgeService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/admin/purge")
@Controller
public class PurgeController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	PurgeService purgeService;

	@Resource
	UserInfoService userInfoService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "purge";
	}  
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@ModelAttribute("etatsPurgeable")
	public List<Etat> getEtatsAvailable4Purge() {
		return Arrays.asList(new Etat[] {Etat.DESTROYED, Etat.CADUC, Etat.CANCELED, Etat.REJECTED});
	}

	@ModelAttribute("userTypes")
	public List<String> getUserTypes() {
		return userInfoService.getListExistingType();
	}

	
	@ModelAttribute("datePurge")
	public LocalDateTime getDefaultDatePurge() {
		LocalDateTime date = LocalDateTime.now();
		return date.minusYears(3);
	} 
	
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index(Model uiModel) {
		uiModel.addAttribute("userWithNoCardsNb", userDaoService.countFindUsersWithNoCards());
        return "templates/admin/purge";
	}

	@RequestMapping(value="/count")
	@ResponseBody
	public Long card2purgeCount(@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date, @RequestParam Etat etat, @RequestParam(required = false) String userType) {
		LocalDateTime dateTime = date.atStartOfDay();
        return cardDaoService.countFindCardsByEtatAndUserTypeAndDateEtatLessThan(etat, userType, dateTime);
	}
	
	// @Transactional - pas de transactionnal ici -> purge de chaque carte dans sa propre transaction
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public synchronized String purge(RedirectAttributes redirectAttrs, @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date, @RequestParam Etat etat, @RequestParam(required = false) String userType) {
        LocalDateTime dateTime = date.atStartOfDay();
        long nbCardsRemoved = 0;
		log.info(cardDaoService.countFindCardsByEtatAndUserTypeAndDateEtatLessThan(etat, userType, dateTime) + " cartes vont être supprimées/purgées");
		for(Card card : cardDaoService.findCardsByEtatAndUserTypeAndDateEtatLessThan(etat, userType, dateTime).getResultList()) {
			try {
				purgeService.purge(card);
				nbCardsRemoved++;
			} catch(Exception e) {
				log.error("Erreur durant la suppression de carte : " + card.getId(), e);
			}
		}
		log.info(nbCardsRemoved + " cartes purgées (supprimées)");
		redirectAttrs.addFlashAttribute("messageSuccess", nbCardsRemoved + " cartes purgées (supprimées)");
		return "redirect:/admin/purge";
	}
	
	// @Transactional - pas de transactionnal ici -> purge de chaque utilisateur dans sa propre transaction
	@RequestMapping(params = "users", method = RequestMethod.POST, produces = "text/html")
	public synchronized String purgeUsers(RedirectAttributes redirectAttrs) {
		long nbUsersRemoved = 0;
		log.info(userDaoService.countFindUsersWithNoCards() + " utilisateurs vont être supprimés/purgés");
		for(User user : userDaoService.findUsersWithNoCards().getResultList()) {
			try {
				purgeService.purge(user);
				nbUsersRemoved++;
			} catch(Exception e) {
				log.error("Erreur durant la suppression de l'utilisateur : " + user.getId(), e);
			}
		}
		log.info(nbUsersRemoved + " utilisateurs purgés (supprimés)");
		redirectAttrs.addFlashAttribute("messageSuccess", nbUsersRemoved + " utilisateurs purgés (supprimés)");
		return "redirect:/admin/purge";
	}

	
}


