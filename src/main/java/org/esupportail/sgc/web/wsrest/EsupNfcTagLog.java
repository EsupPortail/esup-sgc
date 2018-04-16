package org.esupportail.sgc.web.wsrest;

import org.springframework.roo.addon.javabean.RooJavaBean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsupNfcTagLog {
	
	public static String SALLE_ENCODAGE = "Encodage ESUP SGC";
	public static String SALLE_LIVRAISON = "Livraison ESUP SGC";
	public static String SALLE_SEARCH = "Recherche ESUP SGC";
	public static String SALLE_UPDATE = "Mise à jour ESUP SGC";
	public static String VERSO_CARTE = "Verso carte";
	public static String SECONDARY_ID = "Identifiant secondaire";
	
	String csn;
	
	String eppn;
	
	String lastname;
	
	String firstname;
	
	String eppnInit;
	
	String location;

	@Override
	public String toString() {
		return "TagLog [location=" + location + ", csn=" + csn + ", eppn=" + eppn + ", lastname=" + lastname + ", firstname=" + firstname + "]";
	}
	
}