// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import org.esupportail.sgc.domain.Prefs;

privileged aspect Prefs_Roo_Jpa_Entity {
    
    declare @type: Prefs: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long Prefs.id;
    
    @Version
    @Column(name = "version")
    private Integer Prefs.version;
    
    public Long Prefs.getId() {
        return this.id;
    }
    
    public void Prefs.setId(Long id) {
        this.id = id;
    }
    
    public Integer Prefs.getVersion() {
        return this.version;
    }
    
    public void Prefs.setVersion(Integer version) {
        this.version = version;
    }
    
}
