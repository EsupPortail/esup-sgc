package org.esupportail.sgc.services.crous;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
public class CrousRuleConfigDaoService {
    
    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("rne", "numeroCrous", "priority");

    public long countCrousRuleConfigs() {
        return entityManager.createQuery("SELECT COUNT(o) FROM CrousRuleConfig o", Long.class).getSingleResult();
    }

    public List<CrousRuleConfig> findAllCrousRuleConfigs() {
        return entityManager.createQuery("SELECT o FROM CrousRuleConfig o", CrousRuleConfig.class).getResultList();
    }

    public List<CrousRuleConfig> findAllCrousRuleConfigs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRuleConfig o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousRuleConfig.class).getResultList();
    }

    public CrousRuleConfig findCrousRuleConfig(Long id) {
        if (id == null) return null;
        return entityManager.find(CrousRuleConfig.class, id);
    }

    public List<CrousRuleConfig> findCrousRuleConfigEntries(int firstResult, int maxResults) {
        return entityManager.createQuery("SELECT o FROM CrousRuleConfig o", CrousRuleConfig.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public List<CrousRuleConfig> findCrousRuleConfigEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRuleConfig o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousRuleConfig.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist(CrousRuleConfig crousRuleConfig) {
        this.entityManager.persist(crousRuleConfig);
    }

    @Transactional
    public void remove(CrousRuleConfig crousRuleConfig) {
        if (this.entityManager.contains(crousRuleConfig)) {
            this.entityManager.remove(crousRuleConfig);
        } else {
            CrousRuleConfig attached = findCrousRuleConfig(crousRuleConfig.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public CrousRuleConfig merge(CrousRuleConfig crousRuleConfig) {
        CrousRuleConfig merged = this.entityManager.merge(crousRuleConfig);
        this.entityManager.flush();
        return merged;
    }
    
}
