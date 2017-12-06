package org.esupportail.sgc.services.userinfos;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.User;

public interface ExtUserInfoService {

	Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing);
	
	Long getOrder();
	
	String getEppnFilter();

}