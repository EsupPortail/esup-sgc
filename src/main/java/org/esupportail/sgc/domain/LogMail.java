package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class LogMail {

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

    @ManyToOne
    @JoinColumn(name = "card_action_message")
    private CardActionMessage cardActionMessage;

    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    private LocalDateTime logDate;

    private String eppn;

    private String subject;

    private String mailTo;

    @Column(columnDefinition = "TEXT")
    private String message;

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

    public CardActionMessage getCardActionMessage() {
        return this.cardActionMessage;
    }

    public void setCardActionMessage(CardActionMessage cardActionMessage) {
        this.cardActionMessage = cardActionMessage;
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

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailTo() {
        return this.mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public Long getCardActionMessageId() {
        return getCardActionMessage()!=null ? getCardActionMessage().getId() : null;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
