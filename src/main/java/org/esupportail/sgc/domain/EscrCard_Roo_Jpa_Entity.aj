// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import org.esupportail.sgc.domain.EscrCard;

privileged aspect EscrCard_Roo_Jpa_Entity {
    
    declare @type: EscrCard: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long EscrCard.id;
    
    @Version
    @Column(name = "version")
    private Integer EscrCard.version;
    
    public Long EscrCard.getId() {
        return this.id;
    }
    
    public void EscrCard.setId(Long id) {
        this.id = id;
    }
    
    public Integer EscrCard.getVersion() {
        return this.version;
    }
    
    public void EscrCard.setVersion(Integer version) {
        this.version = version;
    }
    
}
