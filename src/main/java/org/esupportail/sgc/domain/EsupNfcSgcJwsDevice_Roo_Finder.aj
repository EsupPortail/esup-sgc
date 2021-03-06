// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;

privileged aspect EsupNfcSgcJwsDevice_Roo_Finder {
    
    public static Long EsupNfcSgcJwsDevice.countFindEsupNfcSgcJwsDevicesByEppnInitEquals(String eppnInit) {
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = EsupNfcSgcJwsDevice.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EsupNfcSgcJwsDevice AS o WHERE o.eppnInit = :eppnInit", Long.class);
        q.setParameter("eppnInit", eppnInit);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<EsupNfcSgcJwsDevice> EsupNfcSgcJwsDevice.findEsupNfcSgcJwsDevicesByEppnInitEquals(String eppnInit) {
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = EsupNfcSgcJwsDevice.entityManager();
        TypedQuery<EsupNfcSgcJwsDevice> q = em.createQuery("SELECT o FROM EsupNfcSgcJwsDevice AS o WHERE o.eppnInit = :eppnInit", EsupNfcSgcJwsDevice.class);
        q.setParameter("eppnInit", eppnInit);
        return q;
    }
    
    public static TypedQuery<EsupNfcSgcJwsDevice> EsupNfcSgcJwsDevice.findEsupNfcSgcJwsDevicesByEppnInitEquals(String eppnInit, String sortFieldName, String sortOrder) {
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = EsupNfcSgcJwsDevice.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM EsupNfcSgcJwsDevice AS o WHERE o.eppnInit = :eppnInit");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<EsupNfcSgcJwsDevice> q = em.createQuery(queryBuilder.toString(), EsupNfcSgcJwsDevice.class);
        q.setParameter("eppnInit", eppnInit);
        return q;
    }
    
}
