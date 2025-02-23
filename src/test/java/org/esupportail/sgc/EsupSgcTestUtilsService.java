package org.esupportail.sgc;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.services.ldap.LdapPersonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EsupSgcTestUtilsService {
	
	@Resource
	LdapPersonService ldapPersonService;

	@Value("${test.userinfo.eppn2test:}") 
	String eppn2testFromConfig; 

	public String getEppnFromDb() {
		String eppn2test = null;
		List<String> eppns = User.findAllEppns();
		if(!eppns.isEmpty()) {
			eppn2test = eppns.get(0);
		}
		return eppn2test;
	}

	public User getUserFromDb() {
		if(User.countUsers()>0) {
			return User.findAllUsersQuery().setMaxResults(1).getSingleResult();
		}
		return null;
	}
    
	public String getEppnFromLdap() {
    	String eppn2test = null;
		List<PersonLdap> personsLdap = ldapPersonService.searchByCommonName("A", null);
		if(!personsLdap.isEmpty()) {
			eppn2test = personsLdap.get(0).getEduPersonPrincipalName();
		}
		return eppn2test;
	}
    
	public String getEppnFromConfig() {
    	if(!eppn2testFromConfig.isEmpty()) {
    		return eppn2testFromConfig;
    	}
		return null;
	}

	public Card getEncodedCardFromDb() {
		TypedQuery<Card> cardQuery = Card.findCardsByEtatIn(Arrays.asList(Card.Etat.ENABLED)).setMaxResults(1);
		if(cardQuery.getResultList().size()>0) {
			return cardQuery.getSingleResult();
		}
		return null;
	}
}

