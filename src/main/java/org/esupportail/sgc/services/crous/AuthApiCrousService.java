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
					throw clientEx2;
				}
			} else {
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
				throw clientEx;
			}
		}
	}
	
	
	public boolean postOrUpdateRightHolder(String eppn) {		
		try {
			return apiCrousService.postOrUpdateRightHolder(eppn);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.postOrUpdateRightHolder(eppn);
				} catch(HttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}

	public boolean validateSmartCard(Card card) {
		try {
			return apiCrousService.validateSmartCard(card);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.validateSmartCard(card);
				} catch(HttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	
	public boolean invalidateSmartCard(Card card) {
		try {
			return apiCrousService.invalidateSmartCard(card);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.invalidateSmartCard(card);
				} catch(HttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
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
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}


	public boolean isEnabled() {
		return apiCrousService.isEnabled();
	}

}

