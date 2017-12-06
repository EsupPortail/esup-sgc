package org.esupportail.sgc.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

public class EsupNfcTagService {

	RestTemplate restTemplate;

	String webUrl;

	String applicationName;

	String location;

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getWebUrl() {
		return webUrl;
	}

	@Transactional
	public String getEsupNfcTagNumeroId(String eppnInit) {

		EsupNfcSgcJwsDevice device = null;

		List<EsupNfcSgcJwsDevice> devices = EsupNfcSgcJwsDevice.findEsupNfcSgcJwsDevicesByEppnInitEquals(eppnInit).getResultList();
		if(devices.isEmpty()) {
			String numeroId = registerNewDeviceOnEsupNfcTagServer(eppnInit);
			device = new EsupNfcSgcJwsDevice();
			device.setEppnInit(eppnInit);
			device.setNumeroId(numeroId);
			device.persist();
		} else {
			device = devices.get(0);
			if(controlDevice(device.getNumeroId())==null || !controlDevice(device.getNumeroId()).equals(eppnInit)){
				String numeroId = registerNewDeviceOnEsupNfcTagServer(eppnInit);
				device.setNumeroId(numeroId);
				device.merge();
			}
		}
		return device.getNumeroId();
	}

	private String registerNewDeviceOnEsupNfcTagServer(String eppnInit) {
		String registerUrl = webUrl + "/wsrest/register";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("User-Agent", "esup-sgc-jws");	
		Map<String, String> params = new HashMap<String, String>();
		params.put("eppnInit", eppnInit);
		params.put("applicationName", applicationName);
		params.put("location", location);	
		params.put("validateAuthWoConfirmation", "True");
		HttpEntity entity = new HttpEntity(params, headers);
		HttpEntity<String> response = restTemplate.exchange(registerUrl, HttpMethod.POST, entity, String.class);
		return response.getBody();
	}

	public String controlDevice(String deviceId) {
		String controlUrl = webUrl + "/wsrest/deviceControl?numeroId="+deviceId;
		String eppnInit = restTemplate.getForObject(controlUrl, String.class);
		return eppnInit;
	}
	
}
