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
import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/admin/purge")
@Controller
public class PurgeController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;
	
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
	public String index() {
		return "admin/purge";
	}
	
	// @Transactional - pas de transactionnal pour purger chaque carte dans sa propre transaction
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public synchronized String purge(RedirectAttributes redirectAttrs, @DateTimeFormat(pattern="yyyy-MM-dd") Date date, @RequestParam Etat etat) {
		long nbCardsRemoved = 0;
		log.info(Card.countFindCardsByEtatAndDateEtatLessThan(etat, date) + " cartes vont être supprimées/purgées");
		for(Card card : Card.findCardsByEtatAndDateEtatLessThan(etat, date).getResultList()) {
			try {
				if(CrousErrorLog.countFindCrousErrorLogsByCard(card)>0) {
					for(CrousErrorLog crousErrorLog : CrousErrorLog.findCrousErrorLogsByCard(card).getResultList()) {
						crousErrorLog.remove();
					}
				}
				card.remove();
				nbCardsRemoved++;
			} catch(Exception e) {
				log.error("Erreur durant la suppression de carte : " + card.getId(), e);
			}
		}
		log.info(nbCardsRemoved + " cartes purgées (supprimées)");
		redirectAttrs.addFlashAttribute("messageSuccess", nbCardsRemoved + " cartes purgées (supprimées)");
		return "redirect:/admin/purge";
	}
	
	// @Transactional - pas de transactionnal pour purger chaque utilisateur dans sa propre transaction
	@RequestMapping(params = "users", method = RequestMethod.POST, produces = "text/html")
	public synchronized String purgeUsers(RedirectAttributes redirectAttrs) {
		long nbUsersRemoved = 0;
		log.info(User.countFindUsersWithNoCards() + " utilisateurs vont être supprimés/purgés");
		for(User user : User.findUsersWithNoCards().getResultList()) {
			try {
				if(CrousErrorLog.countFindCrousErrorLogsByUserAccount(user)>0) {
					for(CrousErrorLog crousErrorLog : CrousErrorLog.findCrousErrorLogsByUserAccount(user).getResultList()) {
						crousErrorLog.remove();
					}
				}
				user.remove();
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


