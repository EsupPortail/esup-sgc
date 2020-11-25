package org.esupportail.sgc.services.crous;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@RooToString
@RooJavaBean
@JsonIgnoreProperties(ignoreUnknown=true)
public class RightHolder {

	private final static Logger log = LoggerFactory.getLogger(RightHolder.class);
	
	public enum AccountStatus {
		DUMMY, NON_CONFIRME, ACTIF, CLOTURE, CLOTURE_EN_COURS;
	}  

	public enum BlockingStatus {
		DUMMY, NON_BLOQUE, BLOCAGE_PAR_3_MOTS_DE_PASSE_ERRONNES, MIS_EN_OPPOSITION, COMPTE_GELE;
	}  
	
	String identifier;

	String firstName;

	String lastName;

	String email;

	Date dueDate;

	Long idCompanyRate;

	Long idRate;

	Date birthDate;

	String ine;

	String rneOrgCode;
	
	AccountStatus accountStatus;
	
	BlockingStatus blockingStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getDueDate() {
		return dueDate;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone="CET")
	public Date getBirthDate() {
		return birthDate;
	}

	boolean checkEqualsVar(String varStringName, RightHolder other) {	
		boolean isEquals = true;
		try {
			Field f = RightHolder.class.getDeclaredField(varStringName);
			f.setAccessible(true);

			Object thisVarObj = f.get(this);
			Object otherVarObj = f.get(other);

			if (thisVarObj == null && otherVarObj != null || thisVarObj != null && otherVarObj == null) {
				isEquals = false;
			}
			if (thisVarObj != null && otherVarObj != null) {
				String thisVar = thisVarObj.toString();
				String otherVar = otherVarObj.toString();
				thisVar = thisVar.toLowerCase();
				otherVar = otherVar.toLowerCase();
				thisVar = StringUtils.stripAccents(thisVar);
				otherVar = StringUtils.stripAccents(otherVar);
				if (!thisVar.equals(otherVar)) {
					isEquals = false;
				}
			}
			if(!isEquals) {
				log.info(String.format("RightHolders not equals because %s is not equals : %s <> %s", varStringName, thisVarObj, otherVarObj));
			}
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			log.warn(String.format("Error when trying to compare %s for %s ans %s", varStringName, this, other));
		}
		return isEquals;
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

