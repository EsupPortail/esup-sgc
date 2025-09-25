package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Configurable
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "card", "userAccount", "date", "crousOperation", "esupSgcOperation" })
public class CrousErrorLog {

    public static enum CrousOperation {
    	GET, PUT, POST, PATCH, DELETE
    };
    
    public static enum EsupSgcOperation {
    	ACTIVATE, DESACTIVATE, SYNC, PATCH, GET, UNCLOSE
    };
    
	@OneToOne
    @JoinColumn(name = "card")
    Card card;

	@OneToOne
    @JoinColumn(name = "user_account")
    User userAccount;

    String code;

    String message;

    String field;

    LocalDateTime date;
    
    Boolean blocking;
    
    @Column
    @Enumerated(EnumType.STRING)
    private CrousOperation crousOperation;
    
    @Column
    @Enumerated(EnumType.STRING)
    private EsupSgcOperation esupSgcOperation;
    
    private String crousUrl;

    Integer tryCount = 1;
    
    public Long getCardId() {
    	if(card!=null) {
    		return card.getId();
    	}
    	return null;
    }
    
    public String getCardCsn() {
    	if(card!=null) {
    		return card.getCsn();
    	}
    	return null;
    }
    
    public String getUserEppn() {
    	if(userAccount!=null) {
    		return userAccount.getEppn();
    	}
    	return null;
    }
    
    public String getUserDisplayName() {
    	if(userAccount!=null) {
    		return userAccount.getDisplayName();
    	}
    	return null;
    }
    
    public String getUserEmail() {
    	if(userAccount!=null) {
    		return userAccount.getEmail();
    	}
    	return null;
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

	public Card getCard() {
        return this.card;
    }

	public void setCard(Card card) {
        this.card = card;
    }

	public User getUserAccount() {
        return this.userAccount;
    }

	public void setUserAccount(User userAccount) {
        this.userAccount = userAccount;
    }

	public String getCode() {
        return this.code;
    }

	public void setCode(String code) {
        this.code = code;
    }

	public String getMessage() {
        return this.message;
    }

	public void setMessage(String message) {
        this.message = message;
    }

	public String getField() {
        return this.field;
    }

	public void setField(String field) {
        this.field = field;
    }

	public LocalDateTime getDate() {
        return this.date;
    }

	public void setDate(LocalDateTime date) {
        this.date = date;
    }

	public Boolean getBlocking() {
        return this.blocking;
    }

	public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }

	public CrousOperation getCrousOperation() {
        return this.crousOperation;
    }

	public void setCrousOperation(CrousOperation crousOperation) {
        this.crousOperation = crousOperation;
    }

	public EsupSgcOperation getEsupSgcOperation() {
        return this.esupSgcOperation;
    }

	public void setEsupSgcOperation(EsupSgcOperation esupSgcOperation) {
        this.esupSgcOperation = esupSgcOperation;
    }

	public String getCrousUrl() {
        return this.crousUrl;
    }

	public void setCrousUrl(String crousUrl) {
        this.crousUrl = crousUrl;
    }

    public Integer getTryCount() {
        return this.tryCount;
    }

    public void setTryCount(Integer tryCount) {
        this.tryCount = tryCount;
    }
}
