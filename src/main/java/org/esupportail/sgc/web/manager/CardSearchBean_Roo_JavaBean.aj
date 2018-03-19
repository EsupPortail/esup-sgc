// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.web.manager;

import java.util.HashMap;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.FlagAdresse;
import org.esupportail.sgc.web.manager.CardSearchBean;

privileged aspect CardSearchBean_Roo_JavaBean {
    
    public String CardSearchBean.getSearchText() {
        return this.searchText;
    }
    
    public void CardSearchBean.setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public Boolean CardSearchBean.getOwnOrFreeCard() {
        return this.ownOrFreeCard;
    }
    
    public void CardSearchBean.setOwnOrFreeCard(Boolean ownOrFreeCard) {
        this.ownOrFreeCard = ownOrFreeCard;
    }
    
    public Etat CardSearchBean.getEtat() {
        return this.etat;
    }
    
    public void CardSearchBean.setEtat(Etat etat) {
        this.etat = etat;
    }
    
    public FlagAdresse CardSearchBean.getFlagAdresse() {
        return this.flagAdresse;
    }
    
    public void CardSearchBean.setFlagAdresse(FlagAdresse flagAdresse) {
        this.flagAdresse = flagAdresse;
    }
    
    public String CardSearchBean.getType() {
        return this.type;
    }
    
    public void CardSearchBean.setType(String type) {
        this.type = type;
    }
    
    public String CardSearchBean.getEditable() {
        return this.editable;
    }
    
    public void CardSearchBean.setEditable(String editable) {
        this.editable = editable;
    }
    
    public String CardSearchBean.getAddress() {
        return this.address;
    }
    
    public void CardSearchBean.setAddress(String address) {
        this.address = address;
    }
    
    public Long CardSearchBean.getNbCards() {
        return this.nbCards;
    }
    
    public void CardSearchBean.setNbCards(Long nbCards) {
        this.nbCards = nbCards;
    }
    
    public Long CardSearchBean.getNbRejets() {
        return this.nbRejets;
    }
    
    public void CardSearchBean.setNbRejets(Long nbRejets) {
        this.nbRejets = nbRejets;
    }
    
    public HashMap<String, String> CardSearchBean.getFreeField() {
        return this.freeField;
    }
    
    public void CardSearchBean.setFreeField(HashMap<String, String> freeField) {
        this.freeField = freeField;
    }
    
    public HashMap<String, String> CardSearchBean.getFreeFieldValue() {
        return this.freeFieldValue;
    }
    
    public void CardSearchBean.setFreeFieldValue(HashMap<String, String> freeFieldValue) {
        this.freeFieldValue = freeFieldValue;
    }
    
}
