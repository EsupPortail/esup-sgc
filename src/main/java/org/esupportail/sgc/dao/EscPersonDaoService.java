package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.EscPerson;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EscPersonDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public TypedQuery<EscPerson> findEscPersonsByEppnEquals(String eppn) {
        return entityManager.createQuery("SELECT o FROM EscPerson o WHERE o.eppn = :eppn", EscPerson.class)
                .setParameter("eppn", eppn);
    }

    public TypedQuery<EscPerson> findEscPersonsByIdenfifierEquals(String identifier) {
        return entityManager.createQuery("SELECT o FROM EscPerson o WHERE o.identifier = :identifier", EscPerson.class)
                .setParameter("identifier", identifier);
    }

    public void persist(EscPerson escPerson) {
        entityManager.persist(escPerson);
    }

    public EscPerson findOneEscPerson4test() {
        List<EscPerson> escPeopleOne = entityManager.createQuery("SELECT o FROM EscPerson o", EscPerson.class).setMaxResults(1).getResultList();
        if(escPeopleOne.isEmpty()) {
            return null;
        } else {
            return escPeopleOne.get(0);
        }
    }

    @Transactional
    public void remove(EscPerson singleResult) {
        entityManager.remove(singleResult);
    }
}
