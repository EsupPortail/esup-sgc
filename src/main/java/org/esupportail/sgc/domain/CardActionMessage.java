package org.esupportail.sgc.domain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.esupportail.sgc.domain.Card.Etat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
public class CardActionMessage {

    @Column
    @Enumerated(EnumType.STRING)
    Etat etatInitial;
    
    @Column
    @Enumerated(EnumType.STRING)
    Etat etatFinal;

    @Column(columnDefinition="TEXT")
    private String message;

    @Column
    private boolean auto;
    
    @Column
    private boolean defaut;
    
    @Column
    @ElementCollection(targetClass=String.class)
    private Set<String> userTypes = new HashSet<String>();

    public static List<CardActionMessage> findCardActionMessagesByEtatFinalAndUserType(Etat etatFinal, String userType) {
        if (etatFinal == null || userType == null) {
        	return new ArrayList<CardActionMessage>();
        }
        EntityManager em = CardActionMessage.entityManager();
        TypedQuery<CardActionMessage> q = em.createQuery("SELECT o FROM CardActionMessage AS o WHERE o.etatFinal = :etatFinal and :userType MEMBER OF o.userTypes", CardActionMessage.class);
        q.setParameter("etatFinal", etatFinal);
        q.setParameter("userType", userType);
        return q.getResultList();
    }
    
    public static TypedQuery<CardActionMessage> findCardActionMessagesByEtatInitialAndEtatFinalAndUserType(Etat etatInitial, Etat etatFinal, String userType) {
    	if(userType == null) throw new IllegalArgumentException("The userType can't ben null");
        EntityManager em = CardActionMessage.entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CardActionMessage> query = criteriaBuilder.createQuery(CardActionMessage.class);
        Root<CardActionMessage> c = query.from(CardActionMessage.class);
        final List<Predicate> predicates = new ArrayList<Predicate>();
        final List<Order> orders = new ArrayList<Order>();
        orders.add(criteriaBuilder.desc(c.get("defaut")));
        if (etatInitial != null) {
        	List<Predicate> orPredicates = new ArrayList<Predicate>();
        	orPredicates.add(criteriaBuilder.isNull(c.get("etatInitial")));
        	orPredicates.add(criteriaBuilder.equal(c.get("etatInitial"), etatInitial));
            predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[] {})));
        }
        if (etatFinal != null) {
        	predicates.add(criteriaBuilder.equal(c.get("etatFinal"), etatFinal));
        }
        
        predicates.add(criteriaBuilder.isMember(userType, c.get("userTypes")));
        		
        orders.add(criteriaBuilder.desc(c.get("id")));
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);
        return em.createQuery(query);
    }
    
}
