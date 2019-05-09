package org.esupportail.sgc.services.escstudentservice;

import java.util.Date;
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class EscRemoteStudent {

	String eppn;
	
	String europeanStudentIdentifier;
	
	Long picInstitutionCode;
	
	String emailAddress;
	
	Date expiryDate;
	
	String firstName;
	
	String lastName;
	
	Long academicLevel;

	List<EscRemoteStudentCard> cards;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setEuropeanStudentIdentifier(String europeanStudentIdentifier) {
		this.europeanStudentIdentifier = europeanStudentIdentifier;
		this.eppn = europeanStudentIdentifier + "@europeanstudentcard.eu";
	}

}
