package org.esupportail.sgc.services.sync;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.CardActionMessageDaoService;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.LogMailDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

@Transactional
@Service
public class CaducMailService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	CardService cardService;

	@Resource
	AppliConfigService appliConfigService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    CardActionMessageDaoService cardActionMessageDaoService;

    @Resource
    LogMailDaoService logMailDaoService;

    @Resource
    UserDaoService userDaoService;

	public void sendMails2PreventCaduc() {
		log.info("sendMails2PreventCaduc called");
		Date now = new Date();
		for(CardActionMessage cardActionMessage : cardActionMessageDaoService.findCardActionMessagesWithDateDelay4PreventCaduc()) {
			LocalDateTime futureCaducLocalDate = LocalDateTime.now().plusDays(cardActionMessage.getDateDelay4PreventCaduc());
			for (User futureCaducUser : userDaoService.findAllUsersWithDueDateBeforeAndDueDateAfterNow(futureCaducLocalDate).getResultList()) {
				if (cardActionMessage.getUserTypes().contains(futureCaducUser.getUserType()) &&
                    cardDaoService.countfindCardsByEppnEqualsAndEtatIn(futureCaducUser.getEppn(), Arrays.asList(new Card.Etat[]{Card.Etat.ENABLED}))>0) {
					// already prevent ?
					LocalDateTime realDueLocalDate = futureCaducUser.getDueDate();
					LocalDateTime realLocalDate2Prevent = realDueLocalDate.minusDays(cardActionMessage.getDateDelay4PreventCaduc());
					Date realDate2PreventMinusOne = Date.from(realLocalDate2Prevent.minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
					Date realDate2PreventPlusOne = Date.from(realLocalDate2Prevent.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
					if (realDate2PreventMinusOne.before(now) && realDate2PreventPlusOne.after(now) &&
                            logMailDaoService.countFindLogMails(cardActionMessage, futureCaducUser.getEppn(), realDate2PreventMinusOne, realDate2PreventPlusOne) == 0) {
						String mailTo = StringUtils.isEmpty(cardActionMessage.getMailTo()) ? futureCaducUser.getEmail() : cardActionMessage.getMailTo();
						cardService.sendMailCard(futureCaducUser, cardActionMessage, appliConfigService.getNoReplyMsg(), mailTo, appliConfigService.getListePpale(),
								appliConfigService.getSubjectAutoCard().concat(" -- ".concat(futureCaducUser.getEppn())), cardActionMessage.getMessage());
					}
				}
			}
		}
	}
}
