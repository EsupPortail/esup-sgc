package org.esupportail.sgc.services.crous;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class PatchIdentifier {

	String currentIdentifier;
	 
	String email;
	
	String newIdentifier;
			

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getCurrentIdentifier() {
        return this.currentIdentifier;
    }

	public void setCurrentIdentifier(String currentIdentifier) {
        this.currentIdentifier = currentIdentifier;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public String getNewIdentifier() {
        return this.newIdentifier;
    }

	public void setNewIdentifier(String newIdentifier) {
        this.newIdentifier = newIdentifier;
    }
}
