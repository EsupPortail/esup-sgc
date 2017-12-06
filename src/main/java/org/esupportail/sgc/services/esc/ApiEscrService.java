package org.esupportail.sgc.services.esc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.EscrCard;
import org.esupportail.sgc.domain.EscrStudent;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ApiEscrService extends ValidateService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	RestTemplate restTemplate;
	
	String webUrl;

	String key;
	
	Boolean enable = false;
	
	String countryCode;
	
	Long picInstitutionCode;
	
	Long cardType;

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setPicInstitutionCode(Long picInstitutionCode) {
		this.picInstitutionCode = picInstitutionCode;
	}

	public void setCardType(Long cardType) {
		this.cardType = cardType;
	}

	@Override
	public void validate(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscrStudent(card.getEppn());
				postEscrCard(card);
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString());
				throw clientEx; 
			}
		}
	}

	@Override
	public void invalidate(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscrStudent(card.getEppn());
				deleteEscrCard(card);
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString());
				throw clientEx; 
			}
		}
	}	
	
	public void postOrUpdateEscrStudent(String eppn) {
		User user = User.findUser(eppn);
			if(user.getEuropeanStudentCard() && enable) {
			EscrStudent escrStudent = getEscrStudent(eppn);
			if(escrStudent == null) {
				postEscrStudent(eppn);
			} else {
				updateEscrStudent(eppn);
			}
		}
	}

	protected EscrStudent getEscrStudent(String eppn) {
		List<EscrStudent> escrStudents = EscrStudent.findEscrStudentsByEppnEquals(eppn).getResultList();
		if(escrStudents.isEmpty()) {
			return null;
		} else {
			EscrStudent escrStudentInDb = escrStudents.get(0);
			String url = webUrl + "/students/" + escrStudentInDb.getEuropeanStudentIdentifier();
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(headers);
			log.debug("Try to get ESCR Student : " + escrStudentInDb.getEuropeanStudentIdentifier()); 
			ResponseEntity<EscrStudent> response = restTemplate.exchange(url, HttpMethod.GET, entity, EscrStudent.class);
			log.info(eppn + " retireved in ESCR as Student -> " + response.getBody());	
			return response.getBody();
		}
	}
	
	
	protected void postEscrStudent(String eppn) {
		String url = webUrl + "/students";
		HttpHeaders headers = this.getJsonHeaders();			
		EscrStudent escrStudent = this.computeEscrStudent(eppn);
		HttpEntity entity = new HttpEntity(escrStudent, headers);
		log.debug("Try to post to ESCR Student : " + escrStudent); 
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		log.info(eppn + " sent in ESCR as Student -> " + response.getBody());	
		escrStudent.persist();
	}
	
	private void updateEscrStudent(String eppn) {
		EscrStudent escrStudent = EscrStudent.findEscrStudentsByEppnEquals(eppn).getSingleResult();
		User user = User.findUser(eppn);
		if(!picInstitutionCode.equals(escrStudent.getPicInstitutionCode()) || 
				!user.getEmail().equals(escrStudent.getEmailAddress()) || 
				!user.getDueDate().equals(escrStudent.getExpiryDate()) ||
				!user.getDisplayName().equals(escrStudent.getName())) {
			escrStudent.setEmailAddress(user.getEmail());
			escrStudent.setExpiryDate(user.getDueDate());
			escrStudent.setName(user.getDisplayName());
			String url = webUrl + "/students/" + escrStudent.getEuropeanStudentIdentifier();
			HttpHeaders headers = this.getJsonHeaders();
			HttpEntity entity = new HttpEntity(escrStudent, headers);
			log.debug("Try to put/update ESCR Student : " + escrStudent); 
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			log.info(user.getEppn() + " updated in ESCR as Student -> " + response.getBody());	
		} 
	}
	
	private EscrStudent computeEscrStudent(String eppn) {
		EscrStudent escrStudent = new EscrStudent();
		escrStudent.setEppn(eppn);
		escrStudent.setEuropeanStudentIdentifier(getEuropeanStudentIdentifier(eppn));
		escrStudent.setPicInstitutionCode(picInstitutionCode);
		User user = User.findUser(eppn);
		escrStudent.setEmailAddress(user.getEmail());
		escrStudent.setExpiryDate(user.getDueDate());
		escrStudent.setName(user.getDisplayName());
		return escrStudent;
		
	}
	
	protected void postEscrCard(Card card) {
		String europeanStudentIdentifier = getEuropeanStudentIdentifier(card.getEppn());
		String url = webUrl + "/students/" + europeanStudentIdentifier + "/cards";
		HttpHeaders headers = this.getJsonHeaders();			
		EscrCard escrCard = this.computeEscrCard(card);
		HttpEntity entity = new HttpEntity(escrCard, headers);
		log.debug("Try to post to ESCR Card : " + escrCard); 
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		log.info(card.getCsn() + " sent in ESCR as Student -> " + response.getBody());	
		escrCard.persist();
	}
	
	protected void deleteEscrCard(Card card) {
		EscrStudent escrStudent = EscrStudent.findEscrStudentsByEppnEquals(card.getEppn()).getSingleResult();
		String url = webUrl + "/students/" + escrStudent.getEuropeanStudentIdentifier() + "/cards";
		HttpHeaders headers = this.getJsonHeaders();			
		List<EscrCard> escrCards = EscrCard.findEscrCardsByCardUidEquals(card.getCsn()).getResultList();
		if(escrCards.isEmpty()) {
			log.warn("No EscrCard found for this card " + card.getCsn() + " so we can't desactivate it on ESCR");
		} else {
			EscrCard escrCard = escrCards.get(0);
			String europeanStudentIdentifier = escrCard.getEuropeanStudentCardNumber();
			Map<String, String> body = new HashMap<String, String>();
			body.put("europeanStudentIdentifier", europeanStudentIdentifier);
			HttpEntity entity = new HttpEntity(body, headers);
			log.debug("Try to delete card : " + escrCard); 
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			log.info(card.getCsn() + " deleted in ESCR -> " + response.getBody());
		}
	}

	private EscrCard computeEscrCard(Card card) {
		EscrCard escrCard = new EscrCard();
		escrCard.setEuropeanStudentCardNumber(card.getEscnUid());
		escrCard.setCardType(cardType);
		escrCard.setCardUid(card.getCsn());
		return escrCard;
	}

	private HttpHeaders getJsonHeaders() {	
		HttpHeaders headers = new HttpHeaders();
		headers.set("Key", key);
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		return headers;
	}
	
	protected String getEuropeanStudentIdentifier(String eppn) {
		User user = User.findUser(eppn);
		String supannCodeINE = user.getSupannCodeINE();
		if(supannCodeINE==null || supannCodeINE.isEmpty()) {
			throw new SgcRuntimeException(eppn + " has no or empty supannCodeINE and this attribute is required for the European Student Card !", null);
		}
		return countryCode + "-" + picInstitutionCode + "-" + supannCodeINE;
	}
	
	
}
