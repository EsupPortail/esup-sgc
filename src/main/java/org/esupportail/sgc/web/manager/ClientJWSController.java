package org.esupportail.sgc.web.manager;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.EsupNfcTagService;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/manager/clientjws")
@Controller
public class ClientJWSController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private SecureRandom random = new SecureRandom();
	
	private Map<String,String> authTokens = new HashMap<String, String>();
	
	@Resource
	EsupNfcTagService esupNfcTagService;

	@Resource
	CardIdsService cardIdsService;
	
	@RequestMapping
	public String getJnlp(HttpServletRequest request, HttpServletResponse response, Model uiModel) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = auth.getName();
		
		String authToken = generateAuthToken(eppnInit);
		
		String esupNfcTagUrl = esupNfcTagService.getWebUrl();
		String sgcUrl = null;
		try {
			// sgcUrl = request.getScheme() + "://"+ new URL(request.getRequestURL().toString()).getHost();
			sgcUrl = "https://"+ new URL(request.getRequestURL().toString()).getHost();
		} catch (MalformedURLException e) {
			throw new SgcRuntimeException("Error retrieving the host (sgc) url", e);
		}

		
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		response.setContentType("application/x-java-jnlp-file");
		response.setHeader("Content-disposition", "attachment; filename=esup-sgc-client.jnlp");
		
		uiModel.addAttribute("authToken", authToken);
		uiModel.addAttribute("esupNfcTagUrl", esupNfcTagUrl);
		uiModel.addAttribute("sgcUrl", sgcUrl);
		uiModel.addAttribute("cnousEncode", cardIdsService.isCrousEncodeEnabled());
		
		return "manager/esup-sgc-jnlp";
		
	}

	@RequestMapping("/r2d2")
	public String getJnlp2(HttpServletRequest request, HttpServletResponse response, Model uiModel) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppnInit = auth.getName();
		
		String authToken = generateAuthToken(eppnInit);
		
		String esupNfcTagUrl = esupNfcTagService.getWebUrl();
		String sgcUrl = null;
		try {
			// sgcUrl = request.getScheme() + "://"+ new URL(request.getRequestURL().toString()).getHost();
			sgcUrl = "https://"+ new URL(request.getRequestURL().toString()).getHost();
		} catch (MalformedURLException e) {
			throw new SgcRuntimeException("Error retrieving the host (sgc) url", e);
		}

		
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		response.setContentType("application/x-java-jnlp-file");
		response.setHeader("Content-disposition", "attachment; filename=esup-sgc-client-r2d2.jnlp");
		
		uiModel.addAttribute("authToken", authToken);
		uiModel.addAttribute("esupNfcTagUrl", esupNfcTagUrl);
		uiModel.addAttribute("sgcUrl", sgcUrl);
		uiModel.addAttribute("cnousEncode", cardIdsService.isCrousEncodeEnabled());
		
		return "manager/esup-sgc-jnlp-r2d2";
		
	}
	
	private String generateAuthToken(String eppnInit) {
		String authToken = new BigInteger(130, random).toString(32);
		// JWS Application is not multi-session for one user  
		// -> only one authToken by user !!
		synchronized (authTokens) {
			if(authTokens.containsValue(eppnInit)) {
				for(String key : new ArrayList<String>(authTokens.keySet())) {
					if(authTokens.get(key).equals(eppnInit)) {
						authTokens.remove(key);
					}
				}
			}
			authTokens.put(authToken, eppnInit);
		}
		return authToken;
	}

	public String getEppnInit(String authToken) {
		return authTokens.get(authToken);
	}

}
