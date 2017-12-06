package org.esupportail.sgc.services;

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
		return Card.countFindCardsByEppnEquals(eppn) == 0;
	}

    public boolean isFreeRenewal(String eppn){
    	boolean isFreeRenewal = false;

		User user = User.findUsersByEppnEquals(eppn).getSingleResult();
		if (!user.getCards().isEmpty()){
			for(Card card : user.getCards()){
				if(Etat.DISABLED.equals(card.getEtat()) || Etat.ENABLED.equals(card.getEtat())){
					if(user.isRequestFree()) {
						isFreeRenewal = true; 
						break;
					}
				}
			}
		}
		
		return !isOutOfDueDate(eppn) && isFreeRenewal && !hasRequestCard(eppn) && !user.hasExternalCard();
    }

	public boolean isPaidRenewal(String eppn){
		boolean isPaidRenewal = false;
		String reference = cardService.getPaymentWithoutCard(eppn);
		if(!reference.isEmpty()){
			isPaidRenewal = true;
		}
		return isPaidRenewal;
	}
	
	public boolean isNotFreeRenewal(String eppn){
		boolean isNotFreeRenewal = false;
		
		User user = User.findUsersByEppnEquals(eppn).getSingleResult();
		boolean etatOk = true;
		for(Card card : user.getCards()){
			if(!Etat.DISABLED.equals(card.getEtat()) && !Etat.ENABLED.equals(card.getEtat())){
				etatOk = false; break;
			}
		}
		if(!isFreeRenewal(eppn) && !isFreeRenewal(eppn) && etatOk){
			isNotFreeRenewal = true;
		}
		
		return isNotFreeRenewal;
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
			displayForm = isEsupSgcUser(eppn);
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
		return !isOutOfDueDate(eppn) && isNotFreeRenewal(eppn) && !isPaidRenewal(eppn);
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
				if(card.getDeliveredDate() == null){
					hasDeliveredCard = false; 
					break;
				}
			}
		}
		return hasDeliveredCard;
	}
}
