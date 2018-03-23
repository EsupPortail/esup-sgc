package org.esupportail.sgc.services.cardid;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsnDomainCardIdService implements CardIdService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	EntityManager entityManager;
	
	private String appName;
	
	private int desfireFileLength = 128;
	
	private String identifierFormat = "{0}@{1}";
	
	private String padChar = " ";
	
	public void setDesfireFileLength(String desfireFileLengthString) {
		this.desfireFileLength = Integer.valueOf(desfireFileLengthString);
	}

	public void setIdentifierFormat(String identifierFormat) {
		this.identifierFormat = identifierFormat;
	}

	public void setPadChar(String padChar) {
		this.padChar = padChar;
	}

	@Override
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	@Override
	public String generateCardId(Long cardId) {
		Card card = Card.findCard(cardId);
		if(card.getDesfireIds().get(appName) == null || card.getDesfireIds().get(appName).isEmpty()) {
			String domain = card.getEppn().replaceAll(".*@", "");
			String desfireId = MessageFormat.format(identifierFormat, card.getCsn(), domain);
			card.getDesfireIds().put(appName, desfireId);
			card.merge();
			log.info("generate card Id for " + card.getEppn() + " : " + appName + " -> "  + desfireId);
		}
		return card.getDesfireIds().get(appName);
	}

	@Override
	public String encodeCardId(String desfireId) {
		String desfireIdWithPad = StringUtils.leftPad(desfireId, desfireFileLength/2, padChar);
		log.info("desfireIdWithPad : " + desfireIdWithPad + " -> size : " + desfireIdWithPad.length());
		String desfireIdWithPadFormatted = "";
		for (char ch : desfireIdWithPad.toCharArray()) {
			desfireIdWithPadFormatted = desfireIdWithPadFormatted + Integer.toHexString(ch);
		}
		log.info("desfireIdWithPadFormatted : " + desfireIdWithPadFormatted);
		if(desfireIdWithPadFormatted.length() > desfireFileLength) {
			log.error(desfireIdWithPadFormatted + " is too long : " + desfireIdWithPadFormatted.length() + " > " +  desfireFileLength);
		}
		return desfireIdWithPadFormatted;
	}


	@Override
	public String decodeCardId(String desfireIdWithPad) {
		boolean onPadding = true;
		int index = 0;
		String desfireId = "";
		while (index < desfireIdWithPad.length()) {
		    String hexString = desfireIdWithPad.substring(index, Math.min(index + 2, desfireIdWithPad.length()));
		    int charLong = Integer.parseInt(hexString, 16);
		    char ch = (char) charLong;
		    if(!onPadding || padChar.charAt(0) != ch) {
		    	desfireId = desfireId + (char)charLong;
		    	onPadding = false;
		    }
		    
		    index += 2;
		}
		log.info("desfireId decoded : " + desfireId);
		return desfireId.toString();
	}

}



