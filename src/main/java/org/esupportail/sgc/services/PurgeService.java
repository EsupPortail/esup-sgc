package org.esupportail.sgc.services;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurgeService {
	
	@Resource
	LogService logService;
	
	@Transactional
	public void purge(Card card) {
		if(CrousErrorLog.countFindCrousErrorLogsByCard(card)>0) {
			for(CrousErrorLog crousErrorLog : CrousErrorLog.findCrousErrorLogsByCard(card).getResultList()) {
				crousErrorLog.remove();
			}
		}
		logService.log(card.getId(), ACTION.PURGE_CARD, RETCODE.SUCCESS, "", card.getEppn(), null);
		card.remove();
	}
	
	@Transactional
	public void purge(User user) {
		if(CrousErrorLog.countFindCrousErrorLogsByUserAccount(user)>0) {
			for(CrousErrorLog crousErrorLog : CrousErrorLog.findCrousErrorLogsByUserAccount(user).getResultList()) {
				crousErrorLog.remove();
			}
		}
		logService.log(null, ACTION.PURGE_USER, RETCODE.SUCCESS, "", user.getEppn(), null);
		user.remove();
	}

}
