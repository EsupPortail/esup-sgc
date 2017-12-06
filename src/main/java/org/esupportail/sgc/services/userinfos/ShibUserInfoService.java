package org.esupportail.sgc.services.userinfos;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.User;
import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
public class ShibUserInfoService implements ExtUserInfoService {
	
	private Map<String, String> sgcParam2requestHeader = new HashMap<String, String>();
	
	private Long order = Long.valueOf(0);

	private String eppnFilter = ".*";
	
	@Override
	public Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {
		Map<String, String> userInfos = new HashMap<String, String>(); 
		if(request != null) {
			for(String name: sgcParam2requestHeader.keySet()) {
				String value = request.getHeader(sgcParam2requestHeader.get(name));
				userInfos.put(name, value);
			}
		}
		return userInfos;
	}
}
