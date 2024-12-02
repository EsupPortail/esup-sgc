package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.EscPerson;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class EscPersonDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public TypedQuery<EscPerson> findEscPersonsByEppnEquals(String eppn) {
        return entityManager.createQuery("SELECT o FROM EscPerson o WHERE o.eppn = :eppn", EscPerson.class)
                .setParameter("eppn", eppn);
    }

    public void persist(EscPerson escPerson) {
        entityManager.persist(escPerson);
    }

    public List<EscPerson> findAllEscPersons() {
        return entityManager.createQuery("SELECT o FROM EscPerson o", EscPerson.class).getResultList();
    }

    public void remove(EscPerson singleResult) {
        entityManager.remove(singleResult);
    }
}
