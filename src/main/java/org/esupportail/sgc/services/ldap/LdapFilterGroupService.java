package org.esupportail.sgc.services.ldap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

public class LdapFilterGroupService implements GroupService {

	LdapTemplate ldapTemplate;
	
	Map<String, String> ldapFiltersGroups;
	
	String memberSearchFilter = "(&(eduPersonPrincipalName={0})({1}))";

	String beanName;

	@Override
	public String getBeanName() {
		return beanName;
	}

	@Override
	public boolean canManageGroup(String groupName) {
		return ldapFiltersGroups.containsValue(groupName);
	}

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setLdapFiltersGroups(Map<String, String> ldapFiltersGroups) {
		this.ldapFiltersGroups = ldapFiltersGroups;
	}

	public void setMemberSearchFilter(String memberSearchFilter) {
		this.memberSearchFilter = memberSearchFilter;
	}

	@Override
	public List<String> getGroupsForEppn(String eppn) {
		
		List<String> groups = new ArrayList<String>();
		
		for(String ldapFilter: ldapFiltersGroups.keySet()) {
			
			String harcodedFilter = MessageFormat.format(memberSearchFilter, new Object[] {eppn, ldapFilter});
			
			List<String> dns = ldapTemplate.search(query().filter(harcodedFilter),
					new ContextMapper<String>() {
	
						@Override
						public String mapFromContext(Object ctx) throws NamingException {
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        String dn = searchResultContext.getNameInNamespace();
							return dn;
						}
				
			});
			
			if(!dns.isEmpty()) {
				groups.add(ldapFiltersGroups.get(ldapFilter));
			}
		
		}
		
		return groups;
		
	}

	@Override
	public List<String> getMembers(String groupName) {

		Set<String> eppns = new HashSet<String>();

		for(String ldapFilter: ldapFiltersGroups.keySet()) {

			if(ldapFiltersGroups.get(ldapFilter).equals(groupName)) {

				eppns.addAll(ldapTemplate.search(query().filter(ldapFilter) ,new ContextMapper<String>() {

					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
						String eppn = searchResultContext.getStringAttribute("eduPersonPrincipalName");
						return eppn;
					}
				}));
			}
		}

		return new ArrayList<String>(eppns);
	}

}
