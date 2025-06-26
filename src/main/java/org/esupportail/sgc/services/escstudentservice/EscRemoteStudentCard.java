package org.esupportail.sgc.services.escstudentservice;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EscRemoteStudentCard {

	String europeanStudentCardNumber;
	
	Long cardType;
	
	String cardUid;
	

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getEuropeanStudentCardNumber() {
        return this.europeanStudentCardNumber;
    }

	public void setEuropeanStudentCardNumber(String europeanStudentCardNumber) {
        this.europeanStudentCardNumber = europeanStudentCardNumber;
    }

	public Long getCardType() {
        return this.cardType;
    }

	public void setCardType(Long cardType) {
        this.cardType = cardType;
    }

	public String getCardUid() {
        return this.cardUid;
    }

	public void setCardUid(String cardUid) {
        this.cardUid = cardUid;
    }
}
