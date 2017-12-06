package org.esupportail.sgc.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
	
	
	public Map<Etat, List<CardActionMessage>> getCardActionMessages() {
		Map<Etat, List<CardActionMessage>> messages = new HashMap<Etat, List<CardActionMessage>>();
		for(Etat etat : Etat.values()) {
			messages.put(etat, CardActionMessage.findCardActionMessagesByEtatFinal(etat).getResultList());
		}
		return messages;
	}

}
