package org.esupportail.sgc.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class CodeBarreDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    public Integer getNextValueCodeBarreBuLyonStEtienne(){
        Integer lInteger = null;
        Long lNextValue = (Long) entityManager.createNativeQuery("SELECT NEXTVAL('seq_for_code_barre_comue_lyon_st_etienne')").getSingleResult();
        if(lNextValue != null){
            lInteger = lNextValue.intValue();
        }
        return lInteger;
    }
}
