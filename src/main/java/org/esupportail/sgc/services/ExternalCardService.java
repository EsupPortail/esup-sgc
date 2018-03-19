package org.esupportail.sgc.services;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.stereotype.Service;

@Service
public class ExternalCardService {
	
	@Resource
	private UserInfoService userInfoService;
	
    @Resource
    ResynchronisationUserService resynchronisationUserService;

	public Card getExternalCard(String eppn, HttpServletRequest request) {
		User dummyUser = new User();
		dummyUser.setEppn(eppn);
		userInfoService.setAdditionalsInfo(dummyUser, null);
		Card externalCard = dummyUser.getExternalCard();
		if(externalCard.getCsn() == null || externalCard.getCsn().isEmpty()) {
			return null;
		} else {
			userInfoService.setPrintedInfo(externalCard);
			return externalCard;
		}
	}

	public Card importExternalCard(String eppn, HttpServletRequest request) {
		User user = User.findUser(eppn);
		Card externalCard = null;
		for(Card card : user.getCards()) {
			if(card.getExternal()) {
				externalCard = card;
				break;
			}
		}
		if(externalCard == null) {
			externalCard = new Card();
			externalCard.setUserAccount(user);
			user.getCards().add(externalCard);
			externalCard.setEppn(user.getEppn());
            externalCard.setEtat(Etat.DISABLED);
            externalCard.setDateEtat(new Date());
            externalCard.setDeliveredDate(new Date());
            externalCard.setExternal(true);
            userInfoService.setPrintedInfo(externalCard);
            user.setCrous(false);
            user.setDifPhoto(false);
            externalCard.persist();
		}
		resynchronisationUserService.synchronizeUserInfo(eppn);
		return externalCard;
	}

}
