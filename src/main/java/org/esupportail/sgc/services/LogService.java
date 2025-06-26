package org.esupportail.sgc.services;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.LogDaoService;
import org.esupportail.sgc.dao.LogMailDaoService;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.LogMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
		CROUS_DESACTIVATION, CROUS_PATCH_IDENTIFIER, CROUS_UNCLOSE, USER_DELIVERY, UPDATEPREFS, ENABLECROUS, ENABLEEUROPEANCARD, RENEWAL,
		MANAGER_DELIVERY, REQUEST_FROM_LDAP, RETOUCHE_PHOTO, RETOUCHE_ACTION_IND, PURGE_USER, PURGE_CARD, REQUEST_MANAGER, DISABLECROUS, DISABLEEUROPEANCARD
		
	}

	@Resource
	SupervisorService supervisorService;
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    LogDaoService logDaoService;

    @Resource
    LogMailDaoService logMailDaoService;

	public void log(Long cardId, ACTION action, RETCODE success, String comment, String eppnCible, String ip) {
		log(cardId, action, success, comment, eppnCible, ip, null);
	}

	public void log(Long cardId, ACTION action, RETCODE success, String comment, String eppnCible, String ip, String printerEppn) {

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

		if(auth == null && printerEppn != null) {
			eppn = printerEppn;
		}

		LocalDateTime logDate = LocalDateTime.now();

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
        logDaoService.persist(log);
	}
	
	
	// Appelé tous les jours à 10H
	@Scheduled(cron="0 0 10 * * *")
	public void purge() {
		
		int daysNb = appliConfigService.getDaysNbConfig();
				
		LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime datePurge = currentDate.minusDays(daysNb);
		
		List<Log> logs2purge = logDaoService.findLogsByLogDateLessThan(datePurge).getResultList();
		for(Log log: logs2purge) {
            logDaoService.remove(log);
		}
		log.info(logs2purge.size() + " logs en base vieux de " + daysNb  + " jours purgés");

		List<LogMail> logmails2purge = logMailDaoService.findLogMailsByLogDateLessThan(datePurge).getResultList();
		for(LogMail log: logmails2purge) {
            logMailDaoService.remove(log);
		}
		log.info(logmails2purge.size() + " logs mails en base vieux de " + daysNb  + " jours purgés");

	}

}

