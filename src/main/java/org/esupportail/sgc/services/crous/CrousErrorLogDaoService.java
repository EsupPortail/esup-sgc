package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.repositories.CrousErrorLogRepository;
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
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrousErrorLogDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    CrousErrorLogRepository crousErrorLogRepository;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("card", "userAccount", "code", "message", "field", "date", "blocking", "crousOperation", "esupSgcOperation", "crousUrl");

    public List<CrousErrorLog> findAllCrousErrorLogs() {
        return entityManager.createQuery("SELECT o FROM CrousErrorLog o", CrousErrorLog.class).getResultList();
    }

    public List<CrousErrorLog> findAllCrousErrorLogs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousErrorLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousErrorLog.class).getResultList();
    }

    public CrousErrorLog findCrousErrorLog(Long id) {
        if (id == null) return null;
        return entityManager.find(CrousErrorLog.class, id);
    }

    public Page<CrousErrorLog> findCrousErrorLogs(CrousErrorLog searchCrousErrorLog, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("tryCount")
                .withIgnoreNullValues()
                .withIgnoreCase();
        Example<CrousErrorLog> searchCrousErrorLogQuery = Example.of(searchCrousErrorLog, matcher);

        return crousErrorLogRepository.findAll(searchCrousErrorLogQuery, pageable);
    }

    public Long countFindCrousErrorLogsByCard(Card card) {
        if (card == null) throw new IllegalArgumentException("The card argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM CrousErrorLog AS o WHERE o.card = :card", Long.class);
        q.setParameter("card", card);
        return ((Long) q.getSingleResult());
    }

    public Long countFindCrousErrorLogsByUserAccount(User userAccount) {
        if (userAccount == null) throw new IllegalArgumentException("The userAccount argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM CrousErrorLog AS o WHERE o.userAccount = :userAccount", Long.class);
        q.setParameter("userAccount", userAccount);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<CrousErrorLog> findCrousErrorLogsByCard(Card card) {
        if (card == null) throw new IllegalArgumentException("The card argument is required");
        EntityManager em = entityManager;
        TypedQuery<CrousErrorLog> q = em.createQuery("SELECT o FROM CrousErrorLog AS o WHERE o.card = :card", CrousErrorLog.class);
        q.setParameter("card", card);
        return q;
    }

    public TypedQuery<CrousErrorLog> findCrousErrorLogsByUserAccount(User userAccount) {
        if (userAccount == null) throw new IllegalArgumentException("The userAccount argument is required");
        EntityManager em = entityManager;
        TypedQuery<CrousErrorLog> q = em.createQuery("SELECT o FROM CrousErrorLog AS o WHERE o.userAccount = :userAccount", CrousErrorLog.class);
        q.setParameter("userAccount", userAccount);
        return q;
    }

    @Transactional
    public void persist(CrousErrorLog crousErrorLog) {
        this.entityManager.persist(crousErrorLog);
    }

    @Transactional
    public void remove(CrousErrorLog crousErrorLog) {
        if (this.entityManager.contains(crousErrorLog)) {
            this.entityManager.remove(crousErrorLog);
        } else {
            CrousErrorLog attached = findCrousErrorLog(crousErrorLog.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public CrousErrorLog merge(CrousErrorLog crousErrorLog) {
        CrousErrorLog merged = this.entityManager.merge(crousErrorLog);
        this.entityManager.flush();
        return merged;
    }

    public List<String> getCrousErrorLogMessages() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CrousErrorLog> root = cq.from(CrousErrorLog.class);
        cq.select(root.get("message")).distinct(true);
        cq.orderBy(cb.asc(root.get("message")));
        TypedQuery<String> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
