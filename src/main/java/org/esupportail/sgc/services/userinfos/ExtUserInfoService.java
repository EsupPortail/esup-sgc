package org.esupportail.sgc.services.userinfos;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.User;
import org.springframework.beans.factory.BeanNameAware;

public interface ExtUserInfoService extends BeanNameAware {

	Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing);
	
	Long getOrder();
	
	String getEppnFilter();

	String getBeanName();

}