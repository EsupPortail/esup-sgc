package org.esupportail.sgc.services.sync;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import antlr.debug.TraceAdapter;

@Service
public class ResynchronisationService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	private ResynchronisationUserService resynchronisationUserService;
	
	private Boolean shutdownCalled = false; 
	
	@PreDestroy
	public void shouldShutdownNow() throws InterruptedException {
		log.warn("shutdown called during synchroization of all users ...");
		shutdownCalled = true;
		Thread.currentThread().sleep(1000);
	}
	
	public void synchronizeAllUsersInfos() {
		log.info("Synchronize of all users called");
		StopWatch stopWatch = new PrettyStopWatch();
		stopWatch.start("Synchronize of all users");
		long nbUpdate = 0;
		long nbNoUpdate = 0;
		long nbError = 0;
		for(String eppn : User.findAllUsersEppns()) {
			try {
				if(resynchronisationUserService.synchronizeUserInfo(eppn)) {
					nbUpdate++;
				} else {
					nbNoUpdate++;
				}
			} catch(Exception ex) {
				log.error("Error during synchronize " + eppn, ex);
				 nbError++;
			}
			if(shutdownCalled) {
				log.warn("shutdown called during synchroization of all users - we stop it");
				break;
			}
			try {
				// on temporise un peu ... 
				Thread.currentThread().sleep(2);
			} catch (InterruptedException e) {
				//
			}
		}
		stopWatch.stop();
		log.debug(stopWatch.prettyPrint());
		log.info("Sync users ok - {} - nb updated (needed) : {} - nb no updated (no need) : {} - errors : {}", stopWatch.shortSummary(), nbUpdate, nbNoUpdate, nbError);
	}

}
