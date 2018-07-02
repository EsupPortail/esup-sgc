package org.esupportail.sgc.domain.ldap;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class PersonLdap {
	
	private String eduPersonPrincipalName;
	
	private String cn ;
	
	private String sn; 
	
	private String givenName ;
	
	private String mail ;
	
	private String schacDateOfBirth;
	
	private String supannEntiteAffectationPrincipale;
	
	private String eduPersonPrimaryAffiliation;
	
}
	
	
