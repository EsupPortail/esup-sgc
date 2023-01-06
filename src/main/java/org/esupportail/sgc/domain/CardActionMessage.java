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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@RooJavaBean
@RooToString(excludeFields={"message"})
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
public class CardActionMessage {

    private static final Logger log = LoggerFactory.getLogger(CardActionMessage.class);

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
    
    @Column(columnDefinition="TEXT")
    private String mailTo;
    
    @Column
    @ElementCollection(targetClass=String.class)
    private Set<String> userTypes = new HashSet<String>();

    Integer dateDelay4PreventCaduc = null;

    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        List<LogMail> logMails = LogMail.findAllLogMailsByCardActionMessage(this);
        for(LogMail logMail : logMails) {
            logMail.setCardActionMessage(null);
        }
        log.info("Remove reference to this CardActionMessage on LogMails before deleting");
        this.entityManager.remove(this);
        log.info("CardActionMessage deleted");
    }
    
    public static List<CardActionMessage> findAllCardActionMessagesAutoWithMailToEmptyOrNull() {
        return entityManager().createQuery("SELECT o FROM CardActionMessage o WHERE auto = true and (o.mailTo IS NULL or o.mailTo = '') and o.dateDelay4PreventCaduc IS NULL", CardActionMessage.class).getResultList();
    }


    public static List<CardActionMessage> findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(Boolean auto, Etat etatInitial, Etat etatFinal, String userType, Boolean mailEmptyOrNull) {
    	if (etatFinal == null || userType == null) {
        	return new ArrayList<CardActionMessage>();
        }
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
        if(auto != null) {
        	predicates.add(criteriaBuilder.equal(c.get("auto"), auto));
        }
        predicates.add(criteriaBuilder.equal(c.get("etatFinal"), etatFinal));       
        predicates.add(criteriaBuilder.isMember(userType, c.get("userTypes")));
        
        if(mailEmptyOrNull) {
	        List<Predicate> orPredicates = new ArrayList<Predicate>();
	    	orPredicates.add(criteriaBuilder.isNull(c.get("mailTo")));
	    	orPredicates.add(criteriaBuilder.equal(c.get("mailTo"), ""));
	        predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[] {})));
        } else {
	        predicates.add(criteriaBuilder.notEqual(c.get("mailTo"), ""));
	    	predicates.add(criteriaBuilder.isNotNull(c.get("mailTo")));
        }
        predicates.add(criteriaBuilder.isNull(c.get("dateDelay4PreventCaduc")));
        
        orders.add(criteriaBuilder.desc(c.get("id")));
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);
        return em.createQuery(query).getResultList();
    }

    public static List<CardActionMessage> findCardActionMessagesWithDateDelay4PreventCaduc() {
        return entityManager().createQuery("SELECT o FROM CardActionMessage o WHERE dateDelay4PreventCaduc > 0 AND etatInitial='ENABLED' AND etatFinal='CADUC'", CardActionMessage.class).getResultList();
    }
}
