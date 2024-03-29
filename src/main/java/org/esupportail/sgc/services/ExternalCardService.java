package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@Transactional
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
		if(user == null) {
			user = new User();
			user.setEppn(eppn);
			user.persist();
		}
		Card externalCard = null;
		for(Card card : user.getCards()) {
			if(card.getExternal()) {
				externalCard = card;
				break;
			}
		}
		if(externalCard == null) {
			externalCard = initExternalCard(user);
		}
		resynchronisationUserService.synchronizeUserInfoNoTx(eppn);
		if(externalCard.getCsn() == null || externalCard.getCsn().isEmpty()) {
			throw new SgcRuntimeException("external card for " + eppn + " can't be imported becaus no csn found", null);
		}
		return externalCard;
	}
	
	public Card initExternalCard(User user) {
		Card externalCard = new Card();
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
        return externalCard;
	}

}
