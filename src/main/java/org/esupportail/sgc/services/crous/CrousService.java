package org.esupportail.sgc.services.crous;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

public class CrousService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AuthApiCrousService authApiCrousService;
	
	@Resource
	CrousLogService crousLogService;
	
	@Resource 
	UserInfoService userInfoService;
	
	@Override
	public void validateInternal(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getCrous() && authApiCrousService.isEnabled()) {
			if(this.postOrUpdateRightHolder(card.getEppn(), EsupSgcOperation.ACTIVATE)) {
				CrousSmartCard smartCard = card.getCrousSmartCard();
				if(smartCard == null) {
					throw new SgcRuntimeException("Card with csn " + card.getCsn() + " has not the CROUS/IZLY application encoded ?", null);
				} else {
					try {
						if(authApiCrousService.validateSmartCard(card)) {
							card.setCrousError("");
						}
					} catch(CrousHttpClientErrorException clientEx) {
						crousLogService.logErrorCrousAsync(clientEx);
						log.warn("Exception calling api crous - crousService.validate " + clientEx, clientEx);
						throw new SgcRuntimeException("Exception calling api crous - crousService.validate " + clientEx, clientEx);
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
				postOrUpdateRightHolderOk4invalication = authApiCrousService.postOrUpdateRightHolder(card.getEppn(), EsupSgcOperation.DESACTIVATE);
			} catch(CrousHttpClientErrorException clientEx) {
				if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode()) || HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
					// si compte non updatable car non créé, locké, ... on considère que c'est ok pour l'invalidation : pas besoin de la faire.
					log.info("Exception calling api crous - crousService.invalidate " + clientEx);
				} else {
					throw new SgcRuntimeException("Exception calling api crous - crousService.invalidate " + clientEx, clientEx);
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
					} catch(CrousHttpClientErrorException clientEx) {
						crousLogService.logErrorCrousAsync(clientEx);
						log.warn("Exception calling api crous - crousService.invalidate " + clientEx, clientEx);
						throw new SgcRuntimeException("Exception calling api crous - crousService.invalidate " + clientEx, clientEx);
					}
				}
			}
		}
	}
	
	public RightHolder getRightHolder(User user) {
		RightHolder rightHolder = null;
		if(user.getCrousIdentifier() != null && !user.getCrousIdentifier().isEmpty()) {
			rightHolder = this.getRightHolder(user.getCrousIdentifier(), user.getEppn());
		}
		if(rightHolder == null && !user.getEppn().equals(user.getCrousIdentifier())) {
			rightHolder = this.getRightHolder(user.getEppn(), user.getEppn());
		}
		if(rightHolder == null && !StringUtils.isEmpty(user.getSupannCodeINE()) && !user.getSupannCodeINE().equals(user.getCrousIdentifier())) {
			rightHolder = this.getRightHolder(user.getSupannCodeINE(), user.getEppn());
		}
		return rightHolder;
	}

	public RightHolder getRightHolder(String identifier, String eppn) {
		try {
			return authApiCrousService.getRightHolder(identifier, eppn, EsupSgcOperation.GET);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				log.debug(String.format("RightHolder %s not found IN API CROUS", identifier, clientEx.getErrorBodyAsJson()));
				return null;
			} else if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				log.info(String.format("RightHolder %s locked IN API CROUS", identifier, clientEx.getErrorBodyAsJson()));
				return null;
			} else if(HttpStatus.FORBIDDEN.equals(clientEx.getStatusCode())) { 
				throw new CrousAccountForbiddenException("Forbidden - crous righolder hold by another institute ?", clientEx);
			}
			// no crouslogError in db for a getRightHolder
			// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
			log.warn("Exception calling api crous - crousService.getRightHolder " + clientEx, clientEx);
			throw new SgcRuntimeException("Exception calling api crous - crousService.getRightHolder " + clientEx, clientEx);
		}
	}
		
	public CrousSmartCard getCrousSmartCard(String csn) {
		try {
			return authApiCrousService.getCrousSmartCard(csn);
		} catch(CrousHttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				return null;
			}
			// no crouslogError in db for a getRightHolder
			// crousLogService.logErrorCrous(eppnOrEmail, null, clientEx.getResponseBodyAsString());
			log.warn("Exception calling api crous - crousService.getCrousSmartCard " + clientEx, clientEx);
			throw new SgcRuntimeException("Exception calling api crous - crousService.getCrousSmartCard " + clientEx, clientEx);
		}
	}
	
	public boolean postOrUpdateRightHolder(String eppn, EsupSgcOperation esupSgcOperation) {		
		User user = User.findUser(eppn);
		try {
			if(authApiCrousService.postOrUpdateRightHolder(eppn, esupSgcOperation)) {
				user.setCrousError("");
				return true;
			} else {
				return false;
			}
		} catch(CrousHttpClientErrorException clientEx) {
			crousLogService.logErrorCrousAsync(clientEx);
			log.warn("Exception calling api crous - crousService.postOrUpdateRightHolder " + clientEx, clientEx);	
			throw new SgcRuntimeException("Exception calling api crous - crousService.postOrUpdateRightHolder " + clientEx, clientEx);
		}
	}
	
	public void patchIdentifier(PatchIdentifier patchIdentifier, EsupSgcOperation esupSgcOperation) {
		try {
			authApiCrousService.patchIdentifier(patchIdentifier);
		} catch(CrousHttpClientErrorException clientEx) {
			List<User> users = User.findUsersByCrousIdentifier(patchIdentifier.getCurrentIdentifier()).getResultList();
			if(!users.isEmpty()) {
				clientEx.setEppn(users.get(0).getEppn());
				crousLogService.logErrorCrousAsync(clientEx);
			}
			log.warn("Exception calling api crous status code : " + clientEx, clientEx);
			throw new SgcRuntimeException("Exception calling api crous status code : " + clientEx, clientEx);
		}	
	}

	@Transactional
	public void enableCrous(String eppn) {
		User user = User.findUser(eppn);
		Card enabledCard = user.getEnabledCard();
		user.setCrous(true);
		userInfoService.updateUser(eppn, null);
		if(enabledCard != null) {
			this.validate(enabledCard);
		}
	}
}
