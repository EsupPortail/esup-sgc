package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.repositories.CrousSmartCardRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

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

    public Page<CrousSmartCard> findCrousSmartCardEntries(CrousSmartCard searchCrousSmartCard, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withMatcher("uid", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("idTransmitter", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("idZdc", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<CrousSmartCard> crousSmartCardSearchQuery = Example.of(searchCrousSmartCard, matcher);
        return crousSmartCardRepository.findAll(crousSmartCardSearchQuery, pageable);
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
