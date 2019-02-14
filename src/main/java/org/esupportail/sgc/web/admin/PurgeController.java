package org.esupportail.sgc.web.admin;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.PurgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/admin/purge")
@Controller
public class PurgeController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	PurgeService purgeService;
	
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
	
	@ModelAttribute("datePurge")
	public Date getDefaultDatePurge() {
		Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, -3);
		return cal.getTime();
	} 
	
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index(Model uiModel) {
		uiModel.addAttribute("userWithNoCardsNb", User.countFindUsersWithNoCards());
		return "admin/purge";
	}

	@RequestMapping(value="/count")
	@ResponseBody
	public Long card2purgeCount(@DateTimeFormat(pattern="yyyy-MM-dd") Date date, @RequestParam Etat etat) {
		return Card.countFindCardsByEtatAndDateEtatLessThan(etat, date);
	}
	
	// @Transactional - pas de transactionnal ici -> purge de chaque carte dans sa propre transaction
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public synchronized String purge(RedirectAttributes redirectAttrs, @DateTimeFormat(pattern="yyyy-MM-dd") Date date, @RequestParam Etat etat) {
		long nbCardsRemoved = 0;
		log.info(Card.countFindCardsByEtatAndDateEtatLessThan(etat, date) + " cartes vont être supprimées/purgées");
		for(Card card : Card.findCardsByEtatAndDateEtatLessThan(etat, date).getResultList()) {
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
		log.info(User.countFindUsersWithNoCards() + " utilisateurs vont être supprimés/purgés");
		for(User user : User.findUsersWithNoCards().getResultList()) {
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


