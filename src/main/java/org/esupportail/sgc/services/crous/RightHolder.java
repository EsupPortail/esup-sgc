package org.esupportail.sgc.services.crous;

import java.util.Date;

import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class RightHolder {
	
	private final static Logger log = LoggerFactory.getLogger(RightHolder.class);
	
	/**** required part ****/
	
	String identifier;
	
	String firstName;
	
	String lastName;
	
	String email;
	
	Date dueDate;
	
	Long idCompanyRate;
	
	Long idRate;
	
	Date birthDate;
	
	String ine;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getDueDate() {
		return dueDate;
	}
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getBirthDate() {
		return birthDate;
	}

	public boolean fieldWoDueDateEquals(RightHolder obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RightHolder other = (RightHolder) obj;
		if (birthDate == null) {
			if (other.birthDate != null)
				{log.trace("birthDate <>"); return false;}
		} 
		// compare only day (without time) for birthday 
		else if (DateTimeComparator.getDateOnlyInstance().compare(birthDate, other.birthDate)!=0)
			{log.trace("birthDate <>"); return false;}
		if (email == null) {
			if (other.email != null)
				{log.trace("email <>"); return false;}
		} else if (!email.equals(other.email))
			{log.trace("email <>"); return false;}
		if (firstName == null) {
			if (other.firstName != null)
				{log.trace("firstName <>"); return false;}
		} else if (!firstName.equals(other.firstName))
			{log.trace("firstName <>"); return false;}
		if (idCompanyRate == null) {
			if (other.idCompanyRate != null)
				{log.trace("idCompanyRate <>"); return false;}
		} else if (!idCompanyRate.equals(other.idCompanyRate))
			{log.trace("idCompanyRate <>"); return false;}
		if (idRate == null) {
			if (other.idRate != null)
				{log.trace("idRate <>"); return false;}
		} else if (!idRate.equals(other.idRate))
			{log.trace("idRate <>"); return false;}
		if (identifier == null) {
			if (other.identifier != null)
				{log.trace("identifier <>"); return false;}
		} else if (!identifier.equals(other.identifier))
			{log.trace("identifier <>"); return false;}
		if (lastName == null) {
			if (other.lastName != null)
				{log.trace("lastName <>"); return false;}
		} else if (!lastName.equals(other.lastName))
			{log.trace("lastName <>"); return false;}
		if (ine == null) {
			if (other.ine != null)
				{log.trace("ine <>"); return false;}
		} else if (!ine.equals(other.ine))
			{log.trace("ine <>"); return false;}
		return true;
	}
	

	/**** optional part ****/
	
	
	/*
	String rneOrgCode;
	
	String rneDepCode;
	
	String internalId;

	String secondaryEmail;
	
	String cellNumber;
	
	String address1;
	
	String address2;
	
	String address3;
	
	String zipCode;
	
	String city;
	
	String country;
	
	String other1;
	
	String other2;
	
	String other3;
	
	String other4;
	
	String other5;
	
	String other6;
	
	String other7;
	
	String other8;
	
	String other9;
	
	String other10;
	
	String changeRateDate;
	
	Long futurIdCompanyRate;
	
	Long futureRate;
	
	Boolean student = false;
	
	Long idCrous;

	
	*/
	
}

