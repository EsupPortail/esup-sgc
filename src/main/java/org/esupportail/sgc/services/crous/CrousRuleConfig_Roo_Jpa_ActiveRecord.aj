// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.services.crous;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.esupportail.sgc.services.crous.CrousRuleConfig;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CrousRuleConfig_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager CrousRuleConfig.entityManager;
    
    public static final List<String> CrousRuleConfig.fieldNames4OrderClauseFilter = java.util.Arrays.asList("rne", "numeroCrous", "priority");
    
    public static final EntityManager CrousRuleConfig.entityManager() {
        EntityManager em = new CrousRuleConfig().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long CrousRuleConfig.countCrousRuleConfigs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM CrousRuleConfig o", Long.class).getSingleResult();
    }
    
    public static List<CrousRuleConfig> CrousRuleConfig.findAllCrousRuleConfigs() {
        return entityManager().createQuery("SELECT o FROM CrousRuleConfig o", CrousRuleConfig.class).getResultList();
    }
    
    public static List<CrousRuleConfig> CrousRuleConfig.findAllCrousRuleConfigs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRuleConfig o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, CrousRuleConfig.class).getResultList();
    }
    
    public static CrousRuleConfig CrousRuleConfig.findCrousRuleConfig(Long id) {
        if (id == null) return null;
        return entityManager().find(CrousRuleConfig.class, id);
    }
    
    public static List<CrousRuleConfig> CrousRuleConfig.findCrousRuleConfigEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM CrousRuleConfig o", CrousRuleConfig.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<CrousRuleConfig> CrousRuleConfig.findCrousRuleConfigEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CrousRuleConfig o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, CrousRuleConfig.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void CrousRuleConfig.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void CrousRuleConfig.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            CrousRuleConfig attached = CrousRuleConfig.findCrousRuleConfig(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void CrousRuleConfig.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void CrousRuleConfig.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public CrousRuleConfig CrousRuleConfig.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        CrousRuleConfig merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
