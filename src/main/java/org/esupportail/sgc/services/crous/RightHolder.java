package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

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

    LocalDateTime dueDate;

	Long idCompanyRate;

	Long idRate;

    String simpleBirthDate;

	String ine;

	String rneOrgCode;
	
	AccountStatus accountStatus;
	
	BlockingStatus blockingStatus;

	// Format attendu par beforeizly avec un 'Z' (temps UTC) à la fin
	// -> mis en dur dans le pattern même si pas de gestion des fuseaux
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	public LocalDateTime getDueDate() {
		return dueDate;
	}


	boolean checkEqualsVar(String varStringName, RightHolder other) {	
		boolean isEquals = true;
		try {
			Field f = RightHolder.class.getDeclaredField(varStringName);
			f.setAccessible(true);

			Object thisVarObj = f.get(this);
			Object otherVarObj = f.get(other);

			if (thisVarObj == null && otherVarObj != null && !otherVarObj.toString().isEmpty() || thisVarObj != null && !thisVarObj.toString().isEmpty() && otherVarObj == null) {
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


	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getIdentifier() {
        return this.identifier;
    }

	public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

	public Long getIdCompanyRate() {
        return this.idCompanyRate;
    }

	public void setIdCompanyRate(Long idCompanyRate) {
        this.idCompanyRate = idCompanyRate;
    }

	public Long getIdRate() {
        return this.idRate;
    }

	public void setIdRate(Long idRate) {
        this.idRate = idRate;
    }

	public String getIne() {
        return this.ine;
    }

	public void setIne(String ine) {
        this.ine = ine;
    }

	public String getRneOrgCode() {
        return this.rneOrgCode;
    }

	public void setRneOrgCode(String rneOrgCode) {
        this.rneOrgCode = rneOrgCode;
    }

	public AccountStatus getAccountStatus() {
        return this.accountStatus;
    }

	public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

	public BlockingStatus getBlockingStatus() {
        return this.blockingStatus;
    }

	public void setBlockingStatus(BlockingStatus blockingStatus) {
        this.blockingStatus = blockingStatus;
    }

	public String getSimpleBirthDate() {
		return simpleBirthDate;
	}

	public void setSimpleBirthDate(String simpleBirthDate) {
		this.simpleBirthDate = simpleBirthDate;
	}
}

