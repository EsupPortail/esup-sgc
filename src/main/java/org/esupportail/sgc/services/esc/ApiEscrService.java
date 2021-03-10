package org.esupportail.sgc.services.esc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.bouncycastle.util.encoders.Hex;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.EscrCard;
import org.esupportail.sgc.domain.EscrStudent;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ApiEscrService extends ValidateService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Resource
	AppliConfigService appliConfigService;
	
	RestTemplate restTemplate;
	
	String webUrl;

	String key;
	
	Boolean enable = false;
	
	String countryCode;
	
	Long picInstitutionCode;
	
	Long cardType;
	
	Map<Date, Long> cardTypes = new HashMap<Date, Long>();

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

	public void setCardTypes(Map<String, Long> cardTypesDateAsString) throws ParseException {
		cardTypes = new HashMap<Date, Long>();
		for(String dateAsString : cardTypesDateAsString.keySet()) {
			Date date = dateFormat.parse(dateAsString);
			cardTypes.put(date, cardTypesDateAsString.get(dateAsString));
		}
	}

	@Override
	public void validateInternal(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscrStudent(card.getEppn());
				if(card.getEscnUid() != null && !card.getEscnUid().isEmpty()) {
					postEscrCard(card);
				}
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString());
				throw clientEx; 
			}
		}
	}

	@Override
	public void invalidateInternal(Card card) {
		User user = User.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscrStudent(card.getEppn());
				if(card.getEscnUid() != null && !card.getEscnUid().isEmpty()) {
					deleteEscrCard(card);
				}
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString(), clientEx);
			}
		}
	}	
	
	public void postOrUpdateEscrStudent(String eppn) {
		User user = User.findUser(eppn);
		if(user.getEuropeanStudentCard() && enable) {
			EscrStudent escrStudent = getEscrStudent(eppn);
			if(escrStudent == null || EscrStudent.findEscrStudentsByEppnEquals(eppn).getResultList().isEmpty()) {
				postEscrStudent(eppn);
			} else {
				updateEscrStudent(eppn);
			}
		}
	}

	public EscrStudent getEscrStudent(String eppn) {
		String europeanStudentIdentifier = getEuropeanStudentIdentifier(eppn);
		if(europeanStudentIdentifier == null) {
			log.info("No europeanStudentIdentifier retrieved for " + eppn);
			return null;
		}
		try {
			String url = webUrl + "/students/" + europeanStudentIdentifier;
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(headers);
			log.debug("Try to get ESCR Student : " + europeanStudentIdentifier); 
			ResponseEntity<EscrStudent> response = restTemplate.exchange(url, HttpMethod.GET, entity, EscrStudent.class);
			log.info(eppn + " retrieved in ESCR as Student -> " + response.getBody());	
			return response.getBody();
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				log.info("No ESCR Student found on API for " + eppn);
				return null;
			} else {
				throw clientEx;
			}
		}
	}
	
	
	protected void postEscrStudent(String eppn) {
		String url = webUrl + "/students";
		HttpHeaders headers = this.getJsonHeaders();			
		EscrStudent escrStudent = this.computeEscrStudent(eppn);
		if(escrStudent == null) {
		    log.error(String.format("Can't compute escrStudent for %s, because EuropeanStudentIdentifier can't be generated ?", eppn));
		    return ;
		}
		HttpEntity entity = new HttpEntity(escrStudent, headers);
		log.debug("Try to post to ESCR Student : " + escrStudent); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info(eppn + " sent in ESCR as Student -> " + response.getBody());
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.CONFLICT.equals(clientEx.getStatusCode())) {
				// conflit -> un post précédent avait été fait mais non persité en base (transaction - TODO à régler avec @Transactional(propagation=Propagation.REQUIRES_NEW) ?)
				EscrStudent escrStudentFromEscr = getEscrStudent(eppn);
				if(!escrStudentFromEscr.getEmailAddress().equals(escrStudent.getEmailAddress())) {
					throw new SgcRuntimeException(String.format("Pb - conflit ESCR avec %s / %s - emails des utilisateurs en conflit : %s et %s" , 
							escrStudent.getEuropeanStudentIdentifier(), eppn, escrStudent.getEmailAddress(), escrStudentFromEscr.getEmailAddress()), clientEx);
				}
				log.info("POST précédent sur ESCR avait fonctionné mais pas la persistence en base pour cause probable de transaction interrompue");
			} else {
				throw clientEx;
			}
		}
		escrStudent.persist();
	}
	
	private void updateEscrStudent(String eppn) {
		EscrStudent escrStudentInESCR = EscrStudent.findEscrStudentsByEppnEquals(eppn).getSingleResult();
		EscrStudent escrStudentGoal = computeEscrStudent(eppn);
		if(!escrStudentInESCR.equals(escrStudentGoal) && escrStudentGoal!=null) {
			String europeanStudentIdentifierInESCR =  escrStudentInESCR.getEuropeanStudentIdentifier();
			escrStudentInESCR.updateWith(escrStudentGoal);
			String url = webUrl + "/students/" + europeanStudentIdentifierInESCR;
			HttpHeaders headers = this.getJsonHeaders();
			HttpEntity entity = new HttpEntity(escrStudentInESCR, headers);
			log.debug("Try to put/update ESCR Student : " + escrStudentInESCR); 
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			log.info(eppn + " updated in ESCR as Student -> " + response.getBody());	
		} 
	}
	
	private EscrStudent computeEscrStudent(String eppn) {
		String europeanStudentIdentifier = getEuropeanStudentIdentifier(eppn);
		if(europeanStudentIdentifier == null) {
			return null;
		}
		EscrStudent escrStudent = new EscrStudent();
		escrStudent.setEppn(eppn);
		escrStudent.setEuropeanStudentIdentifier(europeanStudentIdentifier);
		escrStudent.setPicInstitutionCode(picInstitutionCode);
		User user = User.findUser(eppn);
		escrStudent.setEmailAddress(user.getEmail());
		escrStudent.setExpiryDate(user.getDueDate());
		escrStudent.setName(user.getDisplayName());
		escrStudent.setAcademicLevel(user.getAcademicLevel());
		return escrStudent;
		
	}

	public EscrCard getEscrCard(String eppn, String csn) {
		Card card = Card.findCardByCsn(csn);
		if(card.getEscnUid() == null || card.getEscnUid().isEmpty() || getEuropeanStudentIdentifier(eppn) == null || !enable) {
			return null;
		} else {
			String url = webUrl + "/students/" + getEuropeanStudentIdentifier(eppn) + "/cards/" + card.getEscnUid();
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(headers);
			log.debug(String.format("Try to get ESCR Card : %s - %s - %s - %s" , eppn, getEuropeanStudentIdentifier(eppn), csn, card.getEscnUid())); 
			try {
				ResponseEntity<EscrCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, EscrCard.class);
				log.info(csn + " retrieved in ESCR as Card -> " + response.getBody());	
				return response.getBody();
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.info(String.format("Card non trouvé sur ESCR pour %s - %s : %s  ", eppn, csn, clientEx.getResponseBodyAsString()), clientEx);
					return null;
				} else {
					throw clientEx;
				}
			}	
		}
	}
	
	protected void postEscrCard(Card card) {
		String europeanStudentIdentifier = getEuropeanStudentIdentifier(card.getEppn());
		String url = webUrl + "/students/" + europeanStudentIdentifier + "/cards";
		HttpHeaders headers = this.getJsonHeaders();			
		EscrCard escrCard = this.computeEscrCard(card);
		HttpEntity entity = new HttpEntity(escrCard, headers);
		log.debug("Try to post to ESCR Card : " + escrCard); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info(card.getCsn() + " sent in ESCR as Card -> " + response.getBody());	
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.CONFLICT.equals(clientEx.getStatusCode())) {
				// conflit -> un post précédent avait été fait mais non persité en base (transaction - TODO à régler avec @Transactional(propagation=Propagation.REQUIRES_NEW) ?)
				EscrCard escrCardFromEscr = getEscrCard(card.getEppn(), card.getCsn());
				if(!escrCardFromEscr.getCardUid().equals(escrCard.getCardUid())) {
					throw new SgcRuntimeException(String.format("Pb - conflit ESCR avec %s / %s - cardUid (csn) des utilisateurs en conflit : %s et %s" , 
							escrCard.getEuropeanStudentCardNumber(), escrCardFromEscr.getEuropeanStudentCardNumber(), escrCard.getCardUid(), escrCardFromEscr.getCardUid()), clientEx);
				}
				log.info("POST précédent sur ESCR avait fonctionné mais pas la persistence en base pour cause probable de transaction interrompue");
			} else {
				throw clientEx;
			}
		}
		escrCard.persist();
	}
	
	protected void deleteEscrCard(Card card) {
		EscrStudent escrStudent = EscrStudent.findEscrStudentsByEppnEquals(card.getEppn()).getSingleResult();		
		List<EscrCard> escrCards = EscrCard.findEscrCardsByCardUidEquals(card.getCsn()).getResultList();
		if(escrCards.isEmpty()) {
			log.warn("No EscrCard found for this card " + card.getCsn() + " so we can't desactivate it on ESCR");
		} else {
			EscrCard escrCard = escrCards.get(0);			
			String europeanStudentIdentifier = escrCard.getEuropeanStudentCardNumber();
			String url = webUrl + "/students/" + escrStudent.getEuropeanStudentIdentifier() + "/cards/" + europeanStudentIdentifier;
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(null, headers);
			log.debug("Try to delete card : " + escrCard); 
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
				log.info(card.getCsn() + " deleted in ESCR -> " + response.getBody());
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.warn("No need to delete " + card.getCsn() + " in ESCR because not found in ESCR");
				} else {
					throw clientEx;
				}
			}			
		}
	}

	private EscrCard computeEscrCard(Card card) {
		EscrCard escrCard = new EscrCard();
		escrCard.setEuropeanStudentCardNumber(card.getEscnUid());
		escrCard.setCardType(getCardType(card));
		escrCard.setCardUid(card.getCsn());
		return escrCard;
	}
	
	protected Long getCardType(Card card) {
		Long type = cardType;
		TreeSet<Date> dates = new TreeSet<Date>(cardTypes.keySet());
		for(Date date : dates) {
			if(card.getEncodedDate().after(date)) {
				type = cardTypes.get(date);
			}
		}
		return type;
	}

	private HttpHeaders getJsonHeaders() {	
		HttpHeaders headers = new HttpHeaders();
		headers.set("Key", key);
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("User-Agent", appliConfigService.getEsupSgcAsHttpUserAgent());
		return headers;
	}
	
	protected String getEuropeanStudentIdentifier(String eppn) {
		String esi = null;
		List<EscrStudent> escrStudentsInESCR = EscrStudent.findEscrStudentsByEppnEquals(eppn).getResultList();
		if(!escrStudentsInESCR.isEmpty()) {
			// ESI can't be modified in ESCR
			// Si/quand l'API permettra de modifier le ESI, il suffira de supprimer ce bloc
			EscrStudent escrStudentInESCR = escrStudentsInESCR.get(0);
			esi = escrStudentInESCR.getEuropeanStudentIdentifier();
		} else {
			User user = User.findUser(eppn);
			String supannCodeINE = user.getSupannCodeINE();
			if(supannCodeINE==null || supannCodeINE.isEmpty()) {
				log.info(eppn + " has no or empty supannCodeINE and this attribute is required for the European Student Card !");
				return null;
			}
			esi = String.format("urn:schac:personalUniqueCode:int:esi:%s:%s", countryCode.toLowerCase(), supannCodeINE);
		}
		return esi;
	}

	@Transactional
	public boolean validateESCenableCard(String eppn) {
		User user = User.findUser(eppn);
		Card enabledCard = user.getEnabledCard();			
		if(enabledCard != null && enabledCard.getEscnUid() != null && !enabledCard.getEscnUid().isEmpty()) {
			if(EscrStudent.findEscrStudentsByEppnEquals(user.getEppn()).getResultList().isEmpty() || EscrCard.findEscrCardsByCardUidEquals(enabledCard.getCsn()).getResultList().isEmpty()) {
				this.validate(enabledCard);
				log.info(String.format("Validation on API ESCR for %s with card %s OK", user.getEppn(), enabledCard.getCsn()));
				return true;
			}
		}
		return false;
	}
	
	public String getCaChainCertAsHexa(String picInstitutionCode) {
		String urlTemplate = webUrl.replaceFirst("/v1", "/certs/files/%s/ca-chain.cert.pem");
		String url = String.format(urlTemplate, picInstitutionCode);
		HttpHeaders headers = this.getJsonHeaders();			
		HttpEntity entity = new HttpEntity(headers);
		log.info("Try to get CA Chain Cert on " + url); 
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		log.info("CA Chain Cert for " + picInstitutionCode + " -> \n" + response.getBody());
		byte[] certAsBytesArray = response.getBody().getBytes();
		return Hex.toHexString(certAsBytesArray);
	}
	
}

