// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import java.util.Set;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;

privileged aspect CardActionMessage_Roo_JavaBean {
    
    public Etat CardActionMessage.getEtatInitial() {
        return this.etatInitial;
    }
    
    public void CardActionMessage.setEtatInitial(Etat etatInitial) {
        this.etatInitial = etatInitial;
    }
    
    public Etat CardActionMessage.getEtatFinal() {
        return this.etatFinal;
    }
    
    public void CardActionMessage.setEtatFinal(Etat etatFinal) {
        this.etatFinal = etatFinal;
    }
    
    public String CardActionMessage.getMessage() {
        return this.message;
    }
    
    public void CardActionMessage.setMessage(String message) {
        this.message = message;
    }
    
    public boolean CardActionMessage.isAuto() {
        return this.auto;
    }
    
    public void CardActionMessage.setAuto(boolean auto) {
        this.auto = auto;
    }
    
    public boolean CardActionMessage.isDefaut() {
        return this.defaut;
    }
    
    public void CardActionMessage.setDefaut(boolean defaut) {
        this.defaut = defaut;
    }
    
    public String CardActionMessage.getMailTo() {
        return this.mailTo;
    }
    
    public void CardActionMessage.setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
    
    public Set<String> CardActionMessage.getUserTypes() {
        return this.userTypes;
    }
    
    public void CardActionMessage.setUserTypes(Set<String> userTypes) {
        this.userTypes = userTypes;
    }
    
    public Integer CardActionMessage.getDateDelay4PreventCaduc() {
        return this.dateDelay4PreventCaduc;
    }
    
    public void CardActionMessage.setDateDelay4PreventCaduc(Integer dateDelay4PreventCaduc) {
        this.dateDelay4PreventCaduc = dateDelay4PreventCaduc;
    }
    
}
