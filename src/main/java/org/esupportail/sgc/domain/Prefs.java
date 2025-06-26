package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Prefs {

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

    @Column
    private String eppn;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime dateModification;

    @Column(columnDefinition="TEXT")
    private String value;

    @Column
    private String key;

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

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public LocalDateTime getDateModification() {
        return this.dateModification;
    }

	public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

	public String getValue() {
        return this.value;
    }

	public void setValue(String value) {
        this.value = value;
    }

	public String getKey() {
        return this.key;
    }

	public void setKey(String key) {
        this.key = key;
    }


	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
