package org.esupportail.sgc.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

@Service
public class LogService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static enum RETCODE {
		FAILED, SUCCESS
	}
	
	public static enum TYPE {
		ADMIN, MANAGER, SUPERVISOR, SYSTEM, USER
	}
	
	public static enum ACTION {
		ETAT, DEMANDE, DISABLED, ENABLED, PAYMENT, UPDATEPHOTO, DIFPHOTO, FORCEDUPDATE, MAJVERSO, FORCEDFREEREQUEST, 
		CROUS_DESACTIVATION, CROUS_PATCH_IDENTIFIER, USER_DELIVERY, UPDATEPREFS, ENABLECROUS, ENABLEEUROPEANCARD
		
	}

	@Resource
	SupervisorService supervisorService;
	
	public void log(Long cardId, ACTION action, RETCODE success, String comment, String eppnCible, String ip) {

		TYPE type = TYPE.SYSTEM;
		String eppn = "system";
	
		String remoteAddress = "";
		if(ip !=null){
			remoteAddress = ip;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null) {
			eppn = auth.getName();
			type = TYPE.USER;
			
			// Switch User Par un Supervisor ?
			String supervisorUsername = supervisorService.findCurrentSupervisorUsername();
			if(supervisorUsername != null) {
				eppn = supervisorUsername + "@" + auth.getName();
				type = TYPE.SUPERVISOR;
			}
			
			// IP ?
			if(ip == null){
			Object detail = auth.getDetails();
				if(detail instanceof WebAuthenticationDetails) {
					WebAuthenticationDetails webAuth = (WebAuthenticationDetails) detail;
					remoteAddress = webAuth.getRemoteAddress();
				}
			}
		} 

		Date logDate = new Date();

		Log log = new Log();
		log.setLogDate(logDate);
		log.setEppn(eppn);
		log.setType(type.name());
		log.setCardId(cardId);
		log.setAction(action.name());
		log.setRetCode(success.name());
		log.setComment(comment);
		log.setEppnCible(eppnCible);
		log.setRemoteAddress(remoteAddress);
		log.persist();
	}
	
	
	// Appelé tous les jours à 10H
	@Scheduled(cron="0 0 10 * * *")
	public void purge() {
		
		AppliConfig daysNbConfig = AppliConfig.findAppliConfigByKey("RETENTION_LOGS_DB_DAYS");
		int daysNb = Integer.parseInt(daysNbConfig.getValue());
				
		Date currentDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.DAY_OF_MONTH, -daysNb);
		Date datePurge = cal.getTime();
		
		List<Log> logs2purge = Log.findLogsByLogDateLessThan(datePurge).getResultList();
		for(Log log: logs2purge) {
			log.remove();
		}
		
		log.info(logs2purge.size() + " logs en base vieux de " + daysNb  + " jours purgés");
		
	}

}

