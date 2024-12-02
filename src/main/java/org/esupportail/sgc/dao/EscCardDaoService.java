package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.EscCard;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Service
public class EscCardDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public void persist(EscCard escrCard) {
        entityManager.persist(escrCard);
    }

    public TypedQuery<EscCard> findEscCardsByCardNumberEquals(String escnUid) {
        return entityManager.createQuery("SELECT o FROM EscCard o WHERE o.cardNumber = :escnUid", EscCard.class)
                .setParameter("escnUid", escnUid);
    }

    public void remove(EscCard escCard) {
        entityManager.remove(escCard);
    }
}
