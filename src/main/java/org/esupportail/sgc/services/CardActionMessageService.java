package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.springframework.stereotype.Service;

@Service
public class CardActionMessageService {

	
	public void persist(CardActionMessage cardActionMessage) {
		cardActionMessage.persist();
	}

	
	public void merge(CardActionMessage cardActionMessage) {
		cardActionMessage.merge();
	}

	
	public void remove(CardActionMessage cardActionMessage) {
		cardActionMessage.remove();
	}

	
	public void updateCardActionMessages(Card card) {
		Map<Etat, List<CardActionMessage>> messages = new HashMap<Etat, List<CardActionMessage>>();
		for(Etat etatFinal : card.getEtatsAvailable()) {
			if(card.getUser().getUserType() != null) {
				messages.put(etatFinal, CardActionMessage.findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(null, card.getEtat(), etatFinal, card.getUser().getUserType(), true));
			}
		}
		card.setCardActionMessages(messages);
	}
	
	
	public Map<Etat, List<CardActionMessage>> getCardActionMessagesForCards(List<Long> cardIds) {
		List<Card> cards = Card.findAllCards(cardIds);
		updateCardActionMessages(cards.get(0));
		Map<Etat, List<CardActionMessage>> messages = cards.get(0).getCardActionMessages();
		for(Card card : cards) {
			updateCardActionMessages(card);
			for(Etat etatFinal : messages.keySet()) {
				if(!card.getCardActionMessages().containsKey(etatFinal)) {
					messages.put(etatFinal, new ArrayList<CardActionMessage>());
				} else {
					messages.get(etatFinal).retainAll(card.getCardActionMessages().get(etatFinal));
				}
			}
		}
		return messages;
	}
	
	public Map<String, Set<CardActionMessage>> getCardActionMessagesAutoInConflict() {
		List<String> allUserTypes = User.findDistinctUserType();
		List<CardActionMessage> autoCardActionsMessages = CardActionMessage.findAllCardActionMessagesAutoWithMailToEmptyOrNull();
		Map<String, Set<CardActionMessage>> cardActionsMessagesAutoInConflict = new HashMap<String, Set<CardActionMessage>>();
		for(CardActionMessage cardActionMessage : autoCardActionsMessages) {
			List<Etat> etatsInitiaux = new ArrayList<Etat>();
			if(cardActionMessage.getEtatInitial()!=null) {
				etatsInitiaux.add(cardActionMessage.getEtatInitial());
			} else {
				etatsInitiaux = Arrays.asList(Etat.values());
			}
			for(Etat etatInitial : etatsInitiaux) {
				List<String> userTypes = new ArrayList<String>();
				if(cardActionMessage.getUserTypes()==null || cardActionMessage.getUserTypes().isEmpty()) {
					userTypes = allUserTypes;
				} else {
					userTypes = new ArrayList<String>(cardActionMessage.getUserTypes());
				}
				for(String userType: userTypes) {
					String idMap = String.format("%s/%s/%s", etatInitial, cardActionMessage.getEtatFinal(), userType);
					if(cardActionsMessagesAutoInConflict.get(idMap) == null) {
						cardActionsMessagesAutoInConflict.put(idMap, new HashSet<CardActionMessage>());
					}
					cardActionsMessagesAutoInConflict.get(idMap).add(cardActionMessage);
				}
			}
		}
        List<String> allIdMaps = new ArrayList<String>(cardActionsMessagesAutoInConflict.keySet());
        for(String idMap : allIdMaps) {
        	if(cardActionsMessagesAutoInConflict.get(idMap).size()<2) {
        		cardActionsMessagesAutoInConflict.remove(idMap);
        	}
        }
        return cardActionsMessagesAutoInConflict;
	}

	public Map<String, Set<CardActionMessage>> getCardActionMessagesUnreachable() {
		Map<String, Set<CardActionMessage>> cardActionsMessagesUnreachable = new HashMap<String, Set<CardActionMessage>>();
		for(CardActionMessage cardActionMessage : CardActionMessage.findAllCardActionMessages()) {
			if(CardEtatService.workflow4messages.get(cardActionMessage.getEtatInitial())!=null && !CardEtatService.workflow4messages.get(cardActionMessage.getEtatInitial()).contains(cardActionMessage.getEtatFinal())) {
				String idMap = String.format("%s/%s", cardActionMessage.getEtatInitial(), cardActionMessage.getEtatFinal());
				if(cardActionsMessagesUnreachable.get(idMap) == null) {
					cardActionsMessagesUnreachable.put(idMap, new HashSet<CardActionMessage>());
				}
				cardActionsMessagesUnreachable.get(idMap).add(cardActionMessage);
			}
		}
		return cardActionsMessagesUnreachable;
	}

}
