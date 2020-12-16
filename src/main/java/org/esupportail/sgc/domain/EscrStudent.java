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

	public void updateWith(EscrStudent escrStudentGoal) {
		this.setAcademicLevel(escrStudentGoal.getAcademicLevel());
		this.setEmailAddress(escrStudentGoal.getEmailAddress());
		this.setEppn(escrStudentGoal.getEppn());
		this.setEuropeanStudentIdentifier(escrStudentGoal.getEuropeanStudentIdentifier());
		this.setExpiryDate(escrStudentGoal.getExpiryDate());
		this.setName(escrStudentGoal.getName());
		this.setPicInstitutionCode(escrStudentGoal.getPicInstitutionCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EscrStudent other = (EscrStudent) obj;
		if (academicLevel == null) {
			if (other.academicLevel != null)
				return false;
		} else if (!academicLevel.equals(other.academicLevel))
			return false;
		if (emailAddress == null) {
			if (other.emailAddress != null)
				return false;
		} else if (!emailAddress.equals(other.emailAddress))
			return false;
		if (eppn == null) {
			if (other.eppn != null)
				return false;
		} else if (!eppn.equals(other.eppn))
			return false;
		if (europeanStudentIdentifier == null) {
			if (other.europeanStudentIdentifier != null)
				return false;
		} else if (!europeanStudentIdentifier.equals(other.europeanStudentIdentifier))
			return false;
		if (expiryDate == null) {
			if (other.expiryDate != null)
				return false;
		} else if (!expiryDate.equals(other.expiryDate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (picInstitutionCode == null) {
			if (other.picInstitutionCode != null)
				return false;
		} else if (!picInstitutionCode.equals(other.picInstitutionCode))
			return false;
		return true;
	}


}
