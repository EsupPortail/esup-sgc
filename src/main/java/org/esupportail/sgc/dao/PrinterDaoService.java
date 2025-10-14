package org.esupportail.sgc.dao;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Printer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Service
public class PrinterDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("label", "eppn", "ip", "maintenanceInfo", "printerUsers", "printerGroups", "connectionDate");

    public long countPrinters() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Printer o", Long.class).getSingleResult();
    }

    public Printer findPrinter(Long id) {
        if (id == null) return null;
        return entityManager.find(Printer.class, id);
    }

    public List<Printer> findPrinterEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Printer o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, Printer.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public TypedQuery<Printer> findPrintersByEppn(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o WHERE o.eppn = :eppn", Printer.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public Query findPrintersByEppnOrByEppnInPrinterUsersOryEppnInPrinterGroups(String eppn, List<String> groups) {
        EntityManager em = entityManager;
        TypedQuery<Printer> q = em.createQuery("SELECT o FROM Printer AS o LEFT JOIN o.printerGroups g LEFT JOIN o.printerUsers u WHERE o.eppn = :eppn OR :eppn IN (u) OR g IN (:groups)", Printer.class);
        q.setParameter("eppn", eppn);
        q.setParameter("groups", groups);
        return q;
    }

    public List<Printer> findAllPrinters(String sortFieldName, String sortOrder) {
        if(StringUtils.isEmpty(sortFieldName)) {
            sortFieldName = "connectionDate";
            sortOrder = "DESC";
        }
        String jpaQuery = "SELECT o FROM Printer o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        EntityManager em = entityManager;
        return em.createQuery(jpaQuery, Printer.class).getResultList();
    }

    @Transactional
    public void persist(Printer printer) {
        this.entityManager.persist(printer);
    }

    @Transactional
    public void remove(Printer printer) {
        if (this.entityManager.contains(printer)) {
            this.entityManager.remove(printer);
        } else {
            Printer attached = findPrinter(printer.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public Printer merge(Printer printer) {
        Printer merged = this.entityManager.merge(printer);
        this.entityManager.flush();
        return merged;
    }
}
