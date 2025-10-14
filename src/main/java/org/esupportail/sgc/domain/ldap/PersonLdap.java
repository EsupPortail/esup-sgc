package org.esupportail.sgc.domain.ldap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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


	public String getEduPersonPrincipalName() {
        return this.eduPersonPrincipalName;
    }

	public void setEduPersonPrincipalName(String eduPersonPrincipalName) {
        this.eduPersonPrincipalName = eduPersonPrincipalName;
    }

	public String getCn() {
        return this.cn;
    }

	public void setCn(String cn) {
        this.cn = cn;
    }

	public String getSn() {
        return this.sn;
    }

	public void setSn(String sn) {
        this.sn = sn;
    }

	public String getGivenName() {
        return this.givenName;
    }

	public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

	public String getMail() {
        return this.mail;
    }

	public void setMail(String mail) {
        this.mail = mail;
    }

	public String getSchacDateOfBirth() {
        return this.schacDateOfBirth;
    }

	public void setSchacDateOfBirth(String schacDateOfBirth) {
        this.schacDateOfBirth = schacDateOfBirth;
    }

	public String getSupannEntiteAffectationPrincipale() {
        return this.supannEntiteAffectationPrincipale;
    }

	public void setSupannEntiteAffectationPrincipale(String supannEntiteAffectationPrincipale) {
        this.supannEntiteAffectationPrincipale = supannEntiteAffectationPrincipale;
    }

	public String getEduPersonPrimaryAffiliation() {
        return this.eduPersonPrimaryAffiliation;
    }

	public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
        this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
	
	
