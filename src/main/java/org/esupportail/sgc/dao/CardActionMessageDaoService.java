package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.LogMail;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.repositories.CardActionMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardActionMessageDaoService {

    private static final Logger log = LoggerFactory.getLogger(CardActionMessageDaoService.class);
    
    public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("log", "etatInitial", "etatFinal", "message", "auto", "defaut", "mailTo", "userTypes", "dateDelay4PreventCaduc");

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    LogMailDaoService logMailDaoService;

    @Resource
    CardActionMessageRepository cardActionMessageRepository;

    public List<CardActionMessage> findAllCardActionMessagesAutoWithMailToEmptyOrNull() {
        return entityManager.createQuery("SELECT o FROM CardActionMessage o WHERE auto = true and (o.mailTo IS NULL or o.mailTo = '') and o.dateDelay4PreventCaduc IS NULL", CardActionMessage.class).getResultList();
    }


    public List<CardActionMessage> findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(Boolean auto, Card.Etat etatInitial, Card.Etat etatFinal, String userType, Boolean mailEmptyOrNull) {
        if (etatFinal == null || userType == null) {
            return new ArrayList<CardActionMessage>();
        }
        EntityManager em = entityManager;
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

    public List<CardActionMessage> findCardActionMessagesWithDateDelay4PreventCaduc() {
        return entityManager.createQuery("SELECT o FROM CardActionMessage o WHERE dateDelay4PreventCaduc > 0 AND etatInitial='ENABLED' AND etatFinal='CADUC'", CardActionMessage.class).getResultList();
    }

    public List<CardActionMessage> findAllCardActionMessages() {
        return entityManager.createQuery("SELECT o FROM CardActionMessage o", CardActionMessage.class).getResultList();
    }

    public Page<CardActionMessage> findCardActionMessages(Pageable pageable) {
        return cardActionMessageRepository.findAll(pageable);
    }

    public CardActionMessage findCardActionMessage(Long id) {
        if (id == null) return null;
        return entityManager.find(CardActionMessage.class, id);
    }

    public List<CardActionMessage> findCardActionMessageEntries(int firstResult, int maxResults) {
        return entityManager.createQuery("SELECT o FROM CardActionMessage o", CardActionMessage.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public List<CardActionMessage> findCardActionMessageEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM CardActionMessage o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, CardActionMessage.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist(CardActionMessage cardActionMessage) {
        this.entityManager.persist(cardActionMessage);
    }

    @Transactional
    public CardActionMessage merge(CardActionMessage cardActionMessage) {
        CardActionMessage merged = this.entityManager.merge(cardActionMessage);
        this.entityManager.flush();
        return merged;
    }


    @Transactional
    public void remove(CardActionMessage cardActionMessage) {
        List<LogMail> logMails = logMailDaoService.findLogMailsByCardActionMessage(cardActionMessage).getResultList();
        for(LogMail logMail : logMails) {
            logMail.setCardActionMessage(null);
        }
        log.info("Remove reference to this CardActionMessage on LogMails before deleting");
        this.entityManager.remove(this);
        log.info("CardActionMessage deleted");
    }
}
