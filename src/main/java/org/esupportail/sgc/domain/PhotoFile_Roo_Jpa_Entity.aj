// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import org.esupportail.sgc.domain.PhotoFile;

privileged aspect PhotoFile_Roo_Jpa_Entity {
    
    declare @type: PhotoFile: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long PhotoFile.id;
    
    @Version
    @Column(name = "version")
    private Integer PhotoFile.version;
    
    public Long PhotoFile.getId() {
        return this.id;
    }
    
    public void PhotoFile.setId(Long id) {
        this.id = id;
    }
    
    public Integer PhotoFile.getVersion() {
        return this.version;
    }
    
    public void PhotoFile.setVersion(Integer version) {
        this.version = version;
    }
    
}
