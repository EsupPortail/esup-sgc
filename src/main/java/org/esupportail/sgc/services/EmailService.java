package org.esupportail.sgc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class EmailService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    private transient MailSender mailSender; 
    
    private Boolean isEnabled = false;
  
    	
    public void setMailSender(MailSender mailSender) {
    	this.mailSender = mailSender;
    }

	public void setIsEnabled(Boolean isEnabled) {
    	this.isEnabled = isEnabled;
    }
	
	public boolean sendMessage(String mailFrom, String mailTo, String subject, String mailMessage) {
	    
	    SimpleMailMessage mail = new SimpleMailMessage();  
	    
		if(this.isEnabled) {
	   	try {
	    		mail.setFrom(mailFrom);
			    mail.setTo(mailTo);
			    mail.setSubject(subject);
			    mail.setText(mailMessage);
		        mailSender.send(mail);
		        log.debug("Email sent : " + mail.toString());
		        //logService.logMail(mailTo, mailMessage, LogService.MAIL_SENT);
	    	} catch(Exception e) {   		
		        log.error("Email failed : " + mail.toString(), e);
		        //logService.logMail(mailTo, mailMessage, LogService.MAIL_FAILED);
		        return false;
	    	}
		} else {
			log.warn("sendMessage called but email is not enabled ...");
			log.info("\tmethod call was :  sendMessage(" + mailFrom + ", " + mailTo + ", " + subject + ", " + mailMessage + ")");
		}
		return true;
    }
	
	//avec copie à
	public boolean sendMessageCc(String mailFrom, String mailTo,String Cc, String subject, String mailMessage) {
		
		SimpleMailMessage mail = new SimpleMailMessage();  
		
		if(this.isEnabled) {
	   	try {
	    		mail.setFrom(mailFrom);
			    mail.setTo(mailTo);
			    mail.setSubject(subject);
			    mail.setText(mailMessage);
			    mail.setCc(Cc);
		        mailSender.send(mail);
		        
		        log.debug("Email sent : " + mail.toString());
		        //logService.logMail(mailTo, mailMessage, LogService.MAIL_SENT);
	    	} catch(Exception e) {   		
		        log.error("Email failed : " + mail.toString(), e);
		        //logService.logMail(mailTo, mailMessage, LogService.MAIL_FAILED);
		        return false;
	    	}
		} else {
			log.warn("sendMessage called but email is not enabled ...");
			log.info("\tmethod call was :  sendMessage(" + mailFrom + ", " + mailTo +", "+Cc+ ", " + subject + ", " + mailMessage + ")");
		}
		return true;
    }
}
