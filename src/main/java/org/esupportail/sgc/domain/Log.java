package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Log {

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

    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    private LocalDateTime logDate;

    private String eppn;

    private String eppnCible;

    private String type;

    private Long cardId;

    private String action;

    private String retCode;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String remoteAddress;

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

	public LocalDateTime getLogDate() {
        return this.logDate;
    }

	public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public String getEppnCible() {
        return this.eppnCible;
    }

	public void setEppnCible(String eppnCible) {
        this.eppnCible = eppnCible;
    }

	public String getType() {
        return this.type;
    }

	public void setType(String type) {
        this.type = type;
    }

	public Long getCardId() {
        return this.cardId;
    }

	public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

	public String getAction() {
        return this.action;
    }

	public void setAction(String action) {
        this.action = action;
    }

	public String getRetCode() {
        return this.retCode;
    }

	public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

	public String getComment() {
        return this.comment;
    }

	public void setComment(String comment) {
        this.comment = comment;
    }

	public String getRemoteAddress() {
        return this.remoteAddress;
    }

	public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
