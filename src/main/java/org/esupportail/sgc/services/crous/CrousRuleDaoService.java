package org.esupportail.sgc.services.crous;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
public class CrousRuleDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("rne", "referenceStatus", "indiceMin", "indiceMax", "codeSociete", "codeTarif", "priority", "crousRuleConfig", "updateDate");

    public List<CrousRule> findAllCrousRules(CrousRuleConfig crousRuleConfig) {
        String jpaQuery = "SELECT o FROM CrousRule o where o.crousRuleConfig=:crousRuleConfig";
        return entityManager.createQuery(jpaQuery, CrousRule.class)
                .setParameter("crousRuleConfig", crousRuleConfig)
                .getResultList();
    }

    public List<CrousRule> findAllCrousRulesApi() {
        return entityManager.createQuery("SELECT o FROM CrousRule o where o.crousRuleConfig IS NOT NULL", CrousRule.class).getResultList();
    }

    public List<CrousRule> findAllCrousRulesCustom() {
        return entityManager.createQuery("SELECT o FROM CrousRule o where o.crousRuleConfig  IS NULL", CrousRule.class).getResultList();
    }

    public long countCrousRules() {
        return entityManager.createQuery("SELECT COUNT(o) FROM CrousRule o", Long.class).getSingleResult();
    }

    public List<CrousRule> findAllCrousRules() {
        return entityManager.createQuery("SELECT o FROM CrousRule o", CrousRule.class).getResultList();
    }

    public List<CrousRule> findAllCrousRules(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRule o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousRule.class).getResultList();
    }

    public CrousRule findCrousRule(Long id) {
        if (id == null) return null;
        return entityManager.find(CrousRule.class, id);
    }

    public List<CrousRule> findCrousRuleEntries(int firstResult, int maxResults) {
        return entityManager.createQuery("SELECT o FROM CrousRule o", CrousRule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public List<CrousRule> findCrousRuleEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRule o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CrousRule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist(CrousRule crousRule) {
        this.entityManager.persist(crousRule);
    }

    @Transactional
    public void remove(CrousRule crousRule) {
        if (this.entityManager.contains(crousRule)) {
            this.entityManager.remove(crousRule);
        } else {
            CrousRule attached = findCrousRule(crousRule.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public CrousRule merge(CrousRule crousRule) {
        CrousRule merged = this.entityManager.merge(crousRule);
        this.entityManager.flush();
        return merged;
    }
}
