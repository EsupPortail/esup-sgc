package org.esupportail.sgc.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.util.StringUtils;

public class SgcWebSecurityExpressionRoot extends WebSecurityExpressionRoot {

	public SgcWebSecurityExpressionRoot(Authentication a, FilterInvocation fi) {
		super(a, fi);
	}
	
	public boolean hasHeader(String headerName, String value) {
		String header = request.getHeader(headerName);
		if (!StringUtils.hasText(header)) {
			return false;
		}
		if (header.contains(value)) {
			return true;
		}
		return false;
	}
	
}
