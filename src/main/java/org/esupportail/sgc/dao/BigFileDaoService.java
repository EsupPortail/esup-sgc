package org.esupportail.sgc.dao;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.BigFile;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.hibernate.Hibernate;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class BigFileDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("log", "binaryFile", "md5");
    
    @PersistenceContext
    transient EntityManager entityManager;


    public BigFile findBigFile(Long id) {
        if (id == null) return null;
        return entityManager.find(BigFile.class, id);
    }

    public void setBinaryFileStream(BigFile bigFile, InputStream inputStream, long length) {
        byte[] buffer = new byte[(int)length];
        try {
            IOUtils.readFully(inputStream, buffer);
            this.setBinaryFile(bigFile, buffer);
        } catch (IOException e) {
            throw new SgcRuntimeException("Error saving binary file", e);
        }
    }

    public void setBinaryFile(BigFile bigFile, byte[] bytes) {
        LobHelper helper = Hibernate.getLobHelper();
        bigFile.setBinaryFile(helper.createBlob(bytes));
        bigFile.updateMd5();
    }

}
