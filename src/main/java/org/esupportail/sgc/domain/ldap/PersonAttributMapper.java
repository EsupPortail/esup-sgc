package org.esupportail.sgc.domain.ldap;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class PersonAttributMapper implements AttributesMapper {
	
	public PersonLdap mapFromAttributes(Attributes attrs)
			throws javax.naming.NamingException {
		PersonLdap p = new PersonLdap();
		Attribute attrGivenName = attrs.get("givenName");
		if(attrGivenName != null) {
			p.setGivenName(attrGivenName.get().toString());
		}
		if (null!=attrs.get("sn")) {
			p.setSn(attrs.get("sn").get().toString());
		}
		if (null!=attrs.get("uid")) {
			p.setUid(attrs.get("uid").get().toString());
		}
		if (null!=attrs.get("cn")) {
			p.setCn(attrs.get("cn").get().toString());
		}
		if (null!=attrs.get("mail")) {
			p.setMail(attrs.get("mail").get().toString());
		}
		if (null!=attrs.get("eduPersonPrincipalName")) {
			p.setEduPersonPrincipalName(attrs.get("eduPersonPrincipalName").get().toString());
		}
		if (null!=attrs.get("supannEntiteAffectationPrincipale")) {
			p.setSupannEntiteAffectationPrincipale(attrs.get("supannEntiteAffectationPrincipale").get().toString());
		}
		if (null!=attrs.get("schacDateOfBirth")) {
			p.setSchacDateOfBirth(attrs.get("schacDateOfBirth").get().toString());
		}
		if (null!=attrs.get("eduPersonPrimaryAffiliation")) {
			p.setEduPersonPrimaryAffiliation(attrs.get("eduPersonPrimaryAffiliation").get().toString());
		}
		return p;
	}
	
	
}
