package org.esupportail.sgc.services.esc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.EscCardDaoService;
import org.esupportail.sgc.dao.EscPersonDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.*;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ApiEscService extends ValidateService {

	private final Logger log = LoggerFactory.getLogger(getClass());

    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Resource
	AppliConfigService appliConfigService;

	@Resource
	EscPersonDaoService escPersonDaoService;

	@Resource
	EscCardDaoService escCardDaoService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

	RestTemplate restTemplate;

	String webUrl;

	String key;

	Boolean enable = false;

	String countryCode;

	Long picInstitutionCode;

	String vatProcessorInstitution;

	String cardType;

	Map<LocalDateTime, String> cardTypes = new HashMap<>();
	
	boolean abortActivationIfEscFails = false;

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

	public void setVatProcessorInstitution(String vatProcessorInstitution) {
    		this.vatProcessorInstitution = vatProcessorInstitution;
    	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public void setCardTypes(Map<String, String> cardTypesDateAsString) throws ParseException {
		cardTypes = new HashMap<LocalDateTime, String>();
		for(String dateAsString : cardTypesDateAsString.keySet()) {
            LocalDateTime date = LocalDateTime.parse(dateAsString, dateFormat);
			cardTypes.put(date, cardTypesDateAsString.get(dateAsString));
		}
	}

	@Override
	public void validateInternal(Card card) {
		User user = userDaoService.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscPerson(card.getEppn());
				if(card.getEscnUid() != null && !card.getEscnUid().isEmpty()) {
					postEscCard(card);
				}
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString());
                if(abortActivationIfEscFails) {
					throw clientEx; 
				}
			}
		}
	}

	@Override
	public void invalidateInternal(Card card) {
		User user = userDaoService.findUser(card.getEppn());
		if(user.getEuropeanStudentCard() && enable) {
			try {
				postOrUpdateEscPerson(card.getEppn());
				if(card.getEscnUid() != null && !card.getEscnUid().isEmpty()) {
					deleteEscCard(card);
				}
			} catch(HttpClientErrorException clientEx) {
				log.error("HttpClientErrorException : " + clientEx.getResponseBodyAsString(), clientEx);
			}
		}
	}	

	public void postOrUpdateEscPerson(String eppn) {
		if(eppn.matches(this.getEppnFilter())) {
			User user = userDaoService.findUser(eppn);
			if (user.getEuropeanStudentCard() && enable) {
				EscPerson escPerson = getEscPerson(eppn);
				if (escPerson == null || escPersonDaoService.findEscPersonsByEppnEquals(eppn).getResultList().isEmpty()) {
					postEscPerson(eppn);
				} else {
					updateEscPerson(eppn);
				}
			}
		}
	}

	public EscPerson getEscPerson(String eppn) {
		String europeanPersonIdentifier = getEuropeanPersonIdentifier(eppn);
		if(europeanPersonIdentifier == null) {
			log.info("No europeanPersonIdentifier retrieved for " + eppn);
			return null;
		}
		try {
			String url = webUrl + "/persons/" + europeanPersonIdentifier;
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(headers);
			log.debug("Try to get Esc Person : " + europeanPersonIdentifier); 
			ResponseEntity<EscPerson> response = restTemplate.exchange(url, HttpMethod.GET, entity, EscPerson.class);
			log.info(eppn + " retrieved in Esc as Person -> " + response.getBody());	
			return response.getBody();
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				log.info("No Esc Person found on API for " + eppn);
				return null;
			} else {
				try {
					EscError escError = (EscError) new ObjectMapper().readValue(clientEx.getResponseBodyAsByteArray(), EscError.class);
					if("SE-0000".equals(escError.getCode())) {
						// Access denied
						log.info("SE-0000 : " + escError.getMessage() + " for " + eppn);
						return null;
					} else {
						throw clientEx;
					}
				} catch (IOException e) {
					log.trace("Error parsing response body as EscError - " + clientEx.getResponseBodyAsString(), e);
					throw clientEx;
				}
			}
		}
	}

	protected void postEscPerson(String eppn) {
		String url = webUrl + "/persons";
		HttpHeaders headers = this.getJsonHeaders();			
		EscPerson escPerson = this.computeEscPerson(eppn);
		if(escPerson == null) {
			log.error(String.format("Can't compute EscPerson for %s, because EuropeanPersonIdentifier can't be generated ?", eppn));
			return ;
		}
		HttpEntity entity = new HttpEntity(escPerson, headers);
		log.debug("Try to post to Esc Person : " + escPerson);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info(eppn + " sent in Esc as Person -> " + response.getBody());
		} catch(HttpClientErrorException clientEx) {
			try {
				EscError escError =	(EscError) new ObjectMapper().readValue(clientEx.getResponseBodyAsByteArray(), EscError.class);
				if("ER-0019".equals(escError.getCode())) {
					// User already exists for this organisation
					log.warn("ER-0019 : " + escError.getMessage());
				} else {
					throw clientEx;
				}
			} catch (IOException e) {
				log.trace("Error parsing response body as EscError - " + clientEx.getResponseBodyAsString(), e);
				throw clientEx;
			}
		}
		escPersonDaoService.persist(escPerson);
	}

	protected void updateEscPerson(String eppn) {
		EscPerson escPersonInEscr = escPersonDaoService.findEscPersonsByEppnEquals(eppn).getSingleResult();
		EscPerson escPersonGoal = computeEscPerson(eppn);
		if(!escPersonInEscr.equals(escPersonGoal) && escPersonGoal !=null) {
			String europeanPersonIdentifierInEsc =  escPersonInEscr.getIdentifier();
			escPersonInEscr.updateWith(escPersonGoal);
			String url = webUrl + "/persons/" + europeanPersonIdentifierInEsc;
			HttpHeaders headers = this.getJsonHeaders();
			HttpEntity entity = new HttpEntity(escPersonInEscr, headers);
			log.debug("Try to put/update Esc Person : " + escPersonInEscr);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			log.info(eppn + " updated in Esc as Person -> " + response.getBody());	
		} 
	}


	public void deleteEscPerson(String eppn) {		
		String europeanPersonIdentifier = getEuropeanPersonIdentifier(eppn);
		if(europeanPersonIdentifier == null || !enable) {
			return;
		}
		String url = webUrl + "/persons/" + europeanPersonIdentifier;
		HttpHeaders headers = this.getJsonHeaders();			
		HttpEntity entity = new HttpEntity(null, headers);
		log.debug("Try to delete Person : " + europeanPersonIdentifier); 
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
			log.info(eppn + " deleted in Esc -> " + response.getBody());
		} catch(HttpClientErrorException clientEx) {
			if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
				log.warn("No need to delete " + eppn + " in Esc because not found in Esc");
			} else {
				throw clientEx;
			}
		}
        // local delete if exists
        List<EscPerson> escPersons = escPersonDaoService.findEscPersonsByEppnEquals(eppn).getResultList();
        if(!escPersons.isEmpty()) {
            log.debug("Local delete of EscPerson for " + eppn);
            escPersonDaoService.remove(escPersons.get(0));
        } 
	}



	private EscPerson computeEscPerson(String eppn) {
		String europeanPersonIdentifier = getEuropeanPersonIdentifier(eppn);
		if(europeanPersonIdentifier == null) {
			return null;
		}
		EscPerson escPerson = new EscPerson();
		escPerson.setEppn(eppn);
		escPerson.setIdentifier(europeanPersonIdentifier);
		escPerson.setFullName(userDaoService.findUser(eppn).getDisplayName());
		User user = userDaoService.findUser(eppn);
		EscPersonOrganisationUpdateView escPersonOrganisationUpdateView = new EscPersonOrganisationUpdateView();
		escPersonOrganisationUpdateView.setEmail(user.getEmail());
		if(user.getAcademicLevel()!=null) {
			if(user.getAcademicLevel().equals(6L)) {
				escPersonOrganisationUpdateView.setAcademicLevel(EscPersonOrganisationUpdateView.AcademicLevelEnum.BACHELOR);
			} else if(user.getAcademicLevel().equals(7L)) {
				escPersonOrganisationUpdateView.setAcademicLevel(EscPersonOrganisationUpdateView.AcademicLevelEnum.MASTER);
			} else if(user.getAcademicLevel().equals(8L)) {
				escPersonOrganisationUpdateView.setAcademicLevel(EscPersonOrganisationUpdateView.AcademicLevelEnum.DOCTORATE);
			}
		}
		Long pic = picInstitutionCode;
		if(!StringUtils.isEmpty(user.getPic())) {
			pic = Long.valueOf(user.getPic());
		}
		escPersonOrganisationUpdateView.setOrganisationIdentifier(pic.toString());
		escPerson.getPersonOrganisationUpdateViews().add(escPersonOrganisationUpdateView);
		return escPerson;

	}

	public EscCard getEscCard(String eppn, String csn) {
		Card card = cardDaoService.findCardByCsn(csn);
		if(card.getEscnUid() == null || card.getEscnUid().isEmpty() || getEuropeanPersonIdentifier(eppn) == null || !enable) {
			return null;
		} else {
			String url = webUrl + "/cards/" + card.getEscnUid() + "/status";
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(headers);
			log.debug(String.format("Try to get Esc Card : %s - %s - %s - %s" , eppn, getEuropeanPersonIdentifier(eppn), csn, card.getEscnUid())); 
			try {
				ResponseEntity<EscCard> response = restTemplate.exchange(url, HttpMethod.GET, entity, EscCard.class);
				log.info(csn + " retrieved in Esc as Card -> " + response.getBody());	
				return response.getBody();
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.info(String.format("Card non trouvé sur Esc pour %s - %s : %s  ", eppn, csn, clientEx.getResponseBodyAsString()), clientEx);
					return null;
				} else {
					throw clientEx;
				}
			}	
		}
	}

	protected void postEscCard(Card card) {
		String url = webUrl + "/cards";
		HttpHeaders headers = this.getJsonHeaders();			
		EscCard escCard = this.computeEscCard(card);
		HttpEntity entity = new HttpEntity(escCard, headers);
		log.debug("Try to post to Esc Card : " + escCard);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			log.info(card.getCsn() + " sent/post in Esc as Card -> " + response.getBody());
		} catch(HttpClientErrorException clientEx) {
			try {
				EscError escError =	(EscError) new ObjectMapper().readValue(clientEx.getResponseBodyAsByteArray(), EscError.class);
				if("ER-0002".equals(escError.getCode())) {
					log.info("ER-0002 : {}, Try to put (update) to Esc Card : {}", escError.getMessage(), escCard);
					url += "/" + escCard.getCardNumber();
					ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
					log.info(card.getCsn() + " sent/put in Esc as Card -> " + response.getBody());
				} else {
					throw clientEx;
				}
			} catch (IOException e) {
				log.trace("Error parsing response body as EscError - " + clientEx.getResponseBodyAsString(), e);
				throw clientEx;
			}
		}
		escCardDaoService.persist(escCard);
	}

	protected void deleteEscCard(Card card) {
		List<EscCard> EscCards = escCardDaoService.findEscCardsByCardNumberEquals(card.getEscnUid()).getResultList();
		if(EscCards.isEmpty()) {
			log.warn("No EscCard found for this card " + card.getCsn() + " so we can't desactivate it on Esc");
		} else {
			EscCard escCard = EscCards.get(0);
			String europeanCardIdentifier = escCard.getCardNumber();
			String url = webUrl + "/cards/" + europeanCardIdentifier;
			HttpHeaders headers = this.getJsonHeaders();			
			HttpEntity entity = new HttpEntity(null, headers);
			log.debug("Try to delete card : " + escCard);
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
				log.info(card.getCsn() + " deleted in Esc -> " + response.getBody());
			} catch(HttpClientErrorException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.warn("No need to delete " + card.getCsn() + " in Esc because not found in Esc");
				} else {
					throw clientEx;
				}
			}
			escCardDaoService.remove(escCard);
		}
	}

	protected EscCard computeEscCard(Card card) {
		EscCard escCard = new EscCard();
		escCard.setCardNumber(card.getEscnUid());
		escCard.setCardStatusType(Card.Etat.ENABLED.equals(card.getEtat()) ? org.esupportail.sgc.domain.EscCard.CardStatusType.ACTIVE : org.esupportail.sgc.domain.EscCard.CardStatusType.INACTIVE);
		escCard.setExpiresAt(card.getDueDate());
		escCard.setIssuedAt(card.getEncodedDate());
		Long pic = picInstitutionCode;
		if(!StringUtils.isEmpty(card.getUser().getPic())) {
			pic = Long.valueOf(card.getUser().getPic());
		}

		escCard.setIssuerIdentifier(pic.toString());
		if(StringUtils.isNotBlank(vatProcessorInstitution)) {
			escCard.setProcessorIdentifier(vatProcessorInstitution);
		}
		escCard.setPersonIdentifier(getEuropeanPersonIdentifier(card.getEppn()));
		escCard.setCardType(getCardType(card));
		return escCard;
	}

	protected EscCard.CardType getCardType(Card card) {
		String type = cardType;
		TreeSet<LocalDateTime> dates = new TreeSet<LocalDateTime>(cardTypes.keySet());
		for(LocalDateTime date : dates) {
			if(card.getEncodedDate().isAfter(date)) {
				type = cardTypes.get(date);
			}
		}
		return EscCard.CardType.valueOf(type);
	}

	private HttpHeaders getJsonHeaders() {	
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + key);
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("User-Agent", appliConfigService.getEsupSgcAsHttpUserAgent());
		return headers;
	}

	public String getEuropeanPersonIdentifier(String eppn) {
		String esi = null;
		if(eppn.matches(this.getEppnFilter())) {
			List<EscPerson> escPersonsInESCR = escPersonDaoService.findEscPersonsByEppnEquals(eppn).getResultList();
			if (!escPersonsInESCR.isEmpty()) {
				// ESI can't be modified in ESCR
				// Si/quand l'API permettra de modifier le ESI, il suffira de supprimer ce bloc
				EscPerson escPersonInESCR = escPersonsInESCR.get(0);
				esi = escPersonInESCR.getIdentifier();
			} else {
				User user = userDaoService.findUser(eppn);
				String supannCodeINE = user.getSupannCodeINE();
				if (supannCodeINE == null || supannCodeINE.isEmpty()) {
					log.info(eppn + " has no or empty supannCodeINE and this attribute is required for the European Person Card !");
					return null;
				}
				esi = String.format("urn:schac:personalUniqueCode:int:esi:%s:%s", countryCode.toLowerCase(), supannCodeINE);
			}
		}
		return esi;
	}

	@Transactional
	public boolean validateESCenableCard(String eppn) {
		User user = userDaoService.findUser(eppn);
		Card enabledCard = user.getEnabledCard();			
		if(enabledCard != null && enabledCard.getEscnUid() != null && !enabledCard.getEscnUid().isEmpty()) {
			if(escPersonDaoService.findEscPersonsByEppnEquals(user.getEppn()).getResultList().isEmpty() || escCardDaoService.findEscCardsByCardNumberEquals(enabledCard.getEscnUid()).getResultList().isEmpty()) {
				this.validate(enabledCard);
				log.info(String.format("Validation on API Esc for %s with card %s OK", user.getEppn(), enabledCard.getCsn()));
				return true;
			}
		}
		return false;
	}

	public String getCaChainCertAsHexa(String picInstitutionCode) {
		String urlTemplate = webUrl.replaceFirst("/v1", "/v1/certs/files/%s/ca-chain.cert.pem");
		String url = String.format(urlTemplate, picInstitutionCode);
		HttpHeaders headers = this.getJsonHeaders();			
		HttpEntity entity = new HttpEntity(headers);
		log.info("Try to get CA Chain Cert on " + url); 
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		log.info("CA Chain Cert for " + picInstitutionCode + " -> \n" + response.getBody());
		byte[] certAsBytesArray = response.getBody().getBytes();
		return Hex.toHexString(certAsBytesArray);
	}

	public boolean isEscEnabled() {
		return enable;
	}

	public boolean isEppnMatches(String eppn) {
		return eppn.matches(this.getEppnFilter());
	}
}

