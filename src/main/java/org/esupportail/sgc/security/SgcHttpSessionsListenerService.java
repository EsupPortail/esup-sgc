package org.esupportail.sgc.security;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.esupportail.sgc.domain.SgcHttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class SgcHttpSessionsListenerService implements HttpSessionListener, ServletContextListener {

    Logger log = LoggerFactory.getLogger(SgcHttpSessionsListenerService.class);

    Map<String, SgcHttpSession> sessions = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("sgcHttpSessionsListenerService", this);
        log.info("sgcHttpSessionsListenerService initialized and set in servlet context");
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String id = se.getSession().getId();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String remoteIp = request.getRemoteAddr();
        String originRequestUri = request.getRequestURI();
        Date createdDate = new Date();
        SgcHttpSession session = new SgcHttpSession();
        session.setSessionId(id);
        session.setRemoteIp(remoteIp);
        session.setCreatedDate(createdDate);
        session.setOriginRequestUri(originRequestUri);
        sessions.put(id, session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        sessions.remove(se.getSession().getId());
    }

    public Map<String, SgcHttpSession> getSessions() {
        return sessions;
    }

}
