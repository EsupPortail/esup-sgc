package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.repositories.CrousSmartCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrousSmartCardDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("idTransmitter", "idMapping", "idZdc", "zdcCreationDate", "pixSs", "pixNn", "appl", "uid", "rid");
    
    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    CrousSmartCardRepository crousSmartCardRepository;

    public CrousSmartCard findCrousSmartCard(String uid) {
        CrousSmartCard smartCard = null;
        List<CrousSmartCard> smartCards = findCrousSmartCardsByUidEquals(uid).getResultList();
        if(!smartCards.isEmpty()) {
            smartCard = smartCards.get(0);
        }
        return smartCard;
    }


    public CrousSmartCard findCrousSmartCard(Long id) {
        if (id == null) return null;
        return entityManager.find(CrousSmartCard.class, id);
    }

    public Page<CrousSmartCard> findCrousSmartCardEntries(Pageable pageable) {
        return crousSmartCardRepository.findAll(pageable);
    }

    @Transactional
    public void persist(CrousSmartCard crousSmartCard) {
        this.entityManager.persist(crousSmartCard);
    }

    @Transactional
    public void remove(CrousSmartCard crousSmartCard) {
        if (this.entityManager.contains(crousSmartCard)) {
            this.entityManager.remove(crousSmartCard);
        } else {
            CrousSmartCard attached = findCrousSmartCard(crousSmartCard.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public CrousSmartCard merge(CrousSmartCard crousSmartCard) {
        CrousSmartCard merged = this.entityManager.merge(crousSmartCard);
        this.entityManager.flush();
        return merged;
    }

    public TypedQuery<CrousSmartCard> findCrousSmartCardsByUidEquals(String uid) {
        if (uid == null || uid.length() == 0) throw new IllegalArgumentException("The uid argument is required");
        EntityManager em = entityManager;
        TypedQuery<CrousSmartCard> q = em.createQuery("SELECT o FROM CrousSmartCard AS o WHERE o.uid = :uid", CrousSmartCard.class);
        q.setParameter("uid", uid);
        return q;
    }


}
