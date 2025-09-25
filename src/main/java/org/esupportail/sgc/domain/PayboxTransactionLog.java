package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class PayboxTransactionLog {

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

    @DateTimeFormat(style = "MM")
    private LocalDateTime transactionDate;

    private String eppn;

    @Column(unique=true)
    private String reference;

    private String montant;

    private String auto;

    private String erreur;

    private String idtrans;

    private String signature;

    public String getMontantDevise() {
        double mnt = Double.parseDouble(montant) / 100.0;
        return Double.toString(mnt);
    }

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

	public LocalDateTime getTransactionDate() {
        return this.transactionDate;
    }

	public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public String getReference() {
        return this.reference;
    }

	public void setReference(String reference) {
        this.reference = reference;
    }

	public String getMontant() {
        return this.montant;
    }

	public void setMontant(String montant) {
        this.montant = montant;
    }

	public String getAuto() {
        return this.auto;
    }

	public void setAuto(String auto) {
        this.auto = auto;
    }

	public String getErreur() {
        return this.erreur;
    }

	public void setErreur(String erreur) {
        this.erreur = erreur;
    }

	public String getIdtrans() {
        return this.idtrans;
    }

	public void setIdtrans(String idtrans) {
        this.idtrans = idtrans;
    }

	public String getSignature() {
        return this.signature;
    }

	public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getMontantInt() {
        return Integer.parseInt(this.montant);
    }
}
