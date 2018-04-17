package org.esupportail.sgc.services.crous;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;


@Service
public class AuthApiCrousService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ApiCrousService apiCrousService;
	
	@Resource
	CrousLogService crousLogService;
	
	public RightHolder getRightHolder(String eppnOrEmail) {		
		try {
			return apiCrousService.getRightHolder(eppnOrEmail);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.getRightHolder(eppnOrEmail);
				} catch(HttpClientErrorException clientEx2) {
					// no crouslogError in db for a getRightHolder
					// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.getRightHolder " + eppnOrEmail + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				// no crouslogError in db for a getRightHolder
				// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous - apiCrousService.getRightHolder " + eppnOrEmail + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
				throw clientEx;
			}
		}
	}
	
	public CrousSmartCard getCrousSmartCard(String csn) {		
		try {
			return apiCrousService.getCrousSmartCard(csn);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.getCrousSmartCard(csn);
				} catch(HttpClientErrorException clientEx2) {
					// no crouslogError in db for a getRightHolder
					// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.getCrousSmartCard " + csn + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				// no crouslogError in db for a getRightHolder
				// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous - apiCrousService.getCrousSmartCard " + csn + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
				throw clientEx;
			}
		}
	}
	
	
	public void postOrUpdateRightHolder(String eppn) {		
		try {
			apiCrousService.postOrUpdateRightHolder(eppn);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.postOrUpdateRightHolder(eppn);
				} catch(HttpClientErrorException clientEx2) {
					crousLogService.logErrorCrous(eppn, null, clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.postOrUpdateRightHolder " + eppn + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				crousLogService.logErrorCrous(eppn, null, clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous - apiCrousService.postOrUpdateRightHolder " + eppn + " : \n" + clientEx.getResponseBodyAsString(), clientEx);	
				throw clientEx;
			}
		}
	}

	public void validateSmartCard(Card card) {
		try {
			apiCrousService.validateSmartCard(card);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.validateSmartCard(card);
				} catch(HttpClientErrorException clientEx2) {
					crousLogService.logErrorCrous(null, card.getCsn(), clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.validateSmartCard " + card + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				crousLogService.logErrorCrous(null, card.getCsn(), clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous - apiCrousService.validateSmartCard " + card + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
				throw clientEx;
			}
		}
	}
	
	public void invalidateSmartCard(Card card) {
		try {
			apiCrousService.invalidateSmartCard(card);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.invalidateSmartCard(card);
				} catch(HttpClientErrorException clientEx2) {
					crousLogService.logErrorCrous(null, card.getCsn(), clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.invalidateSmartCard " + card + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				crousLogService.logErrorCrous(null, card.getCsn(), clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous status code : " + clientEx.getStatusCode() + " - apiCrousService.invalidateSmartCard " + card + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
				throw clientEx;
			}
		}
	}
	
	
	public void patchIdentifier(PatchIdentifier patchIdentifier) {
		try {
			apiCrousService.patchIdentifier(patchIdentifier);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.patchIdentifier(patchIdentifier);
				} catch(HttpClientErrorException clientEx2) {
					crousLogService.logErrorCrous(patchIdentifier.getNewIdentifier(), null, clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.patchIdentifier " + patchIdentifier + " : \n" + clientEx2.getResponseBodyAsString(), clientEx2);
					throw clientEx2;
				}
			} else {
				crousLogService.logErrorCrous(patchIdentifier.getNewIdentifier(), null, clientEx.getResponseBodyAsString());
				log.warn("Exception calling api crous status code : " + clientEx.getStatusCode() + " - apiCrousService.invalidateSmartCard " + patchIdentifier + " : \n" + clientEx.getResponseBodyAsString(), clientEx);
				throw clientEx;
			}
		}
	}


	public boolean isEnabled() {
		return apiCrousService.isEnabled();
	}

}

