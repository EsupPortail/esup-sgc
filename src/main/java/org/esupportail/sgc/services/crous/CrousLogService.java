package org.esupportail.sgc.services.crous;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.crous.CrousErrorLog.CrousOperation;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
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
	public void logErrorCrousAsync(CrousHttpClientErrorException crousHttpClientErrorException) {
		crousHttpClientErrorException.setBlocking(true);
		logErrorCrous(crousHttpClientErrorException);
	}

	public void logErrorCrous(CrousHttpClientErrorException crousHttpClientErrorException) {	
		try {
			
			log.info("Try to Log Error Crous in Database : " + crousHttpClientErrorException);
			
			Card card = null;
			User user = null;
			
			String csn = crousHttpClientErrorException.getCsn();
			String eppn = crousHttpClientErrorException.getEppn();
					
			Map<String, List<CrousErrorLog>> errorsMap = crousErrorLogMapper.readValue(crousHttpClientErrorException.getErrorBodyAsJson(), typeCrousErrorLogRef);
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
				crousErrorLog.setBlocking(crousHttpClientErrorException.getBlocking());
				crousErrorLog.setCrousOperation(crousHttpClientErrorException.getCrousOperation());
				crousErrorLog.setEsupSgcOperation(crousHttpClientErrorException.getEsupSgcOperation());
				crousErrorLog.setCrousUrl(crousHttpClientErrorException.getCrousUrl());
				if(errorLogsInDb.isEmpty()) {				
					crousErrorLog.persist();
				}
			} else {
				log.error("Failed to Log Error Crous in Database : " + crousHttpClientErrorException);
			}
		} catch (IOException e) {
			log.error("Error during persist crous Error log : " + crousHttpClientErrorException, e);
		} 
	}


}

