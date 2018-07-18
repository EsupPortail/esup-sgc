package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestValidateService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	String validateRestUrl = null;
	
	String invalidateRestUrl = null;
	
	RestTemplate restTemplate;
	
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
	public void validate(Card card) {
		callGetOnUrl(validateRestUrl, card);
	}

	@Override
	public void invalidate(Card card) {
		callGetOnUrl(invalidateRestUrl, card);
	}

	protected void callGetOnUrl(String restUrl, Card card) {
		if(restUrl == null || restUrl.isEmpty()) {
			log.warn("RestValidateService [" + this.getBeanName() + "] configured with no restUrl ?!");
		} else {
			String url = String.format(restUrl, card.getEppn(), card.getCsn()); 
			log.debug("Try to send a get here : " + url); 
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
			log.info("Got from " + url + " : " + response.getBody());
		}
	}
}


