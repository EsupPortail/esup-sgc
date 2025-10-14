package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name="appli_version")
public class AppliVersion {

	String esupSgcVersion;

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

	public String getEsupSgcVersion() {
        return this.esupSgcVersion;
    }

	public void setEsupSgcVersion(String esupSgcVersion) {
        this.esupSgcVersion = esupSgcVersion;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
