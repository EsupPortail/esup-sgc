package org.esupportail.sgc.services.userinfos;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

public class LdapUserInfoService implements ExtUserInfoService {

	private static final Logger log = LoggerFactory.getLogger(LdapUserInfoService.class);

	private Map<String, String> sgcParam2ldapAttr = new HashMap<String, String>();
	
	private LdapTemplate ldapTemplate;
	
	private Long order = Long.valueOf(0);
	
	private String eppnFilter = ".*";

	String beanName;

	String searchFilter = "(eduPersonPrincipalName={eppn})";

	@Override
	public String getBeanName() {
		return beanName;
	}
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	public void setSgcParam2ldapAttr(Map<String, String> sgcParam2ldapAttr) {
		this.sgcParam2ldapAttr = sgcParam2ldapAttr;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	@Override
	public Map<String,String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {	
		Map<String, String> userInfos = new HashMap<String, String>();
		String[] attributesToReturn = sgcParam2ldapAttr.values().toArray(new String[sgcParam2ldapAttr.values().size()]);

		String ldapSearchFilterWithParams = searchFilter.replace("{eppn}", user.getEppn());
		if(userInfosInComputing != null) {
			for (String key : userInfosInComputing.keySet()) {
				if (ldapSearchFilterWithParams.contains("{" + key + "}")) {
					ldapSearchFilterWithParams = ldapSearchFilterWithParams.replace("{" + key + "}", userInfosInComputing.get(key));
				}
			}
		}
		log.debug("LDAP search filter for eppn {}: {}", user.getEppn(), ldapSearchFilterWithParams);
		List<Map<String, String>>  userInfosList = ldapTemplate.search(query().attributes(attributesToReturn).filter(ldapSearchFilterWithParams),
				new AttributesMapper<Map<String, String>>() {

			@Override
			public Map<String, String> mapFromAttributes(Attributes attributes) throws NamingException {
				Map<String, String> userInfos = new HashMap<String, String>();
				for(String name: sgcParam2ldapAttr.keySet()) {
					Attribute attr = attributes.get(sgcParam2ldapAttr.get(name));
					if(attr != null && attr.get() instanceof java.lang.String) {
						List<String> values = new ArrayList<String>();
						NamingEnumeration<?> attrEnum = attr.getAll();
						while(attrEnum.hasMoreElements()) {
							values.add((String)attrEnum.nextElement());
						}
						userInfos.put(name, StringUtils.join(values, ";"));
					} else if(attr != null && attr.get() instanceof byte[]) {
						byte[] value = (byte[])attr.get();
						userInfos.put(name, java.util.Base64.getEncoder().encodeToString(value));
					} else {
						userInfos.put(name, "");
					}
				}
				return userInfos;
			}
		});
		
		if(!userInfosList.isEmpty()) {
			userInfos = userInfosList.get(0);
		}
		return userInfos;
	}

}

