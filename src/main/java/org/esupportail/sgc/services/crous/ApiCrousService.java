package org.esupportail.sgc.services.crous;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousErrorLog.CrousOperation;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.UnknownHttpStatusCodeException;

public class ApiCrousService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static Map<MotifDisable, String> motifsDisableCrousMapping = new HashMap<MotifDisable, String>();
    static {
        motifsDisableCrousMapping.put(MotifDisable.LOST, "DP");
        motifsDisableCrousMapping.put(MotifDisable.THEFT, "DV");
        motifsDisableCrousMapping.put(MotifDisable.OUT_OF_ORDER, "CD");
    }
    
    private static String defaultCnousMotifDisable = "HS";
    
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
	@Resource
	CrousLogService crousLogService;
    
	@Resource
	AppliConfigService appliConfigService;
	
	RestTemplate restTemplate;
	
	String webUrl;
	
	String appId;
	
	String appSecret;
	
	String authToken = null;
	
	Boolean enable = false;	
	
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public synchronized void authenticate() {
		if(enable) {
			String url = webUrl + "/token";
			String basicAuthorisation = String.format("%s:%s", appId, appSecret);
			basicAuthorisation = Base64.encodeBase64String(basicAuthorisation.getBytes());
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/x-www-form-urlencoded");
			headers.set("User-Agent", appliConfigService.getEsupSgcAsHttpUserAgent());
			headers.set("Authorization", basicAuthorisation);
			log.trace(String.format("Headers for CROUS API authentication : %s", headers));
			String body = "grant_type=client_credentials";
			if("https://api.lescrous.fr/v1".equals(webUrl)) {
				body += "&env=PRD";
			} else if("https://api-pp.nuonet.fr/v1".equals(webUrl)) {
				body += "&env=PPD";
			} else {
				log.error("webUrl should be https://api-pp.nuonet.fr/v1 or https://api.nuonet.fr/v1 ?!");
			}
			log.trace(String.format("Body for CROUS API authentication : %s", body));
			HttpEntity entity = new HttpEntity(body, headers);	
			HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			HttpHeaders httpHeadersResponse = response.getHeaders();
			log.info("Headers response for crous api authentication : " + httpHeadersResponse);
			List<String> authorizations = httpHeadersResponse.get("authorization");
			if(authorizations!=null && !authorizations.isEmpty()) {
				authToken = authorizations.get(0);
				log.info("Auth Token of Crous API is renew : " + authToken);
				
			} else {
				throw new SgcRuntimeException("No authorization header when crous authentication : " + httpHeadersResponse, null);
			}
		}
	}
	
	private HttpHeaders getJsonHeaders() {	
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("User-Agent", appliConfigService.getEsupSgcAsHttpUserAgent());
		return headers;
	}
	
	private HttpHeaders getAuthHeaders() {		
		HttpHeaders headers = getJsonHeaders();
		if(authToken == null) {
			authenticate();
		}
		headers.set("Authorization", authToken);
		return headers;
	}
	
	
	public RightHolder getRightHolder(String identifier, String eppn, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {		
		if(enable) {
			String url = webUrl + "/rightholders/" + identifier;
			HttpHeaders headers = this.getAuthHeaders();	
			HttpEntity entity = new HttpEntity(headers);		
			try {
				ResponseEntity<RightHolder> response = restTemplate.exchange(url, HttpMethod.GET, entity, RightHolder.class);	
				return response.getBody();
			} catch(HttpClientErrorException clientEx) {
				throw new CrousHttpClientErrorException(clientEx, eppn, null, CrousOperation.GET, esupSgcOperation, url);
			}
		} 
		return null;
	}

	public CrousSmartCard getCrousSmartCard(String csn) throws CrousHttpClientErrorException {
		Card card = Card.findCardByCsn(csn);
		CrousSmartCard crousSmartCard = null;
		if(enable && card!=null) {
			if(enable) {
				User user = User.findUser(card.getEppn());
				String url = webUrl + "/rightholders/" + user.getCrousIdentifier() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
				HttpHeaders headers = this.getAuthHeaders();	
				HttpEntity entity = new HttpEntity(headers);	
				try {
					ResponseEntity<CrousSmartCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, CrousSmartCard.class);
					crousSmartCard = response.getBody();
					log.info("GET on " + url + " is OK : " + crousSmartCard);
				} catch(HttpClientErrorException clientEx) {
					throw new CrousHttpClientErrorException(clientEx, null, csn, CrousOperation.GET, null, url);
				}
			}	
		} 
		return crousSmartCard;
	}
	
	
	
	public boolean postOrUpdateRightHolder(String eppn, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {	
		User user = User.findUser(eppn);
		return postOrUpdateRightHolder(user, esupSgcOperation);
	}
	
	protected boolean postOrUpdateRightHolder(User user, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {	
		if(enable) {
			String eppn = user.getEppn();
			String crousIdentifier = user.getCrousIdentifier();
			if(crousIdentifier == null || crousIdentifier.isEmpty()) {
				// cas où le compte existe déjà côté izly sans qu'esup-sgc ne le connaisse encore
				crousIdentifier = computeEsupSgcRightHolder(user, true).getIdentifier();
			}		
			try {
				log.debug("Try to get RightHolder of " + eppn + " with identifier " + crousIdentifier);
				RightHolder oldRightHolder = getRightHolder(crousIdentifier, eppn, esupSgcOperation);
				log.info("Getting RightHolder for " + crousIdentifier + " : " + oldRightHolder.toString());
				RightHolder newRightHolder = computeEsupSgcRightHolder(user, true);
				if(log.isTraceEnabled()) {
					log.trace(String.format("newRightHolder.fieldWoDueDateEquals(oldRightHolder) : %s", fieldsEqualsOrCanNotBeUpdate(newRightHolder, oldRightHolder)));
					log.trace(String.format("mustUpdateDueDateCrous(oldRightHolder, eppn) : %s", mustUpdateDueDateCrous(oldRightHolder, eppn)));
				}
				if(!crousIdentifier.equals(user.getCrousIdentifier())) {
					// cas où le compte existe déjà côté izly sans qu'esup-sgc ne le connaisse encore - bis
					user.setCrousIdentifier(crousIdentifier);
				}
				if(!fieldsEqualsOrCanNotBeUpdate(newRightHolder, oldRightHolder) || mustUpdateDueDateCrous(oldRightHolder, eppn)) {
					if(!newRightHolder.getIdentifier().equals(oldRightHolder.getIdentifier())) {
						PatchIdentifier patchIdentifier = new PatchIdentifier();
						patchIdentifier.setCurrentIdentifier(oldRightHolder.getIdentifier());
						patchIdentifier.setEmail(oldRightHolder.getEmail());
						patchIdentifier.setNewIdentifier(newRightHolder.getIdentifier());		
						try {
							this.patchIdentifier(patchIdentifier);
						} catch(CrousHttpClientErrorException clientEx) {
							clientEx.setEsupSgcOperation(esupSgcOperation);
							log.warn("patchIdentifier on " + eppn + " failed : " + clientEx.getErrorBodyAsJson());
							crousLogService.logErrorCrous(clientEx);	
						} 
					}
					return updateRightHolder(eppn, oldRightHolder, esupSgcOperation);
				} 
			} catch(CrousHttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					// HACK : si user.getCrousIdentifier() renseigné mais not found côté API CROUS -> incohérent
					// on le réinitialise et on relance
					if(user.getCrousIdentifier() != null && !user.getCrousIdentifier().isEmpty()) {
						log.info("crousIdentifier " + user.getCrousIdentifier() + " not found sur l'API CROUS -> crousIdentifier remis à null pour " + eppn);
						user.setCrousIdentifier(null);
						return postOrUpdateRightHolder(user, esupSgcOperation);
					}
					return postRightHolder(user, esupSgcOperation);
				} else if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {	
					crousLogService.logErrorCrous(clientEx);
					log.info("LOCKED : " + clientEx.getErrorBodyAsJson());
					log.info("Getting Crous RightHolder failed : IZLY account is locked");
					return false;
				} else {
					throw clientEx;
				} 
			}
		}
		return true;
	}


	boolean fieldsEqualsOrCanNotBeUpdate(RightHolder newRightHolder, RightHolder oldRightHolder) {
		// pour les étudiants, pas d'update sur nom/prenom/email/datedenaissance/idRate quelque soit l'état du compte
		// ces infos sont normalement créés par l'import du fichier de la CVEC et l'api ne peut pas les modifier
		if(StringUtils.isNotEmpty(oldRightHolder.getIne()) && StringUtils.isNotEmpty(newRightHolder.getIne())) {
			// ETUDIANT
			for(String varStringName : Arrays.asList(new String[] {"identifier", "ine", "rneOrgCode", "idCompanyRate"})) {
				if(!newRightHolder.checkEqualsVar(varStringName, oldRightHolder)) {
					return false;
				}
			}			
		} else {
			// NON ETUDIANT
			if (newRightHolder.getBirthDate() == null) {
				if (oldRightHolder.birthDate != null) {
					log.info(String.format("RightHolder not equals because birthDate is not equals : %s <> %s", newRightHolder.getBirthDate(), oldRightHolder.getBirthDate()));
					return false;
				}
			} 
			// compare only day (without time) for birthday 
			else if (DateTimeComparator.getDateOnlyInstance().compare(newRightHolder.getBirthDate(), oldRightHolder.getBirthDate())!=0) {
				log.info(String.format("RightHolder not equals because birthDate is not equals : %s <> %s", newRightHolder.getBirthDate(), oldRightHolder.getBirthDate())); 
				return false;
			}

			for(String varStringName : Arrays.asList(new String[] {"email", "firstName", "identifier", "lastName", "ine", "rneOrgCode", "idCompanyRate", "idRate"})) {
				if(!newRightHolder.checkEqualsVar(varStringName, oldRightHolder)) {
					return false;
				}
			}			
		}
		return true;
	}

	private boolean postRightHolder(User user, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {
		if(user.getDueDate()!=null && user.getDueDate().before(new Date())) {
			log.info(String.format("%s not sent in CROUS because his due date is in past : %s", user.getEppn(), user.getDueDate()));
			return false;
		}
		String url = webUrl + "/rightholders";
		HttpHeaders headers = this.getAuthHeaders();			
		RightHolder rightHolder = this.computeEsupSgcRightHolder(user, false);
		HttpEntity entity = new HttpEntity(rightHolder, headers);
		log.debug("Try to post to CROUS RightHolder : " + rightHolder); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			user.setCrousIdentifier(rightHolder.getIdentifier());
		} catch(HttpClientErrorException clientEx) {
			CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, user.getEppn(), null, CrousOperation.POST, esupSgcOperation, url);
			if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				log.warn(user.getEppn() + " is locked in crous : " + clientEx.getResponseBodyAsString());
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				return false;		
			} else if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode())) {
				log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
				if(Arrays.asList(new String[] {"-9", "-8", "-41", "-117", "-42"}).contains(getErrorCode(clientEx.getResponseBodyAsString()))) {
					crousLogService.logErrorCrous(crousHttpClientErrorException);
					log.info(getErrorMessage(clientEx.getResponseBodyAsString()));
					return false;
				} else {
					log.warn("UNPROCESSABLE_ENTITY when posting RightHolder : " + rightHolder + " -> crous error response : " + clientEx.getResponseBodyAsString());
				}
			} 
			throw crousHttpClientErrorException;
		}
		log.info(user.getEppn() + " sent in CROUS as RightHolder");	
		return true;
	}

	private boolean updateRightHolder(String eppn, RightHolder oldRightHolder, EsupSgcOperation esupSgcOperation) throws CrousHttpClientErrorException {
		User user = User.findUser(eppn);
		String url = webUrl + "/rightholders/" + user.getCrousIdentifier();
		HttpHeaders headers = this.getAuthHeaders();			
		RightHolder rightHolder = this.computeEsupSgcRightHolder(user, false);
		// hack crous duedate étudiants 
		if(Long.valueOf(10).equals(oldRightHolder.getIdCompanyRate())
				&& Long.valueOf(10).equals(rightHolder.getIdCompanyRate())
				&& oldRightHolder.getDueDate().after(rightHolder.getDueDate())) {
			log.warn(String.format("For Crous/Izly, change of date for a student only if we add time - here it's not the case for %s."
					+ "Actual dueDate : %s ; wanted dueDate : %s -> we keep the actual dueDate", 
					oldRightHolder.getIdentifier(),
					oldRightHolder.getDueDate(), 
					rightHolder.getDueDate()));
			rightHolder.setDueDate(oldRightHolder.getDueDate());
		}
		// hack crous tarifs spéciaux étudiants ~boursiers
		if(Long.valueOf(10).equals(oldRightHolder.getIdCompanyRate())
				&& !oldRightHolder.getIdRate().equals(rightHolder.getIdRate())
				&& Long.valueOf(10).equals(rightHolder.getIdCompanyRate())) {
			log.debug(String.format("For student, idRate can be set up by Crous/Izly - "
					+ "For %s : idRate - %s->%s", 
					oldRightHolder.getIdentifier(),
					rightHolder.getIdRate(),
					oldRightHolder.getIdRate()));
			rightHolder.setIdRate(oldRightHolder.getIdRate());
			user.setIdRate(rightHolder.getIdRate());
		}
		HttpEntity entity = new HttpEntity(rightHolder, headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			log.info(eppn + " sent in CROUS as RightHolder");
		} catch(HttpClientErrorException clientEx) {
			CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, eppn, null, CrousOperation.PUT, esupSgcOperation, url);
			if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				log.warn(eppn + " is locked in crous : " + clientEx.getResponseBodyAsString());
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				return false;
			} else if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode())) {
				log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
				if(Arrays.asList(new String[] {"-9", "-8", "-41", "-117", "-42"}).contains(getErrorCode(clientEx.getResponseBodyAsString()))) {
					crousLogService.logErrorCrous(crousHttpClientErrorException);
					log.info(getErrorMessage(clientEx.getResponseBodyAsString()));
					return false;
				} else {
					log.warn("UNPROCESSABLE_ENTITY when updating RightHolder : " + rightHolder + " -> crous error response : " + clientEx.getResponseBodyAsString());
				}
			}
			throw crousHttpClientErrorException;
		} catch(RestClientResponseException clientEx2) {
			CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx2, eppn, null, CrousOperation.PUT, esupSgcOperation, url);
			if(clientEx2.getRawStatusCode() == 462) {
				// correspond à une erreur type "Le compte a un rne prioritaire qui est différent du rne proposé "
				// Lorsque l'étudiant est inscrit dans 2 établissements, seul l'établissement propriétaire peut modifier le compte.
				// par contre, tous les établissements peuvent ajouter une carte
				// (Note : s'il y a changement de date de fin de validité (== nouvelle année, nouvelle inscription) alors l'update est ok : reset de ce rne prioritaire)
				log.info("NOT_ACCEPTABLE : " + clientEx2.getResponseBodyAsString());
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				log.info(getErrorMessage(clientEx2.getResponseBodyAsString()));
				return false;
			}
			throw crousHttpClientErrorException;
		}
		return true;
	}
	

	public RightHolder computeEsupSgcRightHolder(User user, Boolean ineAsCrousIdentifierIfPossible) {
		RightHolder rightHolder = new RightHolder();
		if(!ineAsCrousIdentifierIfPossible && user.getCrousIdentifier() != null && !user.getCrousIdentifier().isEmpty()) {
			rightHolder.setIdentifier(user.getCrousIdentifier());
		} else if(appliConfigService.getCrousIneAsIdentifier() && user.getSupannCodeINE() != null && !user.getSupannCodeINE().isEmpty()) {
			rightHolder.setIdentifier(user.getSupannCodeINE());
		} else if(user.getCrousIdentifier() != null && !user.getCrousIdentifier().isEmpty()) {
			rightHolder.setIdentifier(user.getCrousIdentifier());
		} else {
			rightHolder.setIdentifier(user.getEppn());
		}
		rightHolder.setFirstName(user.getFirstname());
		rightHolder.setLastName(user.getName());
		rightHolder.setEmail(user.getEmail());
		
		// dueDate can't be past in IZLY -> max = now + 3 hours
		Date dueDate = user.getDueDate();
		Calendar cal = Calendar.getInstance(); // creates calendar
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 3);
		Date nowDate =  cal.getTime(); 
		if(dueDate!=null && dueDate.before(nowDate)) {
			dueDate = nowDate;
		} else {
			dueDate = user.getDueDate();
		}
		
		rightHolder.setDueDate(dueDate);
		rightHolder.setIdCompanyRate(user.getIdCompagnyRate());
		rightHolder.setIdRate(user.getIdRate());
		rightHolder.setBirthDate(user.getBirthday());
		rightHolder.setIne(user.getSupannCodeINE());
		rightHolder.setRneOrgCode(user.getRneEtablissement());
		return rightHolder;
	}
	
	private boolean mustUpdateDueDateCrous(RightHolder oldRightHolder, String eppn) {
		// dueDate can't be past in IZLY -> hack ...
		User user = User.findUser(eppn);
		if(user != null) {		
			Date now = new Date();
			Date duedateCrous = oldRightHolder.getDueDate();
			Date realDueDate = user.getDueDate();
			if(duedateCrous!=null && realDueDate.before(now) && duedateCrous.before(now)) {
				return false;
			}
			return !equalsWithInterval(realDueDate, duedateCrous);
		}
		return false;
	}
	
	
	/**
	 * True si les dates sont égales à 5 minutes près.
	 */
	private boolean equalsWithInterval(Date realDueDate, Date duedateCrous) {
		return Math.abs(realDueDate.getTime() - duedateCrous.getTime()) < 1000*60*5;
	}

	public boolean validateSmartCard(Card card) throws CrousHttpClientErrorException {
		if(enable) {
			User user = User.findUser(card.getEppn());
			String url = webUrl + "/rightholders/" + user.getCrousIdentifier() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
			HttpHeaders headers = this.getAuthHeaders();	
			HttpEntity entity = new HttpEntity(headers);		
			try {
				ResponseEntity<CrousSmartCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, CrousSmartCard.class);
				log.info("GET on " + url + " is OK : " + response.getBody() + " we revalidate this smartCard");
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.info("Card not found in crous - we try to send card " + card.getCsn() + " - " + card.getCrousSmartCard().getIdZdc() + " in CROUS");
					return validateNewSmartCard(card);
				} else {
					CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, card.getEppn(), card.getCsn(), CrousOperation.GET, EsupSgcOperation.ACTIVATE, url);
					throw crousHttpClientErrorException;
				}
			}		
			return revalidateSmartCard(card);
		}
		return true;
	}
	
	private boolean validateNewSmartCard(Card card) throws CrousHttpClientErrorException {
		User user = User.findUser(card.getEppn());
		String url = webUrl + "/rightholders/" + user.getCrousIdentifier() + "/smartcard";
		HttpHeaders headers = this.getAuthHeaders();
		CrousSmartCard smartCard = card.getCrousSmartCard();
		HttpEntity entity = new HttpEntity(smartCard, headers);
		log.debug("Try to post to CROUS SmartCard for " +  card.getEppn() + " : " + smartCard);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info("Card with csn " + card.getCsn() + " sent in CROUS as CrousSmartCard");
		} catch(HttpClientErrorException clientEx) {
			CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, card.getEppn(), card.getCsn(), CrousOperation.POST, EsupSgcOperation.ACTIVATE, url);
			if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				log.info("LOCKED : " + clientEx.getResponseBodyAsString());
				log.info("Card can't be added : IZLY account is locked");
				return false;
			}
			if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode()) && "-31".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
				log.info("Card can't be added : IZLY card is known but righHolder was deleted (rgpd)");
				return false;
			}
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				log.info("NOT_FOUND : " + clientEx.getResponseBodyAsString());
				log.info("Card can't be added : IZLY account should be closed (see logs before)");
				return false;
			}
			throw crousHttpClientErrorException;
		}
		return true;
	}
	
	
	private boolean revalidateSmartCard(Card card) throws CrousHttpClientErrorException {
		User user = User.findUser(card.getEppn());
		String url = webUrl + "/rightholders/" + user.getCrousIdentifier() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
		HttpHeaders headers = this.getAuthHeaders();
		headers.add("uid", card.getCsn().toUpperCase());
		Map<String, String> body = new HashMap<String, String>();
		body.put("revalidationDate", currentDate4Crous());
		body.put("cancelDate", "");
		body.put("reason", "");
		HttpEntity entity = new HttpEntity(body, headers);
		log.debug("Try to patch on CROUS SmartCard for " +  card.getEppn() + " : " + body); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
			log.info("Card with csn " + card.getCsn() + " revalidated in CROUS as CrousSmartCard");
		} catch(HttpClientErrorException clientEx) {
			CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, card.getEppn(), card.getCsn(), CrousOperation.PATCH, EsupSgcOperation.ACTIVATE, url);
			if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode()) && "-8".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
				crousLogService.logErrorCrous(crousHttpClientErrorException);
				log.info("Due date past -8 : izly error code ...");
				return false;
			} 
			throw crousHttpClientErrorException;
		}	
		return true;
	}

	public boolean invalidateSmartCard(Card card) throws CrousHttpClientErrorException {
		if(enable) {
			User user = User.findUser(card.getEppn());
			CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(card.getCsn());
			String url = webUrl + "/rightholders/" + user.getCrousIdentifier() + "/smartcard/" + smartCard.getIdZdc();
			HttpHeaders headers = this.getAuthHeaders();
			headers.add("uid", card.getCsn().toUpperCase());
			Map<String, String> body = new HashMap<String, String>();
			body.put("cancelDate", currentDate4Crous());
			String reason = motifsDisableCrousMapping.get(card.getMotifDisable());
			if(reason==null) {
				reason = defaultCnousMotifDisable;
			}
			body.put("reason", reason);
			body.put("revalidationDate", "");
			HttpEntity entity = new HttpEntity(body, headers);
			log.debug("Try to patch on Crous SmartCard for " +  card.getEppn() + " : " + smartCard); 
			log.trace(String.format("Url : %s / Body : %s / Hraders : %s", url, body, headers));
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
				log.info("Card with csn " + card.getCsn() + " invalidated in CROUS as CrousSmartCard");
			} catch(HttpClientErrorException clientEx) {
				CrousHttpClientErrorException crousHttpClientErrorException = new CrousHttpClientErrorException(clientEx, card.getEppn(), card.getCsn(), CrousOperation.PATCH, EsupSgcOperation.DESACTIVATE, url);
				if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode())) {
					log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
					if(Arrays.asList(new String[] {"-9", "-8", "-41", "-117", "-42"}).contains(getErrorCode(clientEx.getResponseBodyAsString()))) {
						crousLogService.logErrorCrous(crousHttpClientErrorException);
						log.info(getErrorMessage(clientEx.getResponseBodyAsString()));
						return false;
					}
				} else if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					crousLogService.logErrorCrous(crousHttpClientErrorException);
					log.info("Card with csn " + card.getCsn() + " not found in CROUS as CrousSmartCard, no need to invalidate it.");
					return false;
				} else if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
					log.info(card.getEppn() + " is locked in crous : " + clientEx.getResponseBodyAsString());
					crousLogService.logErrorCrous(crousHttpClientErrorException);
					return false;		
				} 	
				throw crousHttpClientErrorException;
			}
		}
		return true;
	}
	
	private String getErrorMessage(String errorsAsJsonString) {
		return getErrorPart(errorsAsJsonString, "message");
	}
	private String getErrorCode(String errorsAsJsonString) {
		return getErrorPart(errorsAsJsonString, "code");
	}
	
	private String getErrorPart(String errorsAsJsonString, String part) {
		String errorPart = null;
		ObjectMapper mapper = new ObjectMapper(); 
	    TypeReference<Map<String,List<Map<String, String>>>> typeRef 
	            = new TypeReference<Map<String,List<Map<String, String>>>>() {};

	            Map<String,List<Map<String, String>>> errorsMap = null;
		try {
			errorsMap = mapper.readValue(errorsAsJsonString, typeRef);
		} catch (IOException e) {
			log.error("Can't parse errors : " + errorsAsJsonString, e);
		} 
		if(errorsMap!=null && errorsMap.get("errors")!=null) {
			List<Map<String, String>> errors = errorsMap.get("errors");
			if(!errors.isEmpty()) {
				Map<String, String> error = errors.get(0);
				errorPart = error.get(part);
			}
		}
	    return errorPart;
	}
	

	public void patchIdentifier(PatchIdentifier patchIdentifier) throws CrousHttpClientErrorException {
		if(enable) {
			String url = webUrl + "/rightholders/" + patchIdentifier.getCurrentIdentifier();
			HttpHeaders headers = this.getAuthHeaders();
			HttpEntity entity = new HttpEntity(patchIdentifier, headers);
			User user = null;
			List<User> users = User.findUsersByCrousIdentifier(patchIdentifier.getCurrentIdentifier()).getResultList();
			if(!users.isEmpty()) {
				user = users.get(0);
				if(user.getDueDate()!=null && user.getDueDate().before(new Date())) {
					log.warn(String.format("%s not patched in CROUS because his due date is in past : %s", user.getEppn(), user.getDueDate()));
					return;
				}
			}
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);	
				if(user != null) {
					user.setCrousIdentifier(patchIdentifier.getNewIdentifier());
					user.merge();
				}
				log.info("patchIdentifier : " + patchIdentifier + " OK : " + response.getBody());
			} catch(HttpClientErrorException clientEx) {
				throw new CrousHttpClientErrorException(clientEx, user!=null ? user.getEppn() : null, null, CrousOperation.PATCH, null, url);
			}
		}
	}

	private String currentDate4Crous() {
		return dateFormat.format(new Date());
	}

	public boolean isEnabled() {
		return enable;
	}
}


