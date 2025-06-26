package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Configurable
@Entity
public class CrousRuleConfig {

	String rne;
	
	String numeroCrous;

	Long priority = 0L;


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

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getRne() {
        return this.rne;
    }

	public void setRne(String rne) {
        this.rne = rne;
    }

	public String getNumeroCrous() {
        return this.numeroCrous;
    }

	public void setNumeroCrous(String numeroCrous) {
        this.numeroCrous = numeroCrous;
    }

	public Long getPriority() {
        return this.priority;
    }

	public void setPriority(Long priority) {
        this.priority = priority;
    }
}
