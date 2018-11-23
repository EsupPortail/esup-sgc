package org.esupportail.sgc.web.manager;

import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.FlagAdresse;
import org.esupportail.sgc.domain.TemplateCard;
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
	
	private TemplateCard lastTemplateCardPrinted;
	
	HashMap<Integer, String> freeField;
	
	SortedMap<Integer, List<String>> freeFieldValue;
	
	String hasRequestCard;
	
}
