package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class CrousPatchIdentifier {

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

	@Column(unique = true)
	String oldId;
	
	@Column(unique = true)
	String eppnNewId;
	
	@Column(unique = true)
	String mail;
	
	Boolean patchSuccess;

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

	public String getOldId() {
        return this.oldId;
    }

	public void setOldId(String oldId) {
        this.oldId = oldId;
    }

	public String getEppnNewId() {
        return this.eppnNewId;
    }

	public void setEppnNewId(String eppnNewId) {
        this.eppnNewId = eppnNewId;
    }

	public String getMail() {
        return this.mail;
    }

	public void setMail(String mail) {
        this.mail = mail;
    }

	public Boolean getPatchSuccess() {
        return this.patchSuccess;
    }

	public void setPatchSuccess(Boolean patchSuccess) {
        this.patchSuccess = patchSuccess;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}

