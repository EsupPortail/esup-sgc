package org.esupportail.sgc.services.userinfos;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.User;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

public interface ExtUserInfoService extends BeanNameAware {

	Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing);
	
	Long getOrder();
	
	String getEppnFilter();

	String getBeanName();

	String getSpelFilter();

	default boolean matchFilter(User user, HttpServletRequest request, Map<String, String> userInfos) {
		String spelFilter = getSpelFilter();
		if(!StringUtils.hasLength(spelFilter)) {
			return true;
		}
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(spelFilter);
		EvaluationContext context = new StandardEvaluationContext();
		context.setVariable("user", user);
		context.setVariable("request", request);
		context.setVariable("userInfosInComputing", userInfos);
		Boolean value = (Boolean) exp.getValue(context);
		return value;
	}
}