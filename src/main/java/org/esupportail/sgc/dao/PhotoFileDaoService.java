package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.PhotoFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
public class PhotoFileDaoService {
    
    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("filename", "imageData", "file", "sendTime", "fileSize", "contentType", "bigFile");

    public List<PhotoFile> findAllPhotoFiles() {
        return entityManager.createQuery("SELECT o FROM PhotoFile o", PhotoFile.class).getResultList();
    }

    public PhotoFile findPhotoFile(Long id) {
        if (id == null) return null;
        return entityManager.find(PhotoFile.class, id);
    }

    @Transactional
    public void persist(PhotoFile photoFile) {
        this.entityManager.persist(photoFile);
    }

    @Transactional
    public void remove(PhotoFile photoFile) {
        if (this.entityManager.contains(photoFile)) {
            this.entityManager.remove(photoFile);
        } else {
            PhotoFile attached = findPhotoFile(photoFile.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public PhotoFile merge(PhotoFile photoFile) {
        PhotoFile merged = this.entityManager.merge(photoFile);
        this.entityManager.flush();
        return merged;
    }
    
}
