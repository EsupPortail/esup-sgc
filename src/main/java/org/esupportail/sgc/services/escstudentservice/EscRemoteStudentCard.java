package org.esupportail.sgc.services.escstudentservice;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class EscRemoteStudentCard {

	String europeanStudentCardNumber;
	
	Long cardType;
	
	String cardUid;
	
}
