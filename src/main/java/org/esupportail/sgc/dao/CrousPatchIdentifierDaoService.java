package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.repositories.CrousPatchIdentifierRepository;
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
public class CrousPatchIdentifierDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("oldId", "eppnNewId", "mail", "patchSuccess");
    
    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    CrousPatchIdentifierRepository crousPatchIdentifierRepository;

    public Long countFindCrousPatchIdentifiersByPatchSuccessNotEquals(Boolean patchSuccess) {
        if (patchSuccess == null) throw new IllegalArgumentException("The patchSuccess argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM CrousPatchIdentifier AS o WHERE o.patchSuccess != :patchSuccess or o.patchSuccess is null", Long.class);
        q.setParameter("patchSuccess", patchSuccess);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<CrousPatchIdentifier> findCrousPatchIdentifiersByPatchSuccessNotEquals(Boolean patchSuccess) {
        if (patchSuccess == null) throw new IllegalArgumentException("The patchSuccess argument is required");
        EntityManager em = entityManager;
        TypedQuery<CrousPatchIdentifier> q = em.createQuery("SELECT o FROM CrousPatchIdentifier AS o WHERE o.patchSuccess != :patchSuccess or o.patchSuccess is null", CrousPatchIdentifier.class);
        q.setParameter("patchSuccess", patchSuccess);
        return q;
    }

    public void removeAll() {
        EntityManager em = entityManager;
        Query q = em.createQuery("DELETE FROM CrousPatchIdentifier");
        q.executeUpdate();
    }

    public CrousPatchIdentifier findCrousPatchIdentifier(Long id) {
        if (id == null) return null;
        return entityManager.find(CrousPatchIdentifier.class, id);
    }

    public Page<CrousPatchIdentifier> findAll(Pageable pageable) {
        return crousPatchIdentifierRepository.findAll(pageable);
    }

    @Transactional
    public void persist(CrousPatchIdentifier crousPatchIdentifier) {
        this.entityManager.persist(crousPatchIdentifier);
    }

    @Transactional
    public void remove(CrousPatchIdentifier crousPatchIdentifier) {
        if (this.entityManager.contains(crousPatchIdentifier)) {
            this.entityManager.remove(crousPatchIdentifier);
        } else {
            CrousPatchIdentifier attached = findCrousPatchIdentifier(crousPatchIdentifier.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public CrousPatchIdentifier merge(CrousPatchIdentifier crousPatchIdentifier) {
        CrousPatchIdentifier merged = this.entityManager.merge(crousPatchIdentifier);
        this.entityManager.flush();
        return merged;
    }
    
}
