package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliVersion;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Service
public class AppliVersionDaoService {
    
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("esupSgcVersion");
    
    @PersistenceContext
    transient EntityManager entityManager;

    public AppliVersion getAppliVersion() {
        List<AppliVersion> appliVersions = this.findAllAppliVersions("esupSgcVersion", "desc");
        if(appliVersions.isEmpty()) {
            return null;
        }
        return appliVersions.get(0);
    }

    public List<AppliVersion> findAllAppliVersions() {
        return entityManager.createQuery("SELECT o FROM AppliVersion o", AppliVersion.class).getResultList();
    }

    public List<AppliVersion> findAllAppliVersions(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM AppliVersion o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, AppliVersion.class).getResultList();
    }

    public AppliVersion findAppliVersion(Long id) {
        if (id == null) return null;
        return entityManager.find(AppliVersion.class, id);
    }

    @Transactional
    public void persist(AppliVersion appliVersion) {
        this.entityManager.persist(appliVersion);
    }

    public void merge(AppliVersion appliVersion) {
        this.entityManager.merge(appliVersion);
    }

    public void remove(AppliVersion appliVersion) {
        if (!this.entityManager.contains(appliVersion)) {
            appliVersion = this.findAppliVersion(appliVersion.getId());
        }
        this.entityManager.remove(appliVersion);
    }
}
