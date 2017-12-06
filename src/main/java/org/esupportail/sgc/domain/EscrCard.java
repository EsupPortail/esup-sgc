package org.esupportail.sgc.domain;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true, value = { "id", "version", "hibernateLazyInitializer", "handler"})
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(finders = { "findEscrCardsByCardUidEquals" })
public class EscrCard {

	String europeanStudentCardNumber;
	
	Long cardType;
	
	String cardUid;

}
