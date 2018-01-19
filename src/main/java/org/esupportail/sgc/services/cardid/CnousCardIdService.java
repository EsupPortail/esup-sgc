package org.esupportail.sgc.services.cardid;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CnousCardIdService extends GenericCardIdService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private Boolean crousEncodeEnabled = false;
	
	public void setCrousEncodeEnabled(Boolean crousEncodeEnabled) {
		this.crousEncodeEnabled = crousEncodeEnabled;
	}

	@Override
	public String generateCardId(Long cardId) {
		Card card = Card.findCard(cardId);
		String csn = card.getCsn();
		CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(csn);
		if(smartCard == null) {
			if(crousEncodeEnabled && getIdCounterBegin(card) != null) {
				Long idZdc = Long.valueOf(super.generateCardId(cardId));
				smartCard = new CrousSmartCard();
				smartCard.setUid(csn);
				smartCard.setIdZdc(idZdc);
				smartCard.persist();
			} else {
				log.warn("crousEncodeEnabled : " + crousEncodeEnabled);
				log.warn("getIdCounterBegin(card) : " + getIdCounterBegin(card));
				throw new SgcRuntimeException("generateCnousCardId called but crousEncodeEnabled = false or cardCrousIdCounterBegin is null", null);
			}
		}
		return smartCard.getIdZdc().toString();
	}

	public boolean isCrousEncodeEnabled() {
		return crousEncodeEnabled;
	}

}
