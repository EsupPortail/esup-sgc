// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.esupportail.sgc.domain.User;

privileged aspect User_Roo_Jpa_Entity {
    
    declare @type: User: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long User.id;
    
    public Long User.getId() {
        return this.id;
    }
    
    public void User.setId(Long id) {
        this.id = id;
    }
    
}
