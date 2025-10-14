package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Configurable
@Entity
public class CrousRule {

	String rne;
	
	String referenceStatus;
	
	Long indiceMin;
	
	Long indiceMax;
	
	Long codeSociete;
	
	Long codeTarif;

	Long priority = 1L;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "crous_rule_config")
	CrousRuleConfig crousRuleConfig;

	@DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss")
    LocalDateTime updateDate;

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

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}
	
	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }
}
