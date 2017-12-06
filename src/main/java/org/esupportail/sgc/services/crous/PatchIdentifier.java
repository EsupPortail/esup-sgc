package org.esupportail.sgc.services.crous;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class PatchIdentifier {

	String currentIdentifier;
	 
	String email;
	
	String newIdentifier;
			
}
