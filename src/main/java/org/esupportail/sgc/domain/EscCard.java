package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EscCard {

	public enum CardType {UNKNOWN, PASSIVE, SMART_NO_CDZ, SMART_CDZ, SMART_MAY_SP, SMART_PASSIVE, SMART_PASSIVE_EMULATION};

	public enum CardStatusType{ACTIVE, INACTIVE};

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
	@Column(name = "id")
	private Long id;

	String cardNumber;

	@Enumerated(EnumType.STRING)
	CardStatusType cardStatusType;

	@Enumerated(EnumType.STRING)
	CardType cardType;

    LocalDateTime expiresAt;

    LocalDateTime issuedAt;

	String issuerIdentifier;

	String personIdentifier;

	String processorIdentifier;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public CardStatusType getCardStatusType() {
		return cardStatusType;
	}

	public void setCardStatusType(CardStatusType cardStatusType) {
		this.cardStatusType = cardStatusType;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	public LocalDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(LocalDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public String getIssuerIdentifier() {
		return issuerIdentifier;
	}

	public void setIssuerIdentifier(String issuerIdentifier) {
		this.issuerIdentifier = issuerIdentifier;
	}

	public String getPersonIdentifier() {
		return personIdentifier;
	}

	public void setPersonIdentifier(String personIdentifier) {
		this.personIdentifier = personIdentifier;
	}

	public String getProcessorIdentifier() {
   		return processorIdentifier;
   	}

    public void setProcessorIdentifier(String processorIdentifier) {
    		this.processorIdentifier = processorIdentifier;
   	}

	@Override
	public String toString() {
		return "EscCard{" +
				"id=" + id +
				", cardNumber='" + cardNumber + '\'' +
				", cardStatusType=" + cardStatusType +
				", cardType=" + cardType +
				", expiresAt=" + expiresAt +
				", issuedAt=" + issuedAt +
				", issuerIdentifier='" + issuerIdentifier + '\'' +
				", personIdentifier='" + personIdentifier + '\'' +
				'}';
	}
}
