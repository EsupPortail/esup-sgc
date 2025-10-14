package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.repositories.AppliConfigRepository;
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
public class AppliConfigDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("key", "value", "description", "type");

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    AppliConfigRepository appliConfigRepository;

    public AppliConfig findAppliConfigByKey(String key) {
        List<AppliConfig> configs = this.findAppliConfigsByKeyEquals(key).getResultList();
        if (configs.isEmpty()) {
            throw new SgcRuntimeException("Configuration with key " + key + " not found - please create it", null);
        } else {
            return configs.get(0);
        }
    }

    public AppliConfig findAppliConfig(Long id) {
        if (id == null) return null;
        return entityManager.find(AppliConfig.class, id);
    }


    public Long countFindAppliConfigsByKeyEquals(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM AppliConfig AS o WHERE o.key = :key", Long.class);
        q.setParameter("key", key);
        return ((Long) q.getSingleResult());
    }

    public Long countFindAppliConfigsByKeyLike(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        key = key.replace('*', '%');
        if (key.charAt(0) != '%') {
            key = "%" + key;
        }
        if (key.charAt(key.length() - 1) != '%') {
            key = key + "%";
        }
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM AppliConfig AS o WHERE LOWER(o.key) LIKE LOWER(:key)", Long.class);
        q.setParameter("key", key);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<AppliConfig> findAppliConfigsByKeyEquals(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery<AppliConfig> q = em.createQuery("SELECT o FROM AppliConfig AS o WHERE o.key = :key", AppliConfig.class);
        q.setParameter("key", key);
        return q;
    }

    public TypedQuery<AppliConfig> findAppliConfigsByKeyLike(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        key = key.replace('*', '%');
        if (key.charAt(0) != '%') {
            key = "%" + key;
        }
        if (key.charAt(key.length() - 1) != '%') {
            key = key + "%";
        }
        EntityManager em = entityManager;
        TypedQuery<AppliConfig> q = em.createQuery("SELECT o FROM AppliConfig AS o WHERE LOWER(o.key) LIKE LOWER(:key)", AppliConfig.class);
        q.setParameter("key", key);
        return q;
    }

    @Transactional
    public void persist(AppliConfig appliConfig) {
        this.entityManager.persist(appliConfig);
    }

    @Transactional
    public void remove(AppliConfig appliConfig) {
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(appliConfig);
        } else {
            AppliConfig attached = this.findAppliConfig(appliConfig.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public AppliConfig merge(AppliConfig appliConfig) {
        if (this.entityManager.contains(appliConfig)) {
            return appliConfig;
        } else {
            return this.entityManager.merge(appliConfig);
        }
    }

    public Page<AppliConfig> findAppliConfigs(AppliConfig.TypeConfig typeConfig, Pageable pageable) {
        if(typeConfig!=null) {
            return appliConfigRepository.findByType(typeConfig, pageable);
        } else {
            return appliConfigRepository.findAll(pageable);
        }
    }
}
