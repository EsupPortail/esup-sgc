package org.esupportail.sgc.services.esc;

import java.util.Date;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class EscrStudent {

	String europeanStudentIdentifier;
	
	Long picInstitutionCode;
	
	String emailAddress;
	
	Date expiryDate;
	
	String name;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getExpiryDate() {
		return expiryDate;
	}

}
