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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eduPersonPrincipalName == null) ? 0 : eduPersonPrincipalName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonLdap other = (PersonLdap) obj;
		if (eduPersonPrincipalName == null) {
			if (other.eduPersonPrincipalName != null)
				return false;
		} else if (!eduPersonPrincipalName.equals(other.eduPersonPrincipalName))
			return false;
		return true;
	}

}
	
	
