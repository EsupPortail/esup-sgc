package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.repositories.LogRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class LogDaoService {
    
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("logDate", "eppn", "eppnCible", "type", "cardId", "action", "retCode", "comment", "remoteAddress");

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    LogRepository logRepository;

    public List<Object[]> countUserDeliveries() {
        EntityManager em = entityManager;
        String sql = "SELECT to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) FROM log WHERE action = 'USER_DELIVERY' GROUP BY day ORDER BY day";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public Log findLog(Long id) {
        if (id == null) return null;
        return entityManager.find(Log.class, id);
    }

    public Page<Log> findLogEntries(Log searchLog, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withMatcher("eppn", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("eppnCible", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<Log> logSearchQuery = Example.of(searchLog, matcher);
        return logRepository.findAll(logSearchQuery, pageable);
    }

    public Long countFindLogsByCardIdEquals(Long cardId) {
        if (cardId == null) throw new IllegalArgumentException("The cardId argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.cardId = :cardId", Long.class);
        q.setParameter("cardId", cardId);
        return ((Long) q.getSingleResult());
    }

    public Long countFindLogsByEppnCibleEquals(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.eppnCible = :eppnCible", Long.class);
        q.setParameter("eppnCible", eppnCible);
        return ((Long) q.getSingleResult());
    }

    public Long countFindLogsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.eppn = :eppn", Long.class);
        q.setParameter("eppn", eppn);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Log> findLogsByCardIdEquals(Long cardId, String sortFieldName, String sortOrder) {
        if (cardId == null) throw new IllegalArgumentException("The cardId argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.cardId = :cardId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("cardId", cardId);
        return q;
    }

    public TypedQuery<Log> findLogsByEppnCibleEquals(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = entityManager;
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.eppnCible = :eppnCible", Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }

    public TypedQuery<Log> findLogsByEppnCibleEquals(String eppnCible, String sortFieldName, String sortOrder) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.eppnCible = :eppnCible");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }

    public TypedQuery<Log> findLogsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.eppn = :eppn", Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public TypedQuery<Log> findLogsByEppnEquals(String eppn, String sortFieldName, String sortOrder) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.eppn = :eppn");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public TypedQuery<Log> findLogsByLogDateLessThan(LocalDateTime logDate) {
        if (logDate == null) throw new IllegalArgumentException("The logDate argument is required");
        EntityManager em = entityManager;
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.logDate < :logDate", Log.class);
        q.setParameter("logDate", logDate);
        return q;
    }


    @Transactional
    public void persist(Log log) {
        this.entityManager.persist(log);
    }

    @Transactional
    public void remove(Log log) {
        if (this.entityManager.contains(log)) {
            this.entityManager.remove(log);
        } else {
            Log attached = findLog(log.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public Log merge(Log log) {
        Log merged = this.entityManager.merge(log);
        this.entityManager.flush();
        return merged;
    }

}
