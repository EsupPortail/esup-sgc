package org.esupportail.sgc.services.ldap;

import java.util.List;

import org.esupportail.sgc.domain.ldap.PersonAttributMapper;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;


@Service
public class LdapPersonService{
	

    @Autowired
    private LdapTemplate ldapTemplate;

	public List<PersonLdap> searchByFirstName(String cn) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "person"));
        filter.and(new LikeFilter("cn", "*" + cn + "*"));
        
        List<PersonLdap> results = ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), new PersonAttributMapper());
        
        return results;
	}
	

}
