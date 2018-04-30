package org.esupportail.sgc.services.ldap;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.sgc.domain.ldap.PersonAttributMapper;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;


@Service
public class LdapPersonService{
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
    private LdapTemplate ldapTemplate;

	public List<PersonLdap> searchByCommonName(String cn) {
		if(ldapTemplate != null) {
	        AndFilter filter = new AndFilter();
	        filter.and(new EqualsFilter("objectclass", "person"));
	        filter.and(new LikeFilter("cn", "*" + cn + "*"));
	        
	        List<PersonLdap> results = ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), new PersonAttributMapper());
	        
	        return results;
		} else {
			log.warn("No ldapTemplate found -> LdapPersonService.searchByCommonName result is empty");
			return new ArrayList<PersonLdap>();
		}
	}
	

}
