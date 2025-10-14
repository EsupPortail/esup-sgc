package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.PayBoxForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class PayBoxFormDaoService {
    
    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("eppn", "requestDate", "actionUrl", "site", "rang", "identifiant", "total", "devise", "commande", "clientEmail", "retourVariables", "hash", "time", "callbackUrl", "forwardEffectueUrl", "forwardRefuseUrl", "forwardAnnuleUrl", "hmac");
    
    public PayBoxForm findPayBoxForm(Long id) {
        if (id == null) return null;
        return entityManager.find(PayBoxForm.class, id);
    }


    public Long countFindPayBoxFormsByCommandeEquals(String commande) {
        if (commande == null || commande.length() == 0) throw new IllegalArgumentException("The commande argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayBoxForm AS o WHERE o.commande = :commande", Long.class);
        q.setParameter("commande", commande);
        return ((Long) q.getSingleResult());
    }

    public Long countFindPayBoxFormsByRequestDateLessThan(Date requestDate) {
        if (requestDate == null) throw new IllegalArgumentException("The requestDate argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayBoxForm AS o WHERE o.requestDate < :requestDate", Long.class);
        q.setParameter("requestDate", requestDate);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<PayBoxForm> findPayBoxFormsByCommandeEquals(String commande) {
        if (commande == null || commande.length() == 0) throw new IllegalArgumentException("The commande argument is required");
        EntityManager em = entityManager;
        TypedQuery<PayBoxForm> q = em.createQuery("SELECT o FROM PayBoxForm AS o WHERE o.commande = :commande", PayBoxForm.class);
        q.setParameter("commande", commande);
        return q;
    }

    public TypedQuery<PayBoxForm> findPayBoxFormsByRequestDateLessThan(LocalDateTime requestDate) {
        if (requestDate == null) throw new IllegalArgumentException("The requestDate argument is required");
        EntityManager em = entityManager;
        TypedQuery<PayBoxForm> q = em.createQuery("SELECT o FROM PayBoxForm AS o WHERE o.requestDate < :requestDate", PayBoxForm.class);
        q.setParameter("requestDate", requestDate);
        return q;
    }

    @Transactional
    public void persist(PayBoxForm payBoxForm) {
        this.entityManager.persist(payBoxForm);
    }

    @Transactional
    public void remove(PayBoxForm payBoxForm) {
        if (this.entityManager.contains(payBoxForm)) {
            this.entityManager.remove(payBoxForm);
        } else {
            PayBoxForm attached = findPayBoxForm(payBoxForm.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public PayBoxForm merge(PayBoxForm payBoxForm) {
        PayBoxForm merged = this.entityManager.merge(payBoxForm);
        this.entityManager.flush();
        return merged;
    }
}
