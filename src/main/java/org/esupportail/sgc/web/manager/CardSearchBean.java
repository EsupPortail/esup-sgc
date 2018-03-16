package org.esupportail.sgc.web.manager;

import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.FlagAdresse;
import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
public class CardSearchBean {

	String searchText = "";

	Boolean ownOrFreeCard;

	Etat etat;
	
	FlagAdresse flagAdresse;

	String type;
	
	String editable;
	
	String address = "";
	
	private Long nbCards;
	
	private Long nbRejets;
	
	String freeField;
	
	String freeFieldValue;

}
