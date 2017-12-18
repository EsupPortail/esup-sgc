package org.esupportail.sgc.services.crous;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountLockException;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class CrousService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AuthApiCrousService authApiCrousService;
	
	@Resource
	CrousLogService crousLogService;
	
	@Override
	public void validate(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getCrous() && authApiCrousService.isEnabled()) {
			try {
				authApiCrousService.postOrUpdateRightHolder(card.getEppn());
			} catch(CrousAccountLockException ex) {
				log.warn(card.getEppn() + " is locked in crous : " + ex.getMessage());
				crousLogService.logErrorCrous(card.getEppn(), card.getCsn(), ex.getMessage());
				return ;
			}
			CrousSmartCard smartCard = card.getCrousSmartCard();
			if(smartCard == null) {
				throw new SgcRuntimeException("Card with csn " + card.getCsn() + " has not the CROUS/IZLY application encoded ?", null);
			} else {
				authApiCrousService.validateSmartCard(card);
			}
		}
	}

	@Override
	public void invalidate(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getCrous() && authApiCrousService.isEnabled()) {
			try {
				authApiCrousService.postOrUpdateRightHolder(card.getEppn());
			} catch(CrousAccountLockException ex) {
				log.warn(card.getEppn() + " is locked in crous : " + ex.getMessage());
				crousLogService.logErrorCrous(card.getEppn(), card.getCsn(), ex.getMessage());
				return ;
			}
			CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(card.getCsn());
			if(smartCard == null) {
				throw new SgcRuntimeException("Card with csn " + card.getCsn() + " has not the CROUS/IZLY application encoded ?", null);
			} else {
				authApiCrousService.invalidateSmartCard(card);
			}
		}
	}

	public RightHolder getRightHolder(String eppnOrEmail) {
		try {
			return authApiCrousService.getRightHolder(eppnOrEmail);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				return null;
			}
			throw clientEx;
		}
	}

	public void patchIdentifier(PatchIdentifier patchIdentifier) {
		authApiCrousService.patchIdentifier(patchIdentifier);
	}
	
}
