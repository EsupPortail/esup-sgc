package org.esupportail.sgc.web.manager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.services.CardEtatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Transactional
@RequestMapping("/manager/nfc")
@Controller
public class EsupNfcEncodeController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	Map<String, Long> managerTarget = new HashMap<String, Long>();
	
	@Resource
	CardEtatService cardEtatService;
	
	@RequestMapping(value = "/{cardId}", method=RequestMethod.POST)
	public String select(@PathVariable("cardId") Long cardId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loginInit = authentication.getName();
		Card card = Card.findCard(cardId);
		cardEtatService.setCardEtat(card, Etat.IN_ENCODE, null, null, true, false);
		managerTarget.put(loginInit, cardId);
		return  "redirect:/manager/" + cardId;
	}
	
	@RequestMapping(value="/current", method=RequestMethod.GET)
	@ResponseBody 
	public String getLoginTarget() {
		String eppn = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = authentication.getName();
		Long cardId =  managerTarget.get(eppnInit);
		if(cardId!=null) {
			Card card = Card.findCard(cardId);
			eppn = card.getEppn();
		}
		return eppn;
	}
	
	public Long getCardIdTarget(String loginInit) {
		Long cardId =  managerTarget.get(loginInit);
		return cardId;
	}
	
	@RequestMapping(value="/clear", method=RequestMethod.GET) 
	public String clear() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Long cardId = managerTarget.remove(login);
		Card card = Card.findCard(cardId);
		cardEtatService.setCardEtat(card, Etat.PRINTED, null, null, true, false);
		return "redirect:/manager/" + cardId;
	}
	
	public void clear(String eppnInit) {
		log.info("No more card selected for encoding for " + eppnInit);
		managerTarget.remove(eppnInit);
	}

	public boolean selectQrcode4CardEncoding(String eppnInit, String qrcode) {
		List<Card> cards = Card.findCardsByQrcodeAndEtatIn(qrcode, Arrays.asList(new Etat[] {Etat.IN_PRINT, Etat.PRINTED, Etat.IN_ENCODE})).getResultList();
		if(!cards.isEmpty()) {
			Card card2encode = cards.get(0);
			cardEtatService.setCardEtat(card2encode, Etat.IN_ENCODE, null, null, true, false);
			managerTarget.put(eppnInit, card2encode.getId());
			log.info(eppnInit + " selected " + qrcode + " card for encoding ..." );
			return true;
		} else {
			log.warn(eppnInit + " want to select " + qrcode + " card for encoding but no card found" );
			return false;
		}
	}

	public boolean selectEppn4CardUpdating(String eppnInit, String csn) {
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		if(card.isEnabled()) {
			return true;
		} else {
			return false;
		}
	}

}

