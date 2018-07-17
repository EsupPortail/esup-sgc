package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
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
			messages.put(etatFinal, CardActionMessage.findCardActionMessagesByEtatInitialAndEtatFinalAndUserType(card.getEtat(), etatFinal, card.getUser().getUserType()).getResultList());
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

}
