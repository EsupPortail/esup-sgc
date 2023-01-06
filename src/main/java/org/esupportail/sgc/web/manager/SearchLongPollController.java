package org.esupportail.sgc.web.manager;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;


@RequestMapping("/manager/searchPoll")
@Controller
public class SearchLongPollController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// Map avec en clef l'eppn de l'utilisateur manager potentiel badgeur -> pas plus d'un searchPoll par utilisateur.
	private Map<String, DeferredResult<String>> suspendedSearchPollRequests = new ConcurrentHashMap<String, DeferredResult<String>>();
	
	@RequestMapping
	@ResponseBody
	public DeferredResult<String> searchPoll(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		final String authName = auth.getName();

		final DeferredResult<String> searchEppn = new DeferredResult<String>(null, "");
		
		if(this.suspendedSearchPollRequests.containsKey(authName)) {
			this.suspendedSearchPollRequests.get(authName).setResult("stop");
		}
		this.suspendedSearchPollRequests.put(authName, searchEppn);
		
		searchEppn.onCompletion(new Runnable() {
			public void run() {
				synchronized (searchEppn) {
					if(searchEppn.equals(suspendedSearchPollRequests.get(authName))) {
						suspendedSearchPollRequests.remove(authName);
					}
				}
			}
		});
		
		// log.info("this.suspendedSearchPollRequests.size : " + this.suspendedSearchPollRequests.size());

		return searchEppn;
	}


	public void handleCard(String eppnInit, Long cardId) {
		log.debug("handleCard : " + cardId + " for " + eppnInit);
		if(this.suspendedSearchPollRequests.containsKey(eppnInit)) {
			String result = "manager/" + cardId;
			this.suspendedSearchPollRequests.get(eppnInit).setResult(result);
		}
	}
}


