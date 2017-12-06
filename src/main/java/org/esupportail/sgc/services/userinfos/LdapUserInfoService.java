package org.esupportail.sgc.services.userinfos;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.User;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
public class LdapUserInfoService implements ExtUserInfoService {
	
	private Map<String, String> sgcParam2ldapAttr = new HashMap<String, String>();
	
	private LdapTemplate ldapTemplate;
	
	private Long order = Long.valueOf(0);
	
	private String eppnFilter = ".*";
	
	@Override
	public Map<String,String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {	
		Map<String, String> userInfos = new HashMap<String, String>();

		List<Map<String, String>>  userInfosList = ldapTemplate.search(query().where("eduPersonPrincipalName").is(user.getEppn()),
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

