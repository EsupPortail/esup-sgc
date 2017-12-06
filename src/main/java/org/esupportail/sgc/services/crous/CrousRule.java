package org.esupportail.sgc.services.crous;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooToString
@RooJavaBean
public class CrousRule {

	String rne;
	
	String referenceStatus;
	
	Long indiceMin;
	
	Long indiceMax;
	
	Long codeSociete;
	
	Long codeTarif;
	
}
