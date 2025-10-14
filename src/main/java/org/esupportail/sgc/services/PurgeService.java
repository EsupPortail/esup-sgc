package org.esupportail.sgc.services;

import jakarta.annotation.Resource;

import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.esupportail.sgc.services.crous.CrousErrorLogDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurgeService {
	
	@Resource
	LogService logService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

    @Resource
    CrousErrorLogDaoService crousErrorLogDaoService;

    @Transactional
	public void purge(Card card) {
		if(crousErrorLogDaoService.countFindCrousErrorLogsByCard(card)>0) {
			for(CrousErrorLog crousErrorLog : crousErrorLogDaoService.findCrousErrorLogsByCard(card).getResultList()) {
                crousErrorLogDaoService.remove(crousErrorLog);
			}
		}
		logService.log(card.getId(), ACTION.PURGE_CARD, RETCODE.SUCCESS, "", card.getEppn(), null);
		cardDaoService.remove(card);
	}
	
	@Transactional
	public void purge(User user) {
		if(crousErrorLogDaoService.countFindCrousErrorLogsByUserAccount(user)>0) {
			for(CrousErrorLog crousErrorLog : crousErrorLogDaoService.findCrousErrorLogsByUserAccount(user).getResultList()) {
                crousErrorLogDaoService.remove(crousErrorLog);
			}
		}
		logService.log(null, ACTION.PURGE_USER, RETCODE.SUCCESS, "", user.getEppn(), null);
		userDaoService.remove(user);
	}

}
