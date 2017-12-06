package org.esupportail.sgc.services.esc;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class EscrCard {

	String europeanStudentCardNumber;
	
	Long cardType;
	
	String cardUid;

}
