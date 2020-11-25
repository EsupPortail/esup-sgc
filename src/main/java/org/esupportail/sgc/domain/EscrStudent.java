package org.esupportail.sgc.domain;

import java.util.Date;

import javax.persistence.Column;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "hibernateLazyInitializer", "handler", "eppn"})
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(finders = {"findEscrStudentsByEppnEquals"})
public class EscrStudent {
	
	@Column(unique=true)
	String eppn;

	String europeanStudentIdentifier;
	
	Long picInstitutionCode;
	
	String emailAddress;
	
	Date expiryDate;
	
	String name;
	
	Long academicLevel;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getExpiryDate() {
		return expiryDate;
	}

}
