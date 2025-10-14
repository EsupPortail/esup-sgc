package org.esupportail.sgc.web.manager;

import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.FlagAdresse;
import org.esupportail.sgc.domain.TemplateCard;

public class CardSearchBean {

	String searchText = "";

	Boolean ownOrFreeCard;

	Etat etat;
	
	FlagAdresse flagAdresse;

	String type = "All";
	
	String editable;
	
	String address = "";
	
	private Long nbCards;
	
	private Long nbRejets;
	
	private TemplateCard lastTemplateCardPrinted;
	
	HashMap<Integer, String> freeField;
	
	SortedMap<Integer, List<String>> freeFieldValue;
	
	String hasRequestCard;
	

	public String getSearchText() {
        return this.searchText;
    }

	public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

	public Boolean getOwnOrFreeCard() {
        return this.ownOrFreeCard;
    }

	public void setOwnOrFreeCard(Boolean ownOrFreeCard) {
        this.ownOrFreeCard = ownOrFreeCard;
    }

	public Etat getEtat() {
        return this.etat;
    }

	public void setEtat(Etat etat) {
        this.etat = etat;
    }

	public FlagAdresse getFlagAdresse() {
        return this.flagAdresse;
    }

	public void setFlagAdresse(FlagAdresse flagAdresse) {
        this.flagAdresse = flagAdresse;
    }

	public String getType() {
        return this.type;
    }

	public void setType(String type) {
        this.type = type;
    }

	public String getEditable() {
        return this.editable;
    }

	public void setEditable(String editable) {
        this.editable = editable;
    }

	public String getAddress() {
        return this.address;
    }

	public void setAddress(String address) {
        this.address = address;
    }

	public Long getNbCards() {
        return this.nbCards;
    }

	public void setNbCards(Long nbCards) {
        this.nbCards = nbCards;
    }

	public Long getNbRejets() {
        return this.nbRejets;
    }

	public void setNbRejets(Long nbRejets) {
        this.nbRejets = nbRejets;
    }

	public TemplateCard getLastTemplateCardPrinted() {
        return this.lastTemplateCardPrinted;
    }

	public void setLastTemplateCardPrinted(TemplateCard lastTemplateCardPrinted) {
        this.lastTemplateCardPrinted = lastTemplateCardPrinted;
    }

	public HashMap<Integer, String> getFreeField() {
        return this.freeField;
    }

	public void setFreeField(HashMap<Integer, String> freeField) {
        this.freeField = freeField;
    }

	public SortedMap<Integer, List<String>> getFreeFieldValue() {
        return this.freeFieldValue;
    }

	public void setFreeFieldValue(SortedMap<Integer, List<String>> freeFieldValue) {
        this.freeFieldValue = freeFieldValue;
    }

	public String getHasRequestCard() {
        return this.hasRequestCard;
    }

	public void setHasRequestCard(String hasRequestCard) {
        this.hasRequestCard = hasRequestCard;
    }
}
