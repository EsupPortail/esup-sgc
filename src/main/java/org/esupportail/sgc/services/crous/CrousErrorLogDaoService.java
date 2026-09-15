package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.repositories.CrousErrorLogRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
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

    public List<CrousErrorLog> findAllCrousErrorLogs(String sortFieldName, String sortOrder, int firstResult, int maxResults) {
        String jpaQuery = "SELECT o FROM CrousErrorLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousErrorLog.class)
            .setFirstResult(firstResult)
            .setMaxResults(maxResults)
            .getResultList();
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

        Specification<CrousErrorLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Reuse the existing exact-match / case-insensitive behavior for the persisted fields
            Predicate examplePredicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, searchCrousErrorLogQuery);
            if (examplePredicate != null) {
                predicates.add(examplePredicate);
            }

            // Additional "like" filters on name / mail / csn / eppn (from related userAccount / card entities)
            if (StringUtils.isNotBlank(searchCrousErrorLog.getName())) {
                Join<CrousErrorLog, User> userJoin = root.join("userAccount", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(userJoin.get("name")), "%" + searchCrousErrorLog.getName().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(searchCrousErrorLog.getMail())) {
                Join<CrousErrorLog, User> userJoin = root.join("userAccount", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(userJoin.get("email")), "%" + searchCrousErrorLog.getMail().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(searchCrousErrorLog.getEppn())) {
                Join<CrousErrorLog, User> userJoin = root.join("userAccount", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(userJoin.get("eppn")), "%" + searchCrousErrorLog.getEppn().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(searchCrousErrorLog.getCsn())) {
                Join<CrousErrorLog, Card> cardJoin = root.join("card", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(cardJoin.get("csn")), "%" + searchCrousErrorLog.getCsn().toLowerCase() + "%"));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };


        return crousErrorLogRepository.findAll(spec, pageable);
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
        return crousErrorLogRepository.findDistinctMessages();
    }

    public List<String> getCrousErrorLogCodes() {
        return crousErrorLogRepository.findDistinctCodes();
    }
}
