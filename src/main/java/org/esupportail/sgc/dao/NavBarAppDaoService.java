package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.NavBarApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NavBarAppDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("title", "url", "icon", "visible4role", "index");


    public List<NavBarApp> findNavBarAppsByVisible4role(Collection<NavBarApp.VisibleRole> visible4roles) {
        EntityManager em = entityManager;
        String sql = "select id from nav_bar_app where nav_bar_app.id in (select nav_bar_app from nav_bar_app_visible4role where visible4role in (:visible4roles))";
        Query q = em.createNativeQuery(sql);
        q.setParameter("visible4roles", visible4roles.stream().map(v->v.toString()).collect(Collectors.toList()));
        List<Long> lids = q.getResultList();

        String jpql = "select o from NavBarApp o where o.id in (:ids) order by o.index";
        Query qq = em.createQuery(jpql);
        qq.setParameter("ids", lids);
        return qq.getResultList();
    }

    public long countNavBarApps() {
        return entityManager.createQuery("SELECT COUNT(o) FROM NavBarApp o", Long.class).getSingleResult();
    }

    public List<NavBarApp> findAllNavBarApps(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM NavBarApp o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, NavBarApp.class).getResultList();
    }

    public NavBarApp findNavBarApp(Long id) {
        if (id == null) return null;
        return entityManager.find(NavBarApp.class, id);
    }

    public List<NavBarApp> findNavBarAppEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM NavBarApp o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, NavBarApp.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist(NavBarApp navBarApp) {
        this.entityManager.persist(navBarApp);
    }

    @Transactional
    public void remove(NavBarApp navBarApp) {
        if (this.entityManager.contains(navBarApp)) {
            this.entityManager.remove(navBarApp);
        } else {
            NavBarApp attached = findNavBarApp(navBarApp.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public NavBarApp merge(NavBarApp navBarApp) {
        NavBarApp merged = this.entityManager.merge(navBarApp);
        this.entityManager.flush();
        return merged;
    }
    
}
