// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import java.util.Date;
import org.esupportail.sgc.domain.PayboxTransactionLog;

privileged aspect PayboxTransactionLog_Roo_JavaBean {
    
    public Date PayboxTransactionLog.getTransactionDate() {
        return this.transactionDate;
    }
    
    public void PayboxTransactionLog.setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String PayboxTransactionLog.getEppn() {
        return this.eppn;
    }
    
    public void PayboxTransactionLog.setEppn(String eppn) {
        this.eppn = eppn;
    }
    
    public String PayboxTransactionLog.getReference() {
        return this.reference;
    }
    
    public void PayboxTransactionLog.setReference(String reference) {
        this.reference = reference;
    }
    
    public String PayboxTransactionLog.getMontant() {
        return this.montant;
    }
    
    public void PayboxTransactionLog.setMontant(String montant) {
        this.montant = montant;
    }
    
    public String PayboxTransactionLog.getAuto() {
        return this.auto;
    }
    
    public void PayboxTransactionLog.setAuto(String auto) {
        this.auto = auto;
    }
    
    public String PayboxTransactionLog.getErreur() {
        return this.erreur;
    }
    
    public void PayboxTransactionLog.setErreur(String erreur) {
        this.erreur = erreur;
    }
    
    public String PayboxTransactionLog.getIdtrans() {
        return this.idtrans;
    }
    
    public void PayboxTransactionLog.setIdtrans(String idtrans) {
        this.idtrans = idtrans;
    }
    
    public String PayboxTransactionLog.getSignature() {
        return this.signature;
    }
    
    public void PayboxTransactionLog.setSignature(String signature) {
        this.signature = signature;
    }
    
}
