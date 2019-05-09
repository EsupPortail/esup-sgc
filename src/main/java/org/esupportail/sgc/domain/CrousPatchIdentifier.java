package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findCrousPatchIdentifiersByPatchSuccessNotEquals"})
public class CrousPatchIdentifier {
	
	@Column(unique = true)
	String oldId;
	
	@Column(unique = true)
	String eppnNewId;
	
	@Column(unique = true)
	String mail;
	
	Boolean patchSuccess;
	
    public static Long countFindCrousPatchIdentifiersByPatchSuccessNotEquals(Boolean patchSuccess) {
        if (patchSuccess == null) throw new IllegalArgumentException("The patchSuccess argument is required");
        EntityManager em = CrousPatchIdentifier.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM CrousPatchIdentifier AS o WHERE o.patchSuccess != :patchSuccess or o.patchSuccess is null", Long.class);
        q.setParameter("patchSuccess", patchSuccess);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<CrousPatchIdentifier> findCrousPatchIdentifiersByPatchSuccessNotEquals(Boolean patchSuccess) {
        if (patchSuccess == null) throw new IllegalArgumentException("The patchSuccess argument is required");
        EntityManager em = CrousPatchIdentifier.entityManager();
        TypedQuery<CrousPatchIdentifier> q = em.createQuery("SELECT o FROM CrousPatchIdentifier AS o WHERE o.patchSuccess != :patchSuccess or o.patchSuccess is null", CrousPatchIdentifier.class);
        q.setParameter("patchSuccess", patchSuccess);
        return q;
    }
    
    
    public static TypedQuery<CrousPatchIdentifier> findCrousPatchIdentifiersByPatchSuccessNotEquals(Boolean patchSuccess, String sortFieldName, String sortOrder) {
        if (patchSuccess == null) throw new IllegalArgumentException("The patchSuccess argument is required");
        EntityManager em = CrousPatchIdentifier.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM CrousPatchIdentifier AS o WHERE o.patchSuccess != :patchSuccess or o.patchSuccess is null");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<CrousPatchIdentifier> q = em.createQuery(queryBuilder.toString(), CrousPatchIdentifier.class);
        q.setParameter("patchSuccess", patchSuccess);
        return q;
    }
    
    public static void removeAll() {
    	EntityManager em = CrousPatchIdentifier.entityManager();
    	Query q = em.createQuery("DELETE FROM CrousPatchIdentifier");
    	q.executeUpdate();
    }

}

