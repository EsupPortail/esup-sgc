package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;
import org.esupportail.sgc.repositories.EsupNfcSgcJwsDeviceRepository;
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
public class EsupNfcSgcJwsDeviceDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("eppnInit", "numeroId");

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    EsupNfcSgcJwsDeviceRepository esupNfcSgcJwsDeviceRepository;

    public TypedQuery<EsupNfcSgcJwsDevice> findEsupNfcSgcJwsDevicesByEppnInitEquals(String eppnInit) {
        if (eppnInit == null || eppnInit.length() == 0) throw new IllegalArgumentException("The eppnInit argument is required");
        EntityManager em = entityManager;
        TypedQuery<EsupNfcSgcJwsDevice> q = em.createQuery("SELECT o FROM EsupNfcSgcJwsDevice AS o WHERE o.eppnInit = :eppnInit", EsupNfcSgcJwsDevice.class);
        q.setParameter("eppnInit", eppnInit);
        return q;
    }

    public EsupNfcSgcJwsDevice findEsupNfcSgcJwsDevice(Long id) {
        if (id == null) return null;
        return entityManager.find(EsupNfcSgcJwsDevice.class, id);
    }

    public Page<EsupNfcSgcJwsDevice> findEsupNfcSgcJwsDeviceEntries(Pageable pageable) {
        return esupNfcSgcJwsDeviceRepository.findAll(pageable);
    }

    @Transactional
    public void persist(EsupNfcSgcJwsDevice esupNfcSgcJwsDevice) {
        this.entityManager.persist(esupNfcSgcJwsDevice);
    }

    @Transactional
    public void remove(EsupNfcSgcJwsDevice esupNfcSgcJwsDevice) {
        if (this.entityManager.contains(esupNfcSgcJwsDevice)) {
            this.entityManager.remove(esupNfcSgcJwsDevice);
        } else {
            EsupNfcSgcJwsDevice attached = findEsupNfcSgcJwsDevice(esupNfcSgcJwsDevice.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public EsupNfcSgcJwsDevice merge(EsupNfcSgcJwsDevice esupNfcSgcJwsDevice) {
        EsupNfcSgcJwsDevice merged = this.entityManager.merge(esupNfcSgcJwsDevice);
        this.entityManager.flush();
        return merged;
    }
}
