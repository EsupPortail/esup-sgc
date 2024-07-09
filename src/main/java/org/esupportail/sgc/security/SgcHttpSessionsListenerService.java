package org.esupportail.sgc.security;

import org.esupportail.sgc.domain.SgcHttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SgcHttpSessionsListenerService {

    Logger log = LoggerFactory.getLogger(SgcHttpSessionsListenerService.class);

    Map<String, SgcHttpSession> sessions = new HashMap<>();

    @EventListener
    public void onHttpSessionCreatedEvent(HttpSessionCreatedEvent event) {
        String id = event.getSession().getId();
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

    @EventListener
    public void onHttpSessionDestroyedEvent(HttpSessionDestroyedEvent event) {
        sessions.remove(event.getSession().getId());
    }

    public Collection<SgcHttpSession> getSessions() {
        return sessions.values();
    }

}
