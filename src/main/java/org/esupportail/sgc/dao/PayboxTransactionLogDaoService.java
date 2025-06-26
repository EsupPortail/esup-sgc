package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.repositories.PayboxTransactionLogRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Service
public class PayboxTransactionLogDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    PayboxTransactionLogRepository payboxTransactionLogRepository;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("transactionDate", "eppn", "reference", "montant", "auto", "erreur", "idtrans", "signature");

    public List<Object[]> countNbPayboxByYearEtat() {
        EntityManager em = entityManager;
        String sql = "SELECT CASE WHEN(DATE_PART('month', transaction_date)<7) "
                + "THEN CONCAT(CAST(DATE_PART('year', transaction_date)-1 AS TEXT),'-',CAST(DATE_PART('year', transaction_date) AS TEXT)) "
                + "ELSE CONCAT(CAST(DATE_PART('year', transaction_date) AS TEXT),'-',CAST(DATE_PART('year', transaction_date)+1 AS TEXT)) END AS Saison, CASE WHEN montant = '1' THEN '' ELSE 'FINALISE' END AS etat, count(*) FROM paybox_transaction_log "
                + "WHERE reference IN (SELECT pay_cmd_num FROM card WHERE pay_cmd_num IS NOT NULL) GROUP BY Saison, etat UNION "
                + "SELECT CASE WHEN(DATE_PART('month', transaction_date)<7) "
                + "THEN CONCAT(CAST(DATE_PART('year', transaction_date)-1 AS TEXT),'-',CAST(DATE_PART('year', transaction_date) AS TEXT)) "
                + "ELSE CONCAT(CAST(DATE_PART('year', transaction_date) AS TEXT),'-',CAST(DATE_PART('year', transaction_date)+1 AS TEXT)) END AS Saison, CASE WHEN montant = '1' THEN '' ELSE 'PAYE' END AS etat, count(*) FROM paybox_transaction_log "
                + "WHERE reference NOT IN (SELECT pay_cmd_num FROM card WHERE pay_cmd_num IS NOT NULL) GROUP BY Saison, etat ORDER BY Saison ASC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public TypedQuery<PayboxTransactionLog> findPayboxTransactionLogsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery<PayboxTransactionLog> q = em.createQuery("SELECT o FROM PayboxTransactionLog AS o WHERE o.eppn = :eppn", PayboxTransactionLog.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public TypedQuery<PayboxTransactionLog> findPayboxTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        EntityManager em = entityManager;
        TypedQuery<PayboxTransactionLog> q = em.createQuery("SELECT o FROM PayboxTransactionLog AS o WHERE o.idtrans = :idtrans", PayboxTransactionLog.class);
        q.setParameter("idtrans", idtrans);
        return q;
    }

    public long countPayboxTransactionLogs() {
        return entityManager.createQuery("SELECT COUNT(o) FROM PayboxTransactionLog o", Long.class).getSingleResult();
    }

    public List<PayboxTransactionLog> findAllPayboxTransactionLogs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayboxTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, PayboxTransactionLog.class).getResultList();
    }

    public PayboxTransactionLog findPayboxTransactionLog(Long id) {
        if (id == null) return null;
        return entityManager.find(PayboxTransactionLog.class, id);
    }

    public Page<PayboxTransactionLog> findPayboxTransactionLogEntries(PayboxTransactionLog searchLog, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withMatcher("eppn", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<PayboxTransactionLog> logSearchQuery = Example.of(searchLog, matcher);
        return payboxTransactionLogRepository.findAll(logSearchQuery, pageable);
    }

    @Transactional
    public void persist(PayboxTransactionLog payboxTransactionLog) {
        this.entityManager.persist(payboxTransactionLog);
    }

    @Transactional
    public void remove(PayboxTransactionLog payboxTransactionLog) {
        if (this.entityManager.contains(payboxTransactionLog)) {
            this.entityManager.remove(payboxTransactionLog);
        } else {
            PayboxTransactionLog attached = findPayboxTransactionLog(payboxTransactionLog.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public PayboxTransactionLog merge(PayboxTransactionLog payboxTransactionLog) {
        PayboxTransactionLog merged = this.entityManager.merge(payboxTransactionLog);
        this.entityManager.flush();
        return merged;
    }
    
}
