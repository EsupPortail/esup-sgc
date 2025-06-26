package org.esupportail.sgc.services.escstudentservice;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EscRemoteStudent {

	String eppn;
	
	String europeanStudentIdentifier;
	
	Long picInstitutionCode;
	
	String emailAddress;

    LocalDateTime expiryDate;
	
	String firstName;
	
	String lastName;
	
	Long academicLevel;

	List<EscRemoteStudentCard> cards;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setEuropeanStudentIdentifier(String europeanStudentIdentifier) {
		this.europeanStudentIdentifier = europeanStudentIdentifier;
		this.eppn = europeanStudentIdentifier + "@europeanstudentcard.eu";
	}


	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public String getEuropeanStudentIdentifier() {
        return this.europeanStudentIdentifier;
    }

	public Long getPicInstitutionCode() {
        return this.picInstitutionCode;
    }

	public void setPicInstitutionCode(Long picInstitutionCode) {
        this.picInstitutionCode = picInstitutionCode;
    }

	public String getEmailAddress() {
        return this.emailAddress;
    }

	public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

	public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

	public String getFirstName() {
        return this.firstName;
    }

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getLastName() {
        return this.lastName;
    }

	public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public Long getAcademicLevel() {
        return this.academicLevel;
    }

	public void setAcademicLevel(Long academicLevel) {
        this.academicLevel = academicLevel;
    }

	public List<EscRemoteStudentCard> getCards() {
        return this.cards;
    }

	public void setCards(List<EscRemoteStudentCard> cards) {
        this.cards = cards;
    }
}
