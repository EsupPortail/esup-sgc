package org.esupportail.sgc.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class LoggingExceptionResolver extends SimpleMappingExceptionResolver {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void logException(Exception ex, HttpServletRequest request) {
		if(ex.getCause() == null) {
			log.warn(buildLogMessage(ex, request), ex);
		} else {
			log.error(buildLogMessage(ex, request), ex);
		}
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		// if response is sent (when ClientAbortException for example) - don't try to send view !
		if(response.isCommitted()) {
			return null;
		}
		return super.doResolveException(request, response, handler, ex);
	}
	
}