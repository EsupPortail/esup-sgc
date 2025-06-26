package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "hibernateLazyInitializer", "handler", "crousSmartCardIdGenerator"})
public class CrousSmartCard {

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

    Long idTransmitter;

    Long idMapping;

    @Column(unique = true)
    Long idZdc;

    LocalDateTime zdcCreationDate;

    String pixSs;

    String pixNn;

    String appl;

    @Column(unique = true, nullable=false)
    String uid;

    String rid;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public LocalDateTime getZdcCreationDate() {
        return zdcCreationDate;
    }

	public Long getIdTransmitter() {
        return this.idTransmitter;
    }

	public void setIdTransmitter(Long idTransmitter) {
        this.idTransmitter = idTransmitter;
    }

	public Long getIdMapping() {
        return this.idMapping;
    }

	public void setIdMapping(Long idMapping) {
        this.idMapping = idMapping;
    }

	public Long getIdZdc() {
        return this.idZdc;
    }

	public void setIdZdc(Long idZdc) {
        this.idZdc = idZdc;
    }

	public void setZdcCreationDate(LocalDateTime zdcCreationDate) {
        this.zdcCreationDate = zdcCreationDate;
    }

	public String getPixSs() {
        return this.pixSs;
    }

	public void setPixSs(String pixSs) {
        this.pixSs = pixSs;
    }

	public String getPixNn() {
        return this.pixNn;
    }

	public void setPixNn(String pixNn) {
        this.pixNn = pixNn;
    }

	public String getAppl() {
        return this.appl;
    }

	public void setAppl(String appl) {
        this.appl = appl;
    }

	public String getUid() {
        return this.uid;
    }

	public void setUid(String uid) {
        this.uid = uid;
    }

	public String getRid() {
        return this.rid;
    }

	public void setRid(String rid) {
        this.rid = rid;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
