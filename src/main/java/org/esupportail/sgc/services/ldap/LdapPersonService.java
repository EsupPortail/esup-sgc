package org.esupportail.sgc.services.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.esupportail.sgc.domain.ldap.PersonAttributMapper;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapUtils;


public class LdapPersonService{
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    private LdapTemplate ldapTemplate;
    
    private Map<String, LdapTemplate> ldapTemplates = new HashMap<String, LdapTemplate>();
    
    private Map<String, String> importExtCardsRights = null;
    
    private Map<String, String> newCardsRights = null;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setLdapTemplates(Map<String, LdapTemplate> ldapTemplates) {
		this.ldapTemplates = ldapTemplates;
	}
	
	public List<String> getLdapTemplatesNames() {
		return new ArrayList<String>(ldapTemplates.keySet());
	}
	
	public void setImportExtCardsRights(Map<String, String> importExtCardsRights) {
		this.importExtCardsRights = importExtCardsRights;
	}

	public void setNewCardsRights(Map<String, String> newCardsRights) {
		this.newCardsRights = newCardsRights;
	}

	public List<PersonLdap> searchByCommonName(String cn, String ldapTemplateName) {
		LdapTemplate ldapTemplateSelected = ldapTemplate;
		if(ldapTemplateName != null && !ldapTemplateName.isEmpty() && ldapTemplates.containsKey(ldapTemplateName)) {
			ldapTemplateSelected = ldapTemplates.get(ldapTemplateName);
		}
		if(ldapTemplateSelected != null) {
	        AndFilter filter = new AndFilter();
	        filter.and(new EqualsFilter("objectclass", "person"));
	        filter.and(new LikeFilter("cn", "*" + cn + "*"));
	        
	        List<PersonLdap> results = ldapTemplateSelected.search(LdapUtils.emptyLdapName(), filter.encode(), new PersonAttributMapper());       
	        return results;
		} else {
			log.warn("No ldapTemplate found -> LdapPersonService.searchByCommonName result is empty");
			return new ArrayList<PersonLdap>();
		}
	}
	
}
