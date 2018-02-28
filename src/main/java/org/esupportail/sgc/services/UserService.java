package org.esupportail.sgc.services;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.springframework.stereotype.Service;


@Service
public class UserService {
	
	@Resource
	CardService cardService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	ExtUserRuleService extUserRuleService;
	
	public boolean isFirstRequest(String eppn){
		return Card.countfindCardsByEppnEqualsAndEtatNotIn(eppn, Arrays.asList(new Etat[] {Etat.CANCELED})) == 0;
	}

    public boolean isFreeRenewal(String eppn){
		User user = User.findUsersByEppnEquals(eppn).getSingleResult();	
		return user.isRequestFree() && !isFirstRequest(eppn) && !isOutOfDueDate(eppn) && !hasRequestCard(eppn) && !user.hasExternalCard();
    }

	public boolean isPaidRenewal(String eppn){
		boolean isPaidRenewal = false;
		String reference = cardService.getPaymentWithoutCard(eppn);
		if(!reference.isEmpty()){
			isPaidRenewal = true;
		}
		return isPaidRenewal;
	}
	
	public boolean displayRenewalForm(String eppn){
		boolean displayRenewalForm = false;
		
		if(isFreeRenewal(eppn) || isPaidRenewal(eppn)){
			displayRenewalForm = true;
		}
		
		User user = User.findUser(eppn);
		if(user != null && user.hasExternalCard()) {
			displayRenewalForm = false;
		}
		
		displayRenewalForm = displayRenewalForm && !hasRequestCard(eppn);		
		return displayRenewalForm;
	}

	public boolean displayForm(String eppn){
		boolean displayForm = displayRenewalForm(eppn);
		if(isFirstRequest(eppn)){
			displayForm = isEsupSgcUser(eppn) && !isOutOfDueDate(eppn);
		} 
		return displayForm;
	}
	
	private boolean hasRequestCard(String eppn){
		return cardEtatService.hasRequestCard(eppn);
	}
	
	
	public boolean isEsupSgcUser(String eppn) {
		User user = User.findUser(eppn);
		return user.getRoles().contains("ROLE_USER") || extUserRuleService.isExtEsupSgcUser(eppn);
	}

	public Boolean canPaidRenewal(String eppn) {
		User user = User.findUser(eppn);
		if(user != null && user.hasExternalCard()) {
			return false;
		}
		return !isOutOfDueDate(eppn) && !isFreeRenewal(eppn) && !isPaidRenewal(eppn);
	}
	
	private boolean isOutOfDueDate(String eppn) {
		User user = User.findUser(eppn);
		return user.getDueDate()==null || user.getDueDate().before(new Date());
	}
	
	public boolean hasDeliveredCard(String eppn){
		boolean hasDeliveredCard = true;
		User user = User.findUsersByEppnEquals(eppn).getSingleResult();
		if (!user.getCards().isEmpty()){
			for(Card card : user.getCards()){
				if(!Etat.CANCELED.equals(card.getEtat()) && card.getDeliveredDate() == null){
					hasDeliveredCard = false; 
					break;
				}
			}
		}
		return hasDeliveredCard;
	}
	
	public boolean isISmartphone(String userAgent){
		boolean isISmartphone = false;
		
		if(userAgent.contains("iPhone") || userAgent.contains("iPad")){
			isISmartphone = true;
		}
		
		return isISmartphone;
	}
}
