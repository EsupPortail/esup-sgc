package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EsupNfcTagLog {
	
	String csn;
	
	String eppn;
	
	String lastname;
	
	String firstname;
	
	String location;
	
	String eppnInit;
	
	public String getEppnInit() {
		return eppnInit;
	}

	public void setEppnInit(String eppnInit) {
		this.eppnInit = eppnInit;
	}

	public String getCsn() {
		return csn;
	}

	public void setCsn(String csn) {
		this.csn = csn;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "TagLog [csn=" + csn + ", eppn=" + eppn + ", lastname=" + lastname + ", firstname=" + firstname + "]";
	}
	
}