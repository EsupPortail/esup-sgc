package org.esupportail.sgc.services.crous;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.OneToOne;
import javax.persistence.TypedQuery;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@RooDbManaged(automaticallyDelete = true)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "card", "userAccount", "date" })
@RooJpaActiveRecord(finders = { "findCrousErrorLogsByUserAccount", "findCrousErrorLogsByCard"})
public class CrousErrorLog {

	@OneToOne
    Card card;

	@OneToOne
    User userAccount;

    String code;

    String message;

    String field;

    Date date;
    
    Boolean blocking;
    
    public Long getCardId() {
    	if(card!=null) {
    		return card.getId();
    	}
    	return null;
    }
    
    public String getCardCsn() {
    	if(card!=null) {
    		return card.getCsn();
    	}
    	return null;
    }
    
    public String getUserEppn() {
    	if(userAccount!=null) {
    		return userAccount.getEppn();
    	}
    	return null;
    }
    
    public String getUserDisplayName() {
    	if(userAccount!=null) {
    		return userAccount.getDisplayName();
    	}
    	return null;
    }
    
    public String getUserEmail() {
    	if(userAccount!=null) {
    		return userAccount.getEmail();
    	}
    	return null;
    }

}
