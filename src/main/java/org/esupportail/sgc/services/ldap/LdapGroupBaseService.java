package org.esupportail.sgc.services.ldap;

import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LdapGroupBaseService extends LdapGroupService {

	private final static Logger log = LoggerFactory.getLogger(LdapGroupBaseService.class);

	String memberAttr;
	
	String memberAttr2EppnRegexp;
	
	String memberAttr2EppnRegexpValue;
	
	String groupName2BaseRegexp;
	
	String groupName2BaseRegexpValue;
	
	public void setMemberAttr(String memberAttr) {
		this.memberAttr = memberAttr;
	}

	public void setMemberAttr2EppnRegexp(String memberAttr2EppnRegexp) {
		this.memberAttr2EppnRegexp = memberAttr2EppnRegexp;
	}

	public void setMemberAttr2EppnRegexpValue(String memberAttr2EppnRegexpValue) {
		this.memberAttr2EppnRegexpValue = memberAttr2EppnRegexpValue;
	}

	public void setGroupName2BaseRegexp(String groupName2BaseRegexp) {
		this.groupName2BaseRegexp = groupName2BaseRegexp;
	}

	public void setGroupName2BaseRegexpValue(String groupName2BaseRegexpValue) {
		this.groupName2BaseRegexpValue = groupName2BaseRegexpValue;
	}

	public void setMemberSearchBase(String memberSearchBase) {
		throw new SgcRuntimeException("A ne pas utiliser pour cette implémentation", null);
	}

	@Override
	public List<String> getMembers(String groupName) {

		String groupBase = groupName.replaceAll(groupName2BaseRegexp, groupName2BaseRegexpValue);

		List<List<String>> eppnsList = new ArrayList<>();
		try {
				eppnsList = ldapTemplate.search(
				groupBase, "(objectClass=*)", new ContextMapper<List<String>>() {
	
						@Override
						public List<String> mapFromContext(Object ctx) throws NamingException {
							List<String> eppns = new ArrayList<String>();
							DirContextAdapter searchResultContext = (DirContextAdapter)ctx;
					        List<String> memberValues = Arrays.asList(searchResultContext.getStringAttributes(memberAttr));
					        for(String memberValue: memberValues) {
					        	String eppn = memberValue.replaceAll(memberAttr2EppnRegexp, memberAttr2EppnRegexpValue);
					        	eppns.add(eppn);
					        }
							return eppns;
						}
					});
		} catch (org.springframework.ldap.NamingException e) {
			log.warn("Failed to retrieve members of Group: " + groupName + " - " + e.getMessage());
			log.trace("LDAP Exception retrieving group " + groupName, e);
		}
		List<String> eppns = new ArrayList<String>();
		for(List<String> eppnList : eppnsList) {
			eppns.addAll(eppnList);
		}
		
		return eppns;
	}
	
}
