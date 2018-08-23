package org.esupportail.sgc.services.crous;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountLockException;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
	RestTemplate restTemplate;
	
	String webUrl;
	
	String login;
	
	String password;
	
	String authToken = null;
	
	Boolean enable = false;	
	
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public synchronized void authenticate() {
		if(enable) {
			String url = webUrl + "/authenticate";
			HttpHeaders headers = getJsonHeaders();
			Map<String, String> body = new HashMap<String, String>();
			body.put("login", login);
			body.put("password", password);
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
	
	
	public RightHolder getRightHolder(String eppnOrEmail) {		
		if(enable) {
			String url = webUrl + "/rightholders/" + eppnOrEmail;
			HttpHeaders headers = this.getAuthHeaders();	
			HttpEntity entity = new HttpEntity(headers);		
			ResponseEntity<RightHolder> response = restTemplate.exchange(url, HttpMethod.GET, entity, RightHolder.class);		
			return response.getBody();
		} 
		return null;
	}
	
	public CrousSmartCard getCrousSmartCard(String csn) {
		Card card = Card.findCard(csn);
		CrousSmartCard crousSmartCard = null;
		if(enable && card!=null) {
			if(enable) {
				String url = webUrl + "/rightholders/" + card.getEppn() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
				HttpHeaders headers = this.getAuthHeaders();	
				HttpEntity entity = new HttpEntity(headers);		
				ResponseEntity<CrousSmartCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, CrousSmartCard.class);
				crousSmartCard = response.getBody();
				log.info("GET on " + url + " is OK : " + crousSmartCard);
			}	
		} 
		return crousSmartCard;
	}
	
	public void postOrUpdateRightHolder(String eppn) {	
		if(enable) {
			String url = webUrl + "/rightholders/" + eppn;
			HttpHeaders headers = this.getAuthHeaders();	
			HttpEntity entity = new HttpEntity(headers);		
			try {
				ResponseEntity<RightHolder> response = restTemplate.exchange(url, HttpMethod.GET, entity, RightHolder.class);
				log.info("Getting RightHolder for " + eppn +" : " + response.toString());
				RightHolder oldRightHolder = response.getBody();
				RightHolder newRightHolder = this.computeEsupSgcRightHolder(eppn);
				// hack dueDate can't be past in IZLY 
				if(!newRightHolder.fieldWoDueDateEquals(oldRightHolder) || mustUpdateDueDateCrous(oldRightHolder, eppn)) {
					updateRightHolder(eppn);
				} 
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					postRightHolder(eppn);
					return ;
				} else {
					throw clientEx;
				}
			}
		}
	}


	private void postRightHolder(String eppn) {
		String url = webUrl + "/rightholders";
		HttpHeaders headers = this.getAuthHeaders();			
		RightHolder rightHolder = this.computeEsupSgcRightHolder(eppn);
		HttpEntity entity = new HttpEntity(rightHolder, headers);
		log.debug("Try to post to CROUS RightHolder : " + rightHolder); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				if("Account is closed".equals(getErrorMessage(clientEx.getResponseBodyAsString()))) {
					log.info("Account " + eppn + " is closed");
					return;
				} else {
					throw new CrousAccountLockException(clientEx.getResponseBodyAsString(), clientEx);
				}
			} else {
				throw clientEx;
			}
		}
		log.info(eppn + " sent in CROUS as RightHolder");	
	}


	private void updateRightHolder(String eppn) {
		String url = webUrl + "/rightholders/" + eppn;
		HttpHeaders headers = this.getAuthHeaders();			
		RightHolder rightHolder = this.computeEsupSgcRightHolder(eppn);
		HttpEntity entity = new HttpEntity(rightHolder, headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			log.info(eppn + " sent in CROUS as RightHolder");
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode())) {
				log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
				if("-9".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
					log.info("Card was already invalidated");
					return;
				} else if("-8".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
					log.info("Card was already invalidated : due date past");
					return;
				} else if("-41".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
					log.info("Client en opposition");
					return;
				} else {
					log.warn("UNPROCESSABLE_ENTITY when updating RightHolder : " + rightHolder + " -> crous error response : " + clientEx.getResponseBodyAsString());
				}
			} 
			throw clientEx;
		}
	}
	

	public RightHolder computeEsupSgcRightHolder(String eppn) {
		RightHolder rightHolder = null;
		List<User> users = User.findUsersByEppnEquals(eppn).getResultList();
		if(!users.isEmpty()) {
			User user = users.get(0);
			rightHolder = new RightHolder();
			rightHolder.setIdentifier(eppn);
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
		}
		return rightHolder;
	}
	
	private boolean mustUpdateDueDateCrous(RightHolder oldRightHolder, String eppn) {
		// dueDate can't be past in IZLY -> hack ...
		List<User> users = User.findUsersByEppnEquals(eppn).getResultList();
		if(!users.isEmpty()) {
			User user = users.get(0);			
			Date now = new Date();
			Date duedateCrous = oldRightHolder.getDueDate();
			Date realDueDate = user.getDueDate();
			if(duedateCrous!=null && realDueDate.before(now) && duedateCrous.before(now)) {
				return false;
			}
			return !realDueDate.equals(duedateCrous);
		}
		return false;
	}
	
	
	public void validateSmartCard(Card card) {
		if(enable) {
			String url = webUrl + "/rightholders/" + card.getEppn() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
			HttpHeaders headers = this.getAuthHeaders();	
			HttpEntity entity = new HttpEntity(headers);		
			try {
				ResponseEntity<CrousSmartCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, CrousSmartCard.class);
				log.info("GET on " + url + " is OK : " + response.getBody() + " we revalidate this smartCard");
				revalidateSmartCard(card);
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.info("Card not found in crous - we try to send card " + card.getCsn() + " - " + card.getCrousSmartCard().getIdZdc() + " in CROUS");
					validateNewSmartCard(card);
					return ;
				} else {
					throw clientEx;
				}
			}
		}
	}
	
	private void validateNewSmartCard(Card card) {
		String url = webUrl + "/rightholders/" + card.getEppn() + "/smartcard";
		HttpHeaders headers = this.getAuthHeaders();
		CrousSmartCard smartCard = card.getCrousSmartCard();
		HttpEntity entity = new HttpEntity(smartCard, headers);
		log.debug("Try to post to CROUS SmartCard for " +  card.getEppn() + " : " + smartCard);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info("Card with csn " + card.getCsn() + " sent in CROUS as CrousSmartCard");
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.LOCKED.equals(clientEx.getStatusCode())) {
				log.info("LOCKED : " + clientEx.getResponseBodyAsString());
				log.info("Card can't be added : IZLY account is locked");
				return;
			}
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				log.info("NOT_FOUND : " + clientEx.getResponseBodyAsString());
				log.info("Card can't be added : IZLY account should be closed (see logs before)");
				return;
			}
			throw clientEx;
		}
	}
	
	
	private void revalidateSmartCard(Card card) {
		String url = webUrl + "/rightholders/" + card.getEppn() + "/smartcard/" + card.getCrousSmartCard().getIdZdc();
		HttpHeaders headers = this.getAuthHeaders();
		headers.add("uid", card.getCsn().toUpperCase());
		Map<String, String> body = new HashMap<String, String>();
		body.put("revalidationDate", currentDate4Crous());
		body.put("cancelDate", "");
		body.put("reason", "");
		HttpEntity entity = new HttpEntity(body, headers);
		log.debug("Try to patch on CROUS SmartCard for " +  card.getEppn() + " : " + body); 
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
		log.info("Card with csn " + card.getCsn() + " revalidated in CROUS as CrousSmartCard");
	}

	public void invalidateSmartCard(Card card) {
		if(enable) {
			try {
				CrousSmartCard smartCard = CrousSmartCard.findCrousSmartCard(card.getCsn());
				String url = webUrl + "/rightholders/" + card.getEppn() + "/smartcard/" + smartCard.getIdZdc();
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
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);		
				log.info("Card with csn " + card.getCsn() + " invalidated in CROUS as CrousSmartCard");
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.UNPROCESSABLE_ENTITY.equals(clientEx.getStatusCode())) {
					log.info("UNPROCESSABLE_ENTITY : " + clientEx.getResponseBodyAsString());
					if("-9".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
						log.info("Card was already invalidated");
						return;
					} else if("-8".equals(getErrorCode(clientEx.getResponseBodyAsString()))) {
						log.info("Card was already invalidated : due date past");
						return;
					} 
				} else if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.info("Card with csn " + card.getCsn() + " not found in CROUS as CrousSmartCard, no need to invalidate it.");
					return;
				} 
				throw clientEx;
			}
		}
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
	

	public void patchIdentifier(PatchIdentifier patchIdentifier) {
		if(enable) {
			String url = webUrl + "/rightholders/" + patchIdentifier.getCurrentIdentifier();
			HttpHeaders headers = this.getAuthHeaders();
			HttpEntity entity = new HttpEntity(patchIdentifier, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);		
			log.info("patchIdentifier : " + patchIdentifier + " OK : " + response.getBody());
		}
	}

	private String currentDate4Crous() {
		return dateFormat.format(new Date());
	}

	public boolean isEnabled() {
		return enable;
	}

}


