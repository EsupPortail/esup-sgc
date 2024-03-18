package org.esupportail.sgc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoggingHttp403ForbiddenEntryPoint extends Http403ForbiddenEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(LoggingHttp403ForbiddenEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException {
        log.warn("Access denied for " + request.getRequestURI() +
                " with IP " + request.getRemoteAddr() +
                " and user agent " + request.getHeader("User-Agent") +
                " and remote user " + request.getRemoteUser());
        super.commence(request, response, arg2);
    }

}
