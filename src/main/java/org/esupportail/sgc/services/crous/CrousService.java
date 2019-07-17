package org.esupportail.sgc.services.crous;

import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class CrousService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AuthApiCrousService authApiCrousService;
	
	@Resource
	CrousLogService crousLogService;
	
	@Override
	public void validateInternal(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getCrous() && authApiCrousService.isEnabled()) {
			if(this.postOrUpdateRightHolder(card.getEppn())) {
				CrousSmartCard smartCard = card.getCrousSmartCard();
				if(smartCard == null) {
					throw new SgcRuntimeException("Card with csn " + card.getCsn() + " has not the CROUS/IZLY application encoded ?", null);
				} else {
					try {
						if(authApiCrousService.validateSmartCard(card)) {
							card.setCrousError("");
						}
					} catch(HttpClientErrorException clientEx) {
						crousLogService.logErrorCrousAsync(null, card.getCsn(), clientEx.getResponseBodyAsString());
						log.warn("Exception calling api crous - crousService.validate " + card + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
						throw clientEx;
					}
				}
			}
		}
	}

	@Override
	public void invalidateInternal(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getCrous() && authApiCrousService.isEnabled()) {
			Boolean postOrUpdateRightHolderOk4invalication = false;
			try {
				postOrUpdateRightHolderOk4invalication = this.postOrUpdateRightHolder(card.getEppn());
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode()) || HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
					// si compte non updatable car non créé, locké, ... on considère que c'est ok pour l'invalidation
					crousLogService.logErrorCrous(card.getEppn(), null, clientEx.getResponseBodyAsString());
					log.warn("Exception calling api crous - crousService.invalidate " + card + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
					postOrUpdateRightHolderOk4invalication = true;
				} else {
					throw clientEx;
				}
			}
			if(postOrUpdateRightHolderOk4invalication) {
				CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(card.getCsn());
				if(smartCard == null) {
					throw new SgcRuntimeException("Card with csn " + card.getCsn() + " has not the CROUS/IZLY application encoded ?", null);
				} else {
					try {
						if(authApiCrousService.invalidateSmartCard(card)) {
							card.setCrousError("");
						}
					} catch(HttpClientErrorException clientEx) {
						crousLogService.logErrorCrousAsync(null, card.getCsn(), clientEx.getResponseBodyAsString());
						log.warn("Exception calling api crous - crousService.invalidate " + card + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
						throw clientEx;
					}
				}
			}
		}
	}
	
	public RightHolder getRightHolder(User user) {
		RightHolder rightHolder = null;
		if(user.getCrousIdentifier() != null && !user.getCrousIdentifier().isEmpty()) {
			rightHolder = this.getRightHolder(user.getCrousIdentifier());
		}
		if(rightHolder == null && !user.getEppn().equals(user.getCrousIdentifier())) {
			rightHolder = this.getRightHolder(user.getEppn());
		}
		return rightHolder;
	}

	public RightHolder getRightHolder(String identifier) {
		try {
			return authApiCrousService.getRightHolder(identifier);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				return null;
			} else if(HttpStatus.FORBIDDEN.equals(clientEx.getStatusCode())) { 
				throw new CrousAccountForbiddenException("Forbidden - crous righolder hold by another institute ?", clientEx);
			}
			// no crouslogError in db for a getRightHolder
			// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
			log.warn("Exception calling api crous - crousService.getRightHolder " + identifier + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
			throw clientEx;
		}
	}
		
	public CrousSmartCard getCrousSmartCard(String csn) {
		try {
			return authApiCrousService.getCrousSmartCard(csn);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				return null;
			}
			// no crouslogError in db for a getRightHolder
			// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
			log.warn("Exception calling api crous - crousService.getCrousSmartCard " + csn + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
			throw clientEx;
		}
	}
	
	public boolean postOrUpdateRightHolder(String eppn) {		
		User user = User.findUser(eppn);
		try {
			if(authApiCrousService.postOrUpdateRightHolder(eppn)) {
				user.setCrousError("");
				return true;
			} else {
				return false;
			}
		} catch(HttpClientErrorException clientEx) {
			crousLogService.logErrorCrousAsync(eppn, null, clientEx.getResponseBodyAsString());
			log.warn("Exception calling api crous - crousService.postOrUpdateRightHolder " + eppn + " : \n" + clientEx.getResponseBodyAsString(), clientEx);	
			throw clientEx;
		}
	}
	
	public void patchIdentifier(PatchIdentifier patchIdentifier) {
		try {
			authApiCrousService.patchIdentifier(patchIdentifier);
		} catch(HttpClientErrorException clientEx) {
			List<User> users = User.findUsersByCrousIdentifier(patchIdentifier.getCurrentIdentifier()).getResultList();
			if(!users.isEmpty()) {
				crousLogService.logErrorCrousAsync(users.get(0).getEppn(), null, clientEx.getResponseBodyAsString());
			}
			log.warn("Exception calling api crous status code : " + clientEx.getStatusCode() + " - crousService.patchIdentifier " + patchIdentifier + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
			throw clientEx;
		}	
	}
	
}


