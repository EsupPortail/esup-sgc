package org.esupportail.sgc.services.crous;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CrousLogService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final TypeReference<Map<String, List<CrousErrorLog>>> typeCrousErrorLogRef = new TypeReference<Map<String, List<CrousErrorLog>>>(){};
	
	private final ObjectMapper crousErrorLogMapper = new ObjectMapper();
	
	/*
	 * errorBodyAsJson example : 
	 * {"errors":[{"code":"-11","message":"Client existant","field":null}]}
	 */
	// @Transactional(propagation=Propagation.REQUIRES_NEW) -> seams to no work well - too complex
	// -> we use @Async here - that's the same but more easy
	@Async
	@Transactional
	public void logErrorCrousAsync(String eppn, String csn, String errorBodyAsJson) {
		logErrorCrous(eppn, csn, errorBodyAsJson, true);
	}
	
	public void logErrorCrous(String eppn, String csn, String errorBodyAsJson) {
		logErrorCrous(eppn, csn, errorBodyAsJson, false);
	}
		
	private void logErrorCrous(String eppn, String csn, String errorBodyAsJson, Boolean blocking) {	
		try {
			
			log.info("Try to Log Error Crous in Database : " + eppn + " - " + csn + " - " + errorBodyAsJson);
			
			Card card = null;
			User user = null;
					
			Map<String, List<CrousErrorLog>> errorsMap = crousErrorLogMapper.readValue(errorBodyAsJson, typeCrousErrorLogRef);
			if(errorsMap != null && errorsMap.get("errors") != null && errorsMap.get("errors").get(0) !=null) { 
				CrousErrorLog crousErrorLog = errorsMap.get("errors").get(0);
				
				List<CrousErrorLog> errorLogsInDb = new ArrayList<CrousErrorLog>(); 
				if(csn != null && !csn.isEmpty()) {
					card = Card.findCardByCsn(csn);
					crousErrorLog.setCard(card);
					user = card.getUserAccount();
					card.setCrousError(crousErrorLog.getMessage());
				} else 	if(eppn != null && !eppn.isEmpty()) {
					user = User.findUser(eppn);
					crousErrorLog.setUserAccount(user);
					user.setCrousError(crousErrorLog.getMessage());
				} else {
					log.warn("No CSN and no EPPN ??!");
				}
				if(user != null) {
					crousErrorLog.setUserAccount(user);
					errorLogsInDb = CrousErrorLog.findCrousErrorLogsByUserAccount(user).getResultList();
				}
				if(card != null) {
					crousErrorLog.setUserAccount(user);
					errorLogsInDb = CrousErrorLog.findCrousErrorLogsByCard(card).getResultList();
				}
				if(!errorLogsInDb.isEmpty()) {
					// on ne garde qu'une erreur par carte / utilisateur - erreur qu'on met à jour
					CrousErrorLog crousErrorLogOld = errorLogsInDb.get(0);
					crousErrorLogOld.setCard(crousErrorLog.getCard());
					crousErrorLogOld.setUserAccount(crousErrorLog.getUserAccount());
					crousErrorLogOld.setCode(crousErrorLog.getCode());
					crousErrorLogOld.setMessage(crousErrorLog.getMessage());
					crousErrorLogOld.setField(crousErrorLog.getField());
					crousErrorLog = crousErrorLogOld;
				} 
				crousErrorLog.setDate(new Date());	
				crousErrorLog.setBlocking(blocking);
				if(errorLogsInDb.isEmpty()) {				
					crousErrorLog.persist();
				}
			} else {
				log.error("Failed to Log Error Crous in Database : " + eppn + " - " + csn + " - " + errorBodyAsJson);
			}
		} catch (IOException e) {
			log.error("Error during persist crous Error log : " + errorBodyAsJson + " on " + eppn + " / " + csn, e);
		} 
	}


}

