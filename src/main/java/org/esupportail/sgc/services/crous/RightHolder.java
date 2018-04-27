package org.esupportail.sgc.services.crous;

import java.util.Date;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class RightHolder {
	
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
				return false;
		} else if (!birthDate.equals(other.birthDate))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (idCompanyRate == null) {
			if (other.idCompanyRate != null)
				return false;
		} else if (!idCompanyRate.equals(other.idCompanyRate))
			return false;
		if (idRate == null) {
			if (other.idRate != null)
				return false;
		} else if (!idRate.equals(other.idRate))
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (ine == null) {
			if (other.ine != null)
				return false;
		} else if (!ine.equals(other.ine))
			return false;
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

