package org.esupportail.sgc.services.sync;

import org.codehaus.plexus.util.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.LogMail;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class CaducMailService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	CardService cardService;

	@Resource
	AppliConfigService appliConfigService;

	public void sendMails2PreventCaduc() {
		log.info("sendMails2PreventCaduc called");
		Date now = new Date();
		for(CardActionMessage cardActionMessage : CardActionMessage.findCardActionMessagesWithDateDelay4PreventCaduc()) {
			LocalDateTime futureCaducLocalDate = LocalDateTime.now().plusDays(cardActionMessage.getDateDelay4PreventCaduc());
			Date futureCaducDate = Date.from(futureCaducLocalDate.atZone(ZoneId.systemDefault()).toInstant());
			for (User futureCaducUser : User.findAllUsersWithDueDateBeforeAndDueDateAfterNow(futureCaducDate).getResultList()) {
				if (cardActionMessage.getUserTypes().contains(futureCaducUser.getUserType()) &&
					Card.countfindCardsByEppnEqualsAndEtatIn(futureCaducUser.getEppn(), Arrays.asList(new Card.Etat[]{Card.Etat.ENABLED}))>0) {
					// already prevent ?
					LocalDateTime realDueLocalDate = futureCaducUser.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					LocalDateTime realLocalDate2Prevent = realDueLocalDate.minusDays(cardActionMessage.getDateDelay4PreventCaduc());
					Date realDate2PreventMinusOne = Date.from(realLocalDate2Prevent.minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
					Date realDate2PreventPlusOne = Date.from(realLocalDate2Prevent.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
					if (realDate2PreventMinusOne.before(now) && realDate2PreventPlusOne.after(now) &&
							LogMail.countFindLogMails(cardActionMessage, futureCaducUser.getEppn(), realDate2PreventMinusOne, realDate2PreventPlusOne) == 0) {
						String mailTo = StringUtils.isEmpty(cardActionMessage.getMailTo()) ? futureCaducUser.getEmail() : cardActionMessage.getMailTo();
						cardService.sendMailCard(futureCaducUser, cardActionMessage, appliConfigService.getNoReplyMsg(), mailTo, appliConfigService.getListePpale(),
								appliConfigService.getSubjectAutoCard().concat(" -- ".concat(futureCaducUser.getEppn())), cardActionMessage.getMessage());
					}
				}
			}
		}
	}
}
