package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.LogMail;
import org.esupportail.sgc.repositories.LogMailRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class LogMailDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("cardActionMessage", "logDate", "eppn", "subject", "mailTo", "message");

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    LogMailRepository logMailRepository;

    public Long countFindLogMails(CardActionMessage cardActionMessage, String eppn, Date date1, Date date2) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM LogMail AS o WHERE o.cardActionMessage=:cardActionMessage AND o.eppn=:eppn AND o.logDate > :date1 AND o.logDate < :date2", Long.class);
        q.setParameter("cardActionMessage", cardActionMessage);
        q.setParameter("eppn", eppn);
        q.setParameter("date1", date1);
        q.setParameter("date2", date2);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<LogMail> findLogMailsByCardActionMessage(CardActionMessage cardActionMessage) {
        if (cardActionMessage == null) throw new IllegalArgumentException("The cardActionMessage argument is required");
        EntityManager em = entityManager;
        TypedQuery<LogMail> q = em.createQuery("SELECT o FROM LogMail AS o WHERE o.cardActionMessage = :cardActionMessage", LogMail.class);
        q.setParameter("cardActionMessage", cardActionMessage);
        return q;
    }

    public TypedQuery<LogMail> findLogMailsByLogDateLessThan(LocalDateTime logDate) {
        if (logDate == null) throw new IllegalArgumentException("The logDate argument is required");
        EntityManager em = entityManager;
        TypedQuery<LogMail> q = em.createQuery("SELECT o FROM LogMail AS o WHERE o.logDate < :logDate", LogMail.class);
        q.setParameter("logDate", logDate);
        return q;
    }


    public LogMail findLogMail(Long id) {
        if (id == null) return null;
        return entityManager.find(LogMail.class, id);
    }

    public Page<LogMail> findLogMailEntries(LogMail searchLog, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withMatcher("eppn", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<LogMail> logSearchQuery = Example.of(searchLog, matcher);
        return logMailRepository.findAll(logSearchQuery, pageable);
    }

    @Transactional
    public void persist(LogMail logMail) {
        this.entityManager.persist(logMail);
    }

    @Transactional
    public void remove(LogMail logMail) {
        if (this.entityManager.contains(logMail)) {
            this.entityManager.remove(logMail);
        } else {
            LogMail attached = findLogMail(logMail.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public LogMail merge(LogMail logMail) {
        LogMail merged = this.entityManager.merge(logMail);
        this.entityManager.flush();
        return merged;
    }
    
}
