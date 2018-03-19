package org.esupportail.sgc.services.cardid;

import java.text.MessageFormat;

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
	
	private int desfireFileLength = 64;
	
	private String identifierFormat = "{0}@{1}";
	
	public void setDesfireFileLength(String desfireFileLengthString) {
		this.desfireFileLength = Integer.valueOf(desfireFileLengthString);
	}

	public void setIdentifierFormat(String identifierFormat) {
		this.identifierFormat = identifierFormat;
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
			String desfireId = MessageFormat.format(identifierFormat, domain);
			card.getDesfireIds().put(appName, desfireId);
			card.merge();
			log.info("generate card Id for " + card.getEppn() + " : " + appName + " -> "  + desfireId);
		}
		return card.getDesfireIds().get(appName);
	}

	@Override
	public String encodeCardId(String desfireId) {
		String desfireIdWithPad = StringUtils.leftPad(desfireId, desfireFileLength, "0");
		return desfireIdWithPad;
	}

	@Override
	public String decodeCardId(String desfireIdWithPad) {
		Long desfireId = Long.valueOf(desfireIdWithPad);
		return desfireId.toString();
	}

}


