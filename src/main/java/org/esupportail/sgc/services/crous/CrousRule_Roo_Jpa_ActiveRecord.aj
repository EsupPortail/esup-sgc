// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.services.crous;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.esupportail.sgc.services.crous.CrousRule;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CrousRule_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager CrousRule.entityManager;
    
    public static final List<String> CrousRule.fieldNames4OrderClauseFilter = java.util.Arrays.asList("rne", "referenceStatus", "indiceMin", "indiceMax", "codeSociete", "codeTarif", "priority", "crousRuleConfig", "updateDate");
    
    public static final EntityManager CrousRule.entityManager() {
        EntityManager em = new CrousRule().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long CrousRule.countCrousRules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM CrousRule o", Long.class).getSingleResult();
    }
    
    public static List<CrousRule> CrousRule.findAllCrousRules() {
        return entityManager().createQuery("SELECT o FROM CrousRule o", CrousRule.class).getResultList();
    }
    
    public static List<CrousRule> CrousRule.findAllCrousRules(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRule o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, CrousRule.class).getResultList();
    }
    
    public static CrousRule CrousRule.findCrousRule(Long id) {
        if (id == null) return null;
        return entityManager().find(CrousRule.class, id);
    }
    
    public static List<CrousRule> CrousRule.findCrousRuleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM CrousRule o", CrousRule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<CrousRule> CrousRule.findCrousRuleEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRule o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, CrousRule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void CrousRule.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void CrousRule.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            CrousRule attached = CrousRule.findCrousRule(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void CrousRule.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void CrousRule.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public CrousRule CrousRule.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        CrousRule merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
