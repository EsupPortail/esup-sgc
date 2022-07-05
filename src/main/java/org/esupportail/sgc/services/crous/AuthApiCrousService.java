package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.List;


@Service
public class AuthApiCrousService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ApiCrousService apiCrousService;

	public RightHolder getRightHolder(String identifier, String eppn, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {		
		try {
			return apiCrousService.getRightHolder(identifier, eppn, esupSgcOperation);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.getRightHolder(identifier, eppn, esupSgcOperation);
				} catch(CrousHttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	
	public CrousSmartCard getCrousSmartCard(String csn) throws CrousHttpClientErrorException {		
		try {
			return apiCrousService.getCrousSmartCard(csn);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.getCrousSmartCard(csn);
				} catch(CrousHttpClientErrorException clientEx2) {
					// no crouslogError in db for a getRightHolder
					// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx2.getResponseBodyAsString());
					log.warn("Exception calling api crous after reauthentication - apiCrousService.getCrousSmartCard " + csn + " : \n" + clientEx2.getErrorBodyAsJson(), clientEx2);
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	
	
	public boolean postOrUpdateRightHolder(String eppn, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {		
		try {
			return apiCrousService.postOrUpdateRightHolder(eppn, esupSgcOperation);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.postOrUpdateRightHolder(eppn, esupSgcOperation);
				} catch(CrousHttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	

	public boolean validateSmartCard(Card card) throws CrousHttpClientErrorException {
		try {
			return apiCrousService.validateSmartCard(card);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.validateSmartCard(card);
				} catch(CrousHttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	
	public boolean invalidateSmartCard(Card card) throws CrousHttpClientErrorException {
		try {
			return apiCrousService.invalidateSmartCard(card);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.invalidateSmartCard(card);
				} catch(CrousHttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}
	
	
	public void patchIdentifier(PatchIdentifier patchIdentifier) throws CrousHttpClientErrorException {
		try {
			apiCrousService.patchIdentifier(patchIdentifier);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.patchIdentifier(patchIdentifier);
				} catch(CrousHttpClientErrorException clientEx2) {
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

	public List<CrousRule> getTarifRules(String numeroCrous, String rne) {
		try {
			return apiCrousService.getTarifRules(numeroCrous, rne);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					return apiCrousService.getTarifRules(numeroCrous, rne);
				} catch(HttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}

	public void unclose(String eppn, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {
		try {
			apiCrousService.unclose(eppn, esupSgcOperation);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.UNAUTHORIZED.equals(clientEx.getStatusCode())) {
				log.info("Auth Token of Crous API should be renew, we call an authentication");
				apiCrousService.authenticate();
				try {
					apiCrousService.unclose(eppn, esupSgcOperation);
				} catch(CrousHttpClientErrorException clientEx2) {
					throw clientEx2;
				}
			} else {
				throw clientEx;
			}
		}
	}

}

