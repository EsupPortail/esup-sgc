package org.esupportail.sgc.services.userinfos;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.tools.DateUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelUserInfoService implements ExtUserInfoService {
	
	private Map<String, String> sgcParam2spelExp = new HashMap<String, String>();
	
	private Long order = Long.valueOf(0);

	private String eppnFilter = ".*";
	
	@Resource
	DateUtils dateUtils;

	String beanName;

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

	public void setSgcParam2spelExp(Map<String, String> sgcParam2spelExp) {
		this.sgcParam2spelExp = sgcParam2spelExp;
	}

	@Override
	public Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {
		
		Map<String, String> userInfos = new HashMap<String, String>(); 
		for(String name: sgcParam2spelExp.keySet()) {
			String expression = sgcParam2spelExp.get(name);
			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(expression);

			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("user", user);
			context.setVariable("request", request);
			context.setVariable("userInfosInComputing", userInfosInComputing);
			context.setVariable("dateUtils", dateUtils);
			
			String value = (String) exp.getValue(context);
			userInfos.put(name, value);
		}
		return userInfos;
	}
}
