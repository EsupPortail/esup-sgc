package org.esupportail.sgc.services.cardid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.esc.EscUidFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CardIdsService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	EscUidFactoryService escUidFactoryService;

	@Resource
	AppliConfigService appliConfigService;
	
	Map<String, CardIdService> cardIdServices = new HashMap<String, CardIdService>();

	public void setCardIdServices(List<CardIdService> cardIdServicesList) {
		for(CardIdService cardIdService : cardIdServicesList) {
			this.cardIdServices.put(cardIdService.getAppName(), cardIdService);
		}
	}

	public String generateCardId(Long cardId, String appName) {
		return cardIdServices.get(appName).generateCardId(cardId);
	}

	public String encodeCardId(String desfireId, String appName) {
		return cardIdServices.get(appName).encodeCardId(desfireId);
	}
	

	public String decodeCardNfcId(String desfireId, String appName) {
		return cardIdServices.get(appName).decodeCardId(desfireId);
	}
	
	public void generateQrcode4Card(Card card) {		
		if(appliConfigService.isQrCodeEscEnabled()) {
			escUidFactoryService.generateEscnUid(card);
			card.setQrcode(escUidFactoryService.getQrCodeUrl(card));
		} else {
			card.setQrcode(card.getEppn());
		}
	}

	public Boolean isCrousEncodeEnabled() {
		return cardIdServices.get("crous") != null && ((CnousCardIdService)cardIdServices.get("crous")).isCrousEncodeEnabled();
	}

	public Card findCardsByDesfireId(String desfireIdEncoded, String appName) {
		String desfireId = decodeCardNfcId(desfireIdEncoded, appName);
		
		// TODO : findCardsByDesfireIdAndAppNameEquals by CardIdService implementation !
		// Here it's for @GenericCardIdService
		List<Card> cards = Card.findCardsByDesfireIdAndAppNameEquals(desfireId, appName).getResultList();
		if(cards.size()==0) {
			return null;
		} else {
			return cards.get(0);
		}
		
	}

}

