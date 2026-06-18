package org.esupportail.sgc.dao;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.BigFile;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.hibernate.Hibernate;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class BigFileDaoService {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("log", "binaryFile", "md5");

    Logger log = LoggerFactory.getLogger(BigFileDaoService.class);
    
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

    public long countOrphanLargeObjects() {
        String sql = "SELECT count(*)\n" +
                "    FROM pg_largeobject_metadata lo\n" +
                "    WHERE NOT EXISTS (\n" +
                "        SELECT 1 FROM big_file bf WHERE bf.binary_file = lo.oid\n" +
                "    )";
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    @PostConstruct
    public List<String> showTriggersOnBigFileTable() {
        List<String> triggers = new ArrayList<>();
        String sql = """
            select trigger_name,
                   event_manipulation,
                   action_timing,
                   action_statement
            from information_schema.triggers
            where event_object_table = 'big_file'
        """;

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        rows.forEach(row -> triggers.add("Trigger name: " + row[0] + ", Event: " + row[1] + ", Timing: " + row[2] + ", Statement: " + row[3]
        ));
        if(triggers.isEmpty()) {
            log.error("Les triggers sur la table big_file n'ont pas été positionnés. Merci de les mettre en place pour éviter d'avoir de très nombreux large objects orphelins en base prenant un espace très conséquent");
        }
        return triggers;
    }

}
