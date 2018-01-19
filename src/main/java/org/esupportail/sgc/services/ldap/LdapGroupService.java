package org.esupportail.sgc.services.ldap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

public class LdapGroupService implements GroupService {
	
	LdapTemplate ldapTemplate;
	
	String groupSearchBase;
	
	String groupSearchFilter;
	
	String memberSearchBase;
	
	String memberSearchFilter;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public void setMemberSearchBase(String memberSearchBase) {
		this.memberSearchBase = memberSearchBase;
	}

	public void setMemberSearchFilter(String memberSearchFilter) {
		this.memberSearchFilter = memberSearchFilter;
	}

	@Override
	public List<String> getGroupsForEppn(String eppn) {
		
		String username = eppn.replaceAll("@.*", "");	
		
		List<String> dns = ldapTemplate.search(query().where("eduPersonPrincipalName").is(eppn),
				new ContextMapper<String>() {

					@Override
					public String mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
				        String dn = searchResultContext.getNameInNamespace();
						return dn;
					}
			
		});
		
		List<String> groups = new ArrayList<String>();
		
		if(!dns.isEmpty()) {
			String userDn = dns.get(0);
			String formattedFilter = MessageFormat.format(groupSearchFilter, new String[] { userDn, username });
			
			groups = ldapTemplate.search(
					groupSearchBase, formattedFilter,new ContextMapper<String>() {
	
						@Override
						public String mapFromContext(Object ctx) throws NamingException {
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        String dn = searchResultContext.getNameInNamespace();
							return dn;
						}
					});
		} 
		
		return groups;
		
	}

	@Override
	public List<String> getMembers(String groupName) {

		String formattedFilter = MessageFormat.format(memberSearchFilter, new String[] {groupName});
			
		List<String> eppns = ldapTemplate.search(
				memberSearchBase, formattedFilter,new ContextMapper<String>() {
	
						@Override
						public String mapFromContext(Object ctx) throws NamingException {
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        String eppn = searchResultContext.getStringAttribute("eduPersonPrincipalName");
							return eppn;
						}
					});
		
		return eppns;
	}
	
}
