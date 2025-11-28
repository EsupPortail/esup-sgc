package org.esupportail.sgc.services;

import jakarta.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class RestValidateService extends ValidateService {
	
	private String CSN = "%csn%";
	
	private String EPPN = "%eppn%";
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	String validateRestUrl = null;
	
	String invalidateRestUrl = null;
	
	RestTemplate restTemplate;
	
	@Resource
	AppliConfigService appliConfigService;
	
	public void setValidateRestUrl(String validateRestUrl) {
		this.validateRestUrl = validateRestUrl;
	}

	public void setInvalidateRestUrl(String invalidateRestUrl) {
		this.invalidateRestUrl = invalidateRestUrl;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public void validateInternal(Card card) {
		callGetOnUrl(validateRestUrl, card);
	}

	@Override
	public void invalidateInternal(Card card) {
		callGetOnUrl(invalidateRestUrl, card);
	}

	protected void callGetOnUrl(String restUrl, Card card) {
		if(restUrl == null || restUrl.isEmpty()) {
			log.warn("RestValidateService [" + this.getBeanName() + "] configured with no restUrl ?!");
		} else {
			String url = String.format(restUrl, card.getEppn(), card.getCsn());
			log.debug("Try to send a get here : " + url); 
			HttpHeaders headers = new HttpHeaders();
			headers.set("User-Agent", appliConfigService.getEsupSgcAsHttpUserAgent());
			HttpEntity entity = new HttpEntity(null, headers);	
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
				log.info("Got from " + url + " : " + response.getBody());
			} catch(HttpStatusCodeException clientEx) {
				if(HttpStatus.NOT_FOUND.equals(clientEx.getStatusCode())) {
					log.warn(String.format("Response NOT_FOUND on GET %s ", url));
				} else {
					throw clientEx;
				}
			}	
		}
	}
}


