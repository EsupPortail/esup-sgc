package org.esupportail.sgc.domain;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.TypedQuery;

import org.esupportail.sgc.domain.Card.Etat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(finders = { "findCardActionMessagesByEtatFinal" })
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
    
    public static Long countFindCardActionMessagesByEtatFinal(Etat etatFinal) {
        if (etatFinal == null) throw new IllegalArgumentException("The etatFinal argument is required");
        EntityManager em = CardActionMessage.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM CardActionMessage AS o WHERE o.etatFinal = :etatFinal", Long.class);
        q.setParameter("etatFinal", etatFinal);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<CardActionMessage> findCardActionMessagesByEtatFinal(Etat etatFinal) {
        if (etatFinal == null) throw new IllegalArgumentException("The etatFinal argument is required");
        EntityManager em = CardActionMessage.entityManager();
        TypedQuery<CardActionMessage> q = em.createQuery("SELECT o FROM CardActionMessage AS o WHERE o.etatFinal = :etatFinal", CardActionMessage.class);
        q.setParameter("etatFinal", etatFinal);
        return q;
    }
}
