package org.esupportail.sgc.domain;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Query;
import javax.persistence.Table;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
public class NavBarApp {
	
	public enum VisibleRole {
        CONSULT,
        MANAGER,
        UPDATER,
        VERSO,
        LIVREUR
	}

    private String title;

    private String url;

    private String icon;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column
    private Collection<VisibleRole> visible4role;

    private Integer index;
    
    public static List<NavBarApp> findNavBarAppsByVisible4role(Collection<VisibleRole> visible4roles) {
    	 
    	EntityManager em = NavBarApp.entityManager();
    	String sql = "select id from nav_bar_app where nav_bar_app.id in (select nav_bar_app from nav_bar_app_visible4role where visible4role in (:visible4roles))";
    	Query q = em.createNativeQuery(sql);
    	q.setParameter("visible4roles", visible4roles.stream().map(v->v.toString()).collect(Collectors.toList()));
    	List<BigInteger> ids = q.getResultList();
    	
    	List<Long> lids = ids.stream().map(i->i.longValue()).collect(Collectors.toList());
    	String jpql = "select o from NavBarApp o where o.id in (:ids) order by o.index";
    	Query qq = em.createQuery(jpql);
    	qq.setParameter("ids", lids);
    	return qq.getResultList();
    }
}
