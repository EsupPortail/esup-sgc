// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.services.crous;

import java.util.Date;
import org.esupportail.sgc.services.crous.RightHolder;

privileged aspect RightHolder_Roo_JavaBean {
    
    public String RightHolder.getIdentifier() {
        return this.identifier;
    }
    
    public void RightHolder.setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String RightHolder.getFirstName() {
        return this.firstName;
    }
    
    public void RightHolder.setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String RightHolder.getLastName() {
        return this.lastName;
    }
    
    public void RightHolder.setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String RightHolder.getEmail() {
        return this.email;
    }
    
    public void RightHolder.setEmail(String email) {
        this.email = email;
    }
    
    public void RightHolder.setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Long RightHolder.getIdCompanyRate() {
        return this.idCompanyRate;
    }
    
    public void RightHolder.setIdCompanyRate(Long idCompanyRate) {
        this.idCompanyRate = idCompanyRate;
    }
    
    public Long RightHolder.getIdRate() {
        return this.idRate;
    }
    
    public void RightHolder.setIdRate(Long idRate) {
        this.idRate = idRate;
    }
    
    public void RightHolder.setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    public String RightHolder.getIne() {
        return this.ine;
    }
    
    public void RightHolder.setIne(String ine) {
        this.ine = ine;
    }
    
    public String RightHolder.getRneOrgCode() {
        return this.rneOrgCode;
    }
    
    public void RightHolder.setRneOrgCode(String rneOrgCode) {
        this.rneOrgCode = rneOrgCode;
    }
    
    public AccountStatus RightHolder.getAccountStatus() {
        return this.accountStatus;
    }
    
    public void RightHolder.setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public BlockingStatus RightHolder.getBlockingStatus() {
        return this.blockingStatus;
    }
    
    public void RightHolder.setBlockingStatus(BlockingStatus blockingStatus) {
        this.blockingStatus = blockingStatus;
    }
    
}
