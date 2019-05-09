package org.esupportail.sgc.security;

import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.util.Assert;

/**
 * @See DefaultWebSecurityExpressionHandler
 *
 */
public class SgcWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	private String defaultRolePrefix = "ROLE_";

	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(
			Authentication authentication, FilterInvocation fi) {
		SgcWebSecurityExpressionRoot root = new SgcWebSecurityExpressionRoot(authentication, fi);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		root.setDefaultRolePrefix(this.defaultRolePrefix);
		return root;
	}

	public void setTrustResolver(AuthenticationTrustResolver trustResolver) {
		Assert.notNull(trustResolver, "trustResolver cannot be null");
		this.trustResolver = trustResolver;
	}


	public void setDefaultRolePrefix(String defaultRolePrefix) {
		this.defaultRolePrefix = defaultRolePrefix;
	}
}
