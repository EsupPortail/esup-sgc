package org.esupportail.sgc.services.crous;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@RooDbManaged(automaticallyDelete = true)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "card", "userAccount", "date", "crousOperation", "esupSgcOperation" })
@RooJpaActiveRecord(finders = { "findCrousErrorLogsByUserAccount", "findCrousErrorLogsByCard"})
public class CrousErrorLog {

    public static enum CrousOperation {
    	GET, PUT, POST, PATCH
    };
    
    public static enum EsupSgcOperation {
    	ACTIVATE, DESACTIVATE, SYNC, PATCH, GET
    };
    
	@OneToOne
    Card card;

	@OneToOne
    User userAccount;

    String code;

    String message;

    String field;

    Date date;
    
    Boolean blocking;
    
    @Column
    @Enumerated(EnumType.STRING)
    private CrousOperation crousOperation;
    
    @Column
    @Enumerated(EnumType.STRING)
    private EsupSgcOperation esupSgcOperation;
    
    private String crousUrl;
    
    public Long getCardId() {
    	if(card!=null) {
    		return card.getId();
    	}
    	return null;
    }
    
    public String getCardCsn() {
    	if(card!=null) {
    		return card.getCsn();
    	}
    	return null;
    }
    
    public String getUserEppn() {
    	if(userAccount!=null) {
    		return userAccount.getEppn();
    	}
    	return null;
    }
    
    public String getUserDisplayName() {
    	if(userAccount!=null) {
    		return userAccount.getDisplayName();
    	}
    	return null;
    }
    
    public String getUserEmail() {
    	if(userAccount!=null) {
    		return userAccount.getEmail();
    	}
    	return null;
    }

	public static List<CrousErrorLog> findCrousErrorLogs(CrousErrorLog searchCrousErrorLog, int firstResult, int sizeNo,
			String sortFieldName, String sortOrder) {
		EntityManager em = entityManager();
	    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
	    CriteriaQuery<CrousErrorLog> query = criteriaBuilder.createQuery(CrousErrorLog.class);
	    Root<CrousErrorLog> c = query.from(CrousErrorLog.class);
	    
	    final List<Order> orders = new ArrayList<Order>();
	    if ("DESC".equalsIgnoreCase(sortOrder)) {
	        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
	            orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
	        } 
	    } else {
	        if(fieldNames4OrderClauseFilter.contains(sortFieldName)) {
	            orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
	        }
	    }

	    orders.add(criteriaBuilder.desc(c.get("id")));
	    
	    final List<Predicate> predicates = getPredicates4SearchCrousErrorLog(searchCrousErrorLog, criteriaBuilder, c);
	    
	    query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
	    query.orderBy(orders);
	    query.select(c);
	    return em.createQuery(query).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList();
	}

	private static List<Predicate> getPredicates4SearchCrousErrorLog(CrousErrorLog searchCrousErrorLog,
			CriteriaBuilder criteriaBuilder, Root<CrousErrorLog> c) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		
        if (searchCrousErrorLog.getBlocking() != null) {
            Expression<Boolean> blockingExpr = c.get("blocking");
            if (searchCrousErrorLog.getBlocking()) {
                predicates.add(criteriaBuilder.isTrue(blockingExpr));
            } else {
                predicates.add(criteriaBuilder.isFalse(blockingExpr));
            }
        }
        if (searchCrousErrorLog.getEsupSgcOperation() != null) {
            predicates.add(criteriaBuilder.equal(c.get("esupSgcOperation"), searchCrousErrorLog.getEsupSgcOperation()));
        }
        
        
        return predicates;
	}

	public static long countCrousErrorLogs(CrousErrorLog searchCrousErrorLog) {
		EntityManager em = entityManager();
	    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
	    CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
	    Root<CrousErrorLog> c = query.from(CrousErrorLog.class);
	    
	    final List<Predicate> predicates = getPredicates4SearchCrousErrorLog(searchCrousErrorLog, criteriaBuilder, c);
	    
	    query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
	    query.select(criteriaBuilder.count(c));
        return em.createQuery(query).getSingleResult();
	}

}
