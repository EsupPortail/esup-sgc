// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import java.util.Date;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.LogMail;

privileged aspect LogMail_Roo_JavaBean {
    
    public CardActionMessage LogMail.getCardActionMessage() {
        return this.cardActionMessage;
    }
    
    public void LogMail.setCardActionMessage(CardActionMessage cardActionMessage) {
        this.cardActionMessage = cardActionMessage;
    }
    
    public Date LogMail.getLogDate() {
        return this.logDate;
    }
    
    public void LogMail.setLogDate(Date logDate) {
        this.logDate = logDate;
    }
    
    public String LogMail.getEppn() {
        return this.eppn;
    }
    
    public void LogMail.setEppn(String eppn) {
        this.eppn = eppn;
    }
    
    public String LogMail.getSubject() {
        return this.subject;
    }
    
    public void LogMail.setSubject(String subject) {
        this.subject = subject;
    }
    
    public String LogMail.getMailTo() {
        return this.mailTo;
    }
    
    public void LogMail.setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
    
    public String LogMail.getMessage() {
        return this.message;
    }
    
    public void LogMail.setMessage(String message) {
        this.message = message;
    }
    
}
