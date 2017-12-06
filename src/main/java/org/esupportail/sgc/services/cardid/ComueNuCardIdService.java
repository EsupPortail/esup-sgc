package org.esupportail.sgc.services.cardid;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CardIdGenerator;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.CrousSmartCardIdGenerator;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.esc.EscUidFactoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO : idea would be to support multiple CardIdServices ...
 */
public class ComueNuCardIdService {
	
	private Long cardNfcIdCounterBegin;
	
	private Long cardCrousIdCounterBegin;
	
	private Boolean crousEncodeEnabled;
	
	@Autowired
	EscUidFactoryService escUidFactoryService;

	@Resource
	AppliConfigService appliConfigService;
	
	public Boolean isCrousEncodeEnabled() {
		return crousEncodeEnabled;
	}

	public void setCrousEncodeEnabled(Boolean crousEncodeEnabled) {
		this.crousEncodeEnabled = crousEncodeEnabled;
	}

	public void setCardNfcIdCounterBegin(Long cardNfcIdCounterBegin) {
		this.cardNfcIdCounterBegin = cardNfcIdCounterBegin;
	}

	public void setCardCrousIdCounterBegin(Long cardCrousIdCounterBegin) {
		this.cardCrousIdCounterBegin = cardCrousIdCounterBegin;
	}

	public String generateCardNfcId(Long cardId) {
		Card card = Card.findCard(cardId);
		if(card.getDesfireId() == null || card.getDesfireId().isEmpty()) {
			CardIdGenerator cardIdGenerator = card.getCardIdGenerator();
			if(cardIdGenerator == null) {
				cardIdGenerator = new CardIdGenerator();
				cardIdGenerator.persist();
				card.setCardIdGenerator(cardIdGenerator);
			}
			Long cardNfcId = getCardNfcIdCounterBegin(card) + cardIdGenerator.getId();
			card.setDesfireId(cardNfcId.toString());
			card.merge();
		}
		return card.getDesfireId();
	}

	private Long getCardNfcIdCounterBegin(Card card) {
		long userTypeCardNfcIdCounterBegin = cardNfcIdCounterBegin;
		User user = card.getUser();
		if("E".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("100000000000");
		} else if("P".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("200000000000");
		} else if("I".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("900000000000");
		} else {
			userTypeCardNfcIdCounterBegin += Long.valueOf("300000000000");
		}	
		return userTypeCardNfcIdCounterBegin;
	}

	public String encodeCardNfcId(String desfireId) {
		
		/*  on formatte l'ID façon COMUE Normandie Université
		 * cf http://wiki.univ-rouen.fr/dsi/systeme/services/controle_acces?s[]=leocarte
		 * commence par un octet représentant la version du mapping du fichier (valeur '30', correspondant au code ASCII de '0' pour commencer)
		 * 15 car encodés en ASCII ⇒ 15 octets
         * 15 car encodés en Hexa ⇒ 7 octets
         * 15 car encodés en BCD ⇒ 8 octets
		 */
	
		String cardNfcIdFormatted = "30";

		for (char ch : desfireId.toCharArray()) {
			cardNfcIdFormatted = cardNfcIdFormatted + Integer.toHexString(ch);
		}
		String desfireHex = Long.toHexString(Long.valueOf(desfireId));
		for(int i = 0 ; 14-desfireHex.length()>0 ; i++) {
			desfireHex = "0" + desfireHex;
		}
		cardNfcIdFormatted = cardNfcIdFormatted + desfireHex;
		for(int i = 0 ; 16-desfireId.length()>0 && i<16-desfireId.length() ; i++) {
			desfireId = "0" + desfireId;
		}
		cardNfcIdFormatted = cardNfcIdFormatted + desfireId;
		
		return cardNfcIdFormatted;
	}

	public String decodeCardNfcId(String desfireId) {
		return desfireId.substring(47, 62);
	}

	public Long generateCnousCardId(String csn) {
		CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(csn);
		if(smartCard == null) {
			smartCard = new CrousSmartCard();
			smartCard.setUid(csn);
			smartCard.persist();
			CrousSmartCardIdGenerator crousSmartCardIdGenerator = new CrousSmartCardIdGenerator();
			crousSmartCardIdGenerator.persist();
			smartCard.setCrousSmartCardIdGenerator(crousSmartCardIdGenerator);
			Long idZdc = cardCrousIdCounterBegin + crousSmartCardIdGenerator.getId();
			smartCard.setIdZdc(idZdc);
			smartCard.merge();
		}
		return smartCard.getIdZdc();
	}
	
	public void generateQrcode4Card(Card card) {
		escUidFactoryService.generateEscnUid(card);
		if(appliConfigService.isQrCodeEscEnabled()) {
			card.setQrcode(escUidFactoryService.getQrCodeUrl(card));
		} else {
			card.setQrcode(card.getEppn());
		}
	}

}
