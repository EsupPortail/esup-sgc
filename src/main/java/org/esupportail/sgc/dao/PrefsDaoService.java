package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.repositories.PrefsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Service
public class PrefsDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    PrefsRepository prefsRepository;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("eppn", "dateModification", "value", "key");

    public Long countFindPrefsesByEppnEqualsAndKeyEquals(String eppn, String key) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Prefs AS o WHERE o.eppn = :eppn  AND o.key = :key", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("key", key);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Prefs> findPrefsesByEppnEqualsAndKeyEquals(String eppn, String key) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery<Prefs> q = em.createQuery("SELECT o FROM Prefs AS o WHERE o.eppn = :eppn  AND o.key = :key", Prefs.class);
        q.setParameter("eppn", eppn);
        q.setParameter("key", key);
        return q;
    }

    public Prefs findPrefs(Long id) {
        if (id == null) return null;
        return entityManager.find(Prefs.class, id);
    }

    public Page<Prefs> findPrefsEntries(Pageable pageable) {
        return prefsRepository.findAll(pageable);
    }

    @Transactional
    public void persist(Prefs prefs) {
        this.entityManager.persist(prefs);
    }

    @Transactional
    public void remove(Prefs prefs) {
        if (this.entityManager.contains(prefs)) {
            this.entityManager.remove(prefs);
        } else {
            Prefs attached = findPrefs(prefs.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public Prefs merge(Prefs prefs) {
        Prefs merged = this.entityManager.merge(prefs);
        this.entityManager.flush();
        return merged;
    }
    
    
}
