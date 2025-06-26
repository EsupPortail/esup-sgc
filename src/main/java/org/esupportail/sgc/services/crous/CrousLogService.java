package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CrousLogService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final TypeReference<Map<String, List<CrousErrorLog>>> typeCrousErrorLogRef = new TypeReference<Map<String, List<CrousErrorLog>>>(){};
	
	private final ObjectMapper crousErrorLogMapper = new ObjectMapper();

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

    @Resource
    CrousErrorLogDaoService crousErrorLogDaoService;
	
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
					card = cardDaoService.findCardByCsn(csn);
					crousErrorLog.setCard(card);
					user = card.getUserAccount();
					card.setCrousError(crousErrorLog.getMessage());
				} else 	if(eppn != null && !eppn.isEmpty()) {
					user = userDaoService.findUser(eppn);
					crousErrorLog.setUserAccount(user);
					user.setCrousError(crousErrorLog.getMessage());
				} else {
					log.warn("No CSN and no EPPN ??!");
				}
				if(user != null) {
					crousErrorLog.setUserAccount(user);
					errorLogsInDb = crousErrorLogDaoService.findCrousErrorLogsByUserAccount(user).getResultList();
				}
				if(card != null) {
					crousErrorLog.setUserAccount(user);
					errorLogsInDb = crousErrorLogDaoService.findCrousErrorLogsByCard(card).getResultList();
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
				crousErrorLog.setDate(LocalDateTime.now());
				crousErrorLog.setBlocking(crousHttpClientErrorException.getBlocking());
				crousErrorLog.setCrousOperation(crousHttpClientErrorException.getCrousOperation());
				crousErrorLog.setEsupSgcOperation(crousHttpClientErrorException.getEsupSgcOperation());
				crousErrorLog.setCrousUrl(crousHttpClientErrorException.getCrousUrl());
				if(errorLogsInDb.isEmpty()) {
                    crousErrorLogDaoService.persist(crousErrorLog);
				}
			} else {
				log.error("Failed to Log Error Crous in Database : " + crousHttpClientErrorException);
			}
		} catch (IOException e) {
			log.error("Error during persist crous Error log : " + crousHttpClientErrorException, e);
		} 
	}


}

