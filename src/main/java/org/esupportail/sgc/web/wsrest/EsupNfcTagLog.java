package org.esupportail.sgc.web.wsrest;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsupNfcTagLog {
	
	public static String SALLE_ENCODAGE = "Encodage ESUP SGC";
	public static String SALLE_LIVRAISON = "Livraison ESUP SGC";
	public static String SALLE_SEARCH = "Recherche ESUP SGC";
	public static String SALLE_UPDATE = "Mise à jour ESUP SGC";
	public static String VERSO_CARTE = "Verso carte";
	public static String SECONDARY_ID = "Identifiant secondaire";
	public static String SALLE_DESTROY = "Destruction de la carte";
	
    String desfireId;
    
	String csn;
	
	String eppn;
	
	String lastname;
	
	String firstname;
	
	String eppnInit;
	
	String location;
	
	String applicationName;

	@Override
	public String toString() {
		return "TagLog [location=" + location + ", csn=" + csn + ", eppn=" + eppn + ", lastname=" + lastname + ", firstname=" + firstname + "]";
	}
	

	public String getCsn() {
        return this.csn;
    }

	public void setCsn(String csn) {
        this.csn = csn;
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public String getLastname() {
        return this.lastname;
    }

	public void setLastname(String lastname) {
        this.lastname = lastname;
    }

	public String getFirstname() {
        return this.firstname;
    }

	public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

	public String getEppnInit() {
        return this.eppnInit;
    }

	public void setEppnInit(String eppnInit) {
        this.eppnInit = eppnInit;
    }

	public String getLocation() {
        return this.location;
    }

	public void setLocation(String location) {
        this.location = location;
    }

	public String getDesfireId() {
		return desfireId;
	}

	public void setDesfireId(String desfireId) {
		this.desfireId = desfireId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}