package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;

@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
public class CrousRule {

	String rne;
	
	String referenceStatus;
	
	Long indiceMin;
	
	Long indiceMax;
	
	Long codeSociete;
	
	Long codeTarif;

	Long priority = 1L;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	CrousRuleConfig crousRuleConfig;

	@DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss")
	Date updateDate;

	public String getRne() {
		return rne;
	}

	public void setRne(String rne) {
		this.rne = rne;
	}

	public String getReferenceStatus() {
		return referenceStatus;
	}

	@JsonProperty("statusCode")
	public void setReferenceStatus(String referenceStatus) {
		this.referenceStatus = referenceStatus;
	}

	public Long getIndiceMin() {
		return indiceMin;
	}

	@JsonProperty("indexMin")
	public void setIndiceMin(Long indiceMin) {
		this.indiceMin = indiceMin;
	}

	public Long getIndiceMax() {
		return indiceMax;
	}

	@JsonProperty("indexMax")
	public void setIndiceMax(Long indiceMax) {
		this.indiceMax = indiceMax;
	}

	public Long getCodeSociete() {
		return codeSociete;
	}

	@JsonProperty("idCompanyRate")
	public void setCodeSociete(Long codeSociete) {
		this.codeSociete = codeSociete;
	}

	public Long getCodeTarif() {
		return codeTarif;
	}

	@JsonProperty("idRate")
	public void setCodeTarif(Long codeTarif) {
		this.codeTarif = codeTarif;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	public CrousRuleConfig getCrousRuleConfig() {
		return crousRuleConfig;
	}
	public void setCrousRuleConfig(CrousRuleConfig crousRuleConfig) {
		this.crousRuleConfig = crousRuleConfig;
	}

	public Date getUpdateDate() {
		return updateDate;
	}
	
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public static List<CrousRule> findAllCrousRules(CrousRuleConfig crousRuleConfig) {
		String jpaQuery = "SELECT o FROM CrousRule o where o.crousRuleConfig=:crousRuleConfig";
		return entityManager().createQuery(jpaQuery, CrousRule.class)
				.setParameter("crousRuleConfig", crousRuleConfig)
				.getResultList();
	}

	public static List<CrousRule> findAllCrousRulesApi() {
		return entityManager().createQuery("SELECT o FROM CrousRule o where o.crousRuleConfig!= null", CrousRule.class).getResultList();
	}

	public static List<CrousRule> findAllCrousRulesCustom() {
		return entityManager().createQuery("SELECT o FROM CrousRule o where o.crousRuleConfig = null", CrousRule.class).getResultList();
	}

}
