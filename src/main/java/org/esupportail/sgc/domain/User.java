package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.sgc.security.SgcRoleHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@JsonFilter("userFilter")
@Table(name = "UserAccount", indexes = {
		@Index(name = "user_account_nb_cards_id", columnList = "nbCards"),
		@Index(name = "user_account_user_type_id", columnList = "userType"),
		@Index(name = "user_account_address_id", columnList = "address asc"),
		@Index(name = "user_account_last_card_template_printed", columnList = "last_card_template_printed"),
		@Index(name = "editable_id", columnList = "editable")
})
public class User {
	
	private final static Logger log = LoggerFactory.getLogger(User.class);

	public enum CnousReferenceStatut {
		psg, etd, prs, hbg, fct, fpa, stg, ctr, hb2, hb3, hb4, hb5, hb6, po, rtr;
	};
	
	public final static List<String> BOOLEAN_FIELDS = Arrays.asList(new String[] {"crous", "europeanStudentCard", "difPhoto", "editable", "requestFree", "hasCardRequestPending"});

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

	@JsonInclude
    @Column(unique=true)
    private String eppn;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "userAccount")
    @Column
    @OrderBy("requestDate DESC")
    private List<Card> cards = new ArrayList<Card>();
    
	@Column
	private Boolean crous = false;
	
	private String crousError;
	
	private String crousIdentifier;
	
	@Column
	private Boolean europeanStudentCard = false;
	
	@Column
	private Boolean difPhoto = false;

	private String name;
	
	private String firstname;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private LocalDateTime birthday;
	
	private String institute;
	
	private String eduPersonPrimaryAffiliation;
	
	private String email;
	
	private String rneEtablissement = "";
	
	@Column
	@Enumerated(EnumType.STRING)
	private CnousReferenceStatut cnousReferenceStatut = CnousReferenceStatut.psg;
	
	private Long indice = Long.valueOf(0);
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private LocalDateTime dueDate;
	
	private Long idCompagnyRate;
	
	private Long idRate;
	
	private String supannEmpId = "";
	
	private String supannEtuId = "";
	
	private String supannEntiteAffectationPrincipale = "";
	
	private String supannCodeINE = "";
	
	private String secondaryId = "";
	
	private String recto1 = "";
	
	private String recto2 = "";
	
	private String recto3 = "";
	
	private String recto4 = "";
	
	private String recto5 = "";
	
	private String recto6 = "";
	
	private String recto7 = "";
	
	private String verso1 = "";
	
	private String verso2 = "";
	
	private String verso3 = "";
	
	private String verso4 = "";
	
	private String verso5 = "";
	
	private String verso6 = "";
	
	private String verso7 = "";
	
	private boolean editable = true;
	
	private boolean requestFree = true;
	
	private String address = "";
	
	private String externalAddress = "";
	
	private String freeField1 = "";
	
	private String freeField2 = "";
	
	private String freeField3 = "";

	private String freeField4 = "";

	private String freeField5 = "";

	private String freeField6 = "";

	private String freeField7 = "";

	private Long nbCards = 0L;
	
	private String userType;
	
	private String templateKey;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_card_template_printed")
	private TemplateCard lastCardTemplatePrinted;

	@ElementCollection
    @CollectionTable(name="roles", joinColumns=@JoinColumn(name="user_account"), indexes = {
    		@Index(name="roles_role_id", columnList = "role"), @Index(name="roles_user_account_id", columnList = "user_account")})
    @Column(name="role")
    private Set<String> roles = new HashSet<String>();
	
	@Transient
	private Card externalCard = new Card();
	
	private String blockUserMsg;
	
	@Column
	private Boolean hasCardRequestPending = false;
	
	private Long academicLevel;
	
	@Transient
	private Boolean importExtCardRight = true;
	
	@Transient
	private Boolean newCardRight = true;
	
	@Transient
	private Boolean viewRight = true;
	
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "default_photo", nullable = false)
    private PhotoFile defaultPhoto = new PhotoFile();
    
	@Column(columnDefinition="TEXT")
	private String fullText;
	
	private String pic = "";

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd@HH:mm:ss.SSSZ")
	private LocalDateTime updateDate;
	
	@PreUpdate
	@PrePersist
	public void updateFullText() {
		fullText = "";
		fullText += StringUtils.join(getVersoText(), " ");
		fullText += getInstitute() + " ";
		fullText += getRneEtablissement() + " ";
		fullText += getSecondaryId() + " ";
		fullText += getSupannCodeINE() + " ";
		fullText += getRecto1() + " ";
		fullText += getRecto2() + " ";
		fullText += getRecto3() + " ";
		fullText += getRecto4() + " ";
		fullText += getRecto5() + " ";
		fullText += getRecto6() + " ";
		fullText += getRecto7() + " ";
		fullText += getFreeField1() + " ";
		fullText += getFreeField2() + " ";
		fullText += getFreeField3() + " ";
		fullText += getFreeField4() + " ";
		fullText += getFreeField5() + " ";
		fullText += getFreeField6() + " ";
		fullText += getFreeField7() + " ";
	}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getDisplayName() {
		return getName() + " " + getFirstname();
	}
	
	public Boolean getHasExternalCard() {
		for(Card card : cards) {
			if(card.getExternal()) {
				return true;
			}
		}
		return false;
	}
	
	public Card getEnabledCard() {
		for(Card card : cards) {
			if(card.isEnabled()) {
				return card;
			}
		}
		return null;
	}
	

	public Set<String> getReachableRoles() {
		return SgcRoleHierarchy.getReachableRoles(this.getRoles());
	}
	
	public List<String> getVersoText() {
		String[] versoText = new String[] {verso1, verso2, verso3, verso4, verso5, verso6, verso7};
		return Arrays.asList(versoText);
	}

	public List<String> getFieldNotEquals(Object obj) {
		List<String> fieldsNotEquals = new ArrayList<String>();
		if (this == obj) {
			return fieldsNotEquals;
		}
		if (obj == null) {
			fieldsNotEquals.add("null");
			return fieldsNotEquals;
		}
		if (getClass() != obj.getClass()) {
			fieldsNotEquals.add("class");
			return fieldsNotEquals;
		}
		User other = (User) obj;

		if (birthday == null) {
			if (other.birthday != null) {
				fieldsNotEquals.add("birthday");
			}
		} 
		// compare only day (without time) for birthday 
		else if (!birthday.equals(other.birthday)) {
			fieldsNotEquals.add("birthday");
		}

		if (dueDate == null) {
			if (other.dueDate != null) {
				fieldsNotEquals.add("dueDate");
			}
		} else if (!dueDate.equals(other.dueDate)) {
			fieldsNotEquals.add("dueDate");
		}
		
		if (externalCard == null) {
			if (other.externalCard != null) {
				fieldsNotEquals.add("externalCard");
			}
		} else if (!externalCard.equals(other.externalCard)) {
			fieldsNotEquals.add("externalCard");
		}
		
		if (this.getDefaultPhoto() == null || this.getDefaultPhoto().getBigFile() == null || this.getDefaultPhoto().getBigFile().getMd5() == null) {
			if (!(other.getDefaultPhoto() == null || other.getDefaultPhoto().getBigFile() == null || other.getDefaultPhoto().getBigFile().getMd5() == null)) {
				fieldsNotEquals.add("defaultPhoto");
			}
		} else if (other.getDefaultPhoto() == null || other.getDefaultPhoto().getBigFile() == null || !this.getDefaultPhoto().getBigFile().getMd5().equals(other.getDefaultPhoto().getBigFile().getMd5())) {
			fieldsNotEquals.add("defaultPhoto");
		}
		
		for(String varStringName : Arrays.asList(new String[] {"address", "externalAddress", "cnousReferenceStatut", "crous", "difPhoto", "editable", "eduPersonPrimaryAffiliation", "email",
				"eppn", "europeanStudentCard", "firstname", "idCompagnyRate", "indice", "institute", "name", 
				"recto1", "recto2", "recto3", "recto4", "recto5", "recto6", "recto7", 
				"freeField1", "freeField2", "freeField3", "freeField4", "freeField5", "freeField6", "freeField7",
				"requestFree", "rneEtablissement", "secondaryId", "supannCodeINE", "supannEmpId", "supannEntiteAffectationPrincipale", "supannEtuId", "userType", 
				"verso1", "verso2", "verso3", "verso4", "verso5", "verso6", "verso7", 
				"templateKey", "blockUserMsg", "academicLevel", "pic"})) {
			if(!this.checkEqualsVar(varStringName, other)) {
				fieldsNotEquals.add(varStringName);
			}
		}
		
		// hack idRate pour student : si idCompagnyRate == 10 (student), idRate pas pris en compte pour getFieldNotEquals
		if (idRate == null) {
			if (other.idRate != null) {
				fieldsNotEquals.add("idRate");
			}
		} else if(idCompagnyRate==null || other.idCompagnyRate==null || !idCompagnyRate.equals(other.idCompagnyRate) || !idCompagnyRate.equals(Long.valueOf(10))) {
			if(!this.checkEqualsVar("idRate", other)) {
				fieldsNotEquals.add("idRate");
			}
		}
		return fieldsNotEquals;
	}

	
	private boolean checkEqualsVar(String varStringName, User other) {	
		boolean isEquals = true;
		try {
			Field f = User.class.getDeclaredField(varStringName);
			f.setAccessible(true);

			Object thisVarObj = f.get(this);
			Object otherVarObj = f.get(other);

			if (thisVarObj == null && otherVarObj != null) {
				if(!otherVarObj.toString().isEmpty()) {
					isEquals = false;
				}
			}
			if(thisVarObj != null && otherVarObj == null) {
				if(!thisVarObj.toString().isEmpty()) {
					isEquals = false;
				}
			}
			if (thisVarObj != null && otherVarObj != null) {
				String thisVar = thisVarObj.toString();
				String otherVar = otherVarObj.toString();
				thisVar = thisVar.toLowerCase();
				otherVar = otherVar.toLowerCase();
				thisVar = StringUtils.stripAccents(thisVar);
				otherVar = StringUtils.stripAccents(otherVar);
				if (!thisVar.equals(otherVar)) {
					isEquals = false;
				}
			}
			if(!isEquals) {
				log.debug(String.format("Users [%s] not synchronized because %s is not synchronized : %s <> %s", this.getEppn(), varStringName, thisVarObj, otherVarObj));
			}
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			log.warn(String.format("Error when trying to compare %s for %s ans %s [%s]", varStringName, this, other, this.getEppn()));
		}
		return isEquals;
	}

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public List<Card> getCards() {
        return this.cards;
    }

	public void setCards(List<Card> cards) {
        this.cards = cards;
    }

	public Boolean getCrous() {
        return this.crous;
    }

	public void setCrous(Boolean crous) {
        this.crous = crous;
    }

	public String getCrousError() {
        return this.crousError;
    }

	public void setCrousError(String crousError) {
        this.crousError = crousError;
    }

	public String getCrousIdentifier() {
        return this.crousIdentifier;
    }

	public void setCrousIdentifier(String crousIdentifier) {
        this.crousIdentifier = crousIdentifier;
    }

	public Boolean getEuropeanStudentCard() {
        return this.europeanStudentCard;
    }

	public void setEuropeanStudentCard(Boolean europeanStudentCard) {
        this.europeanStudentCard = europeanStudentCard;
    }

	public Boolean getDifPhoto() {
        return this.difPhoto;
    }

	public void setDifPhoto(Boolean difPhoto) {
        this.difPhoto = difPhoto;
    }

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getFirstname() {
        return this.firstname;
    }

	public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

	public LocalDateTime getBirthday() {
        return this.birthday;
    }

	public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

	public String getInstitute() {
        return this.institute;
    }

	public void setInstitute(String institute) {
        this.institute = institute;
    }

	public String getEduPersonPrimaryAffiliation() {
        return this.eduPersonPrimaryAffiliation;
    }

	public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
        this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public String getRneEtablissement() {
        return this.rneEtablissement;
    }

	public void setRneEtablissement(String rneEtablissement) {
        this.rneEtablissement = rneEtablissement;
    }

	public CnousReferenceStatut getCnousReferenceStatut() {
        return this.cnousReferenceStatut;
    }

	public void setCnousReferenceStatut(CnousReferenceStatut cnousReferenceStatut) {
        this.cnousReferenceStatut = cnousReferenceStatut;
    }

	public Long getIndice() {
        return this.indice;
    }

	public void setIndice(Long indice) {
        this.indice = indice;
    }

	public LocalDateTime getDueDate() {
        return this.dueDate;
    }

	public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

	public Long getIdCompagnyRate() {
        return this.idCompagnyRate;
    }

	public void setIdCompagnyRate(Long idCompagnyRate) {
        this.idCompagnyRate = idCompagnyRate;
    }

	public Long getIdRate() {
        return this.idRate;
    }

	public void setIdRate(Long idRate) {
        this.idRate = idRate;
    }

	public String getSupannEmpId() {
        return this.supannEmpId;
    }

	public void setSupannEmpId(String supannEmpId) {
        this.supannEmpId = supannEmpId;
    }

	public String getSupannEtuId() {
        return this.supannEtuId;
    }

	public void setSupannEtuId(String supannEtuId) {
        this.supannEtuId = supannEtuId;
    }

	public String getSupannEntiteAffectationPrincipale() {
        return this.supannEntiteAffectationPrincipale;
    }

	public void setSupannEntiteAffectationPrincipale(String supannEntiteAffectationPrincipale) {
        this.supannEntiteAffectationPrincipale = supannEntiteAffectationPrincipale;
    }

	public String getSupannCodeINE() {
        return this.supannCodeINE;
    }

	public void setSupannCodeINE(String supannCodeINE) {
        this.supannCodeINE = supannCodeINE;
    }

	public String getSecondaryId() {
        return this.secondaryId;
    }

	public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

	public String getRecto1() {
        return this.recto1;
    }

	public void setRecto1(String recto1) {
        this.recto1 = recto1;
    }

	public String getRecto2() {
        return this.recto2;
    }

	public void setRecto2(String recto2) {
        this.recto2 = recto2;
    }

	public String getRecto3() {
        return this.recto3;
    }

	public void setRecto3(String recto3) {
        this.recto3 = recto3;
    }

	public String getRecto4() {
        return this.recto4;
    }

	public void setRecto4(String recto4) {
        this.recto4 = recto4;
    }

	public String getRecto5() {
        return this.recto5;
    }

	public void setRecto5(String recto5) {
        this.recto5 = recto5;
    }

	public String getRecto6() {
        return this.recto6;
    }

	public void setRecto6(String recto6) {
        this.recto6 = recto6;
    }

	public String getRecto7() {
        return this.recto7;
    }

	public void setRecto7(String recto7) {
        this.recto7 = recto7;
    }

	public String getVerso1() {
        return this.verso1;
    }

	public void setVerso1(String verso1) {
        this.verso1 = verso1;
    }

	public String getVerso2() {
        return this.verso2;
    }

	public void setVerso2(String verso2) {
        this.verso2 = verso2;
    }

	public String getVerso3() {
        return this.verso3;
    }

	public void setVerso3(String verso3) {
        this.verso3 = verso3;
    }

	public String getVerso4() {
        return this.verso4;
    }

	public void setVerso4(String verso4) {
        this.verso4 = verso4;
    }

	public String getVerso5() {
        return this.verso5;
    }

	public void setVerso5(String verso5) {
        this.verso5 = verso5;
    }

	public String getVerso6() {
        return this.verso6;
    }

	public void setVerso6(String verso6) {
        this.verso6 = verso6;
    }

	public String getVerso7() {
        return this.verso7;
    }

	public void setVerso7(String verso7) {
        this.verso7 = verso7;
    }

	public boolean isEditable() {
        return this.editable;
    }

	public void setEditable(boolean editable) {
        this.editable = editable;
    }

	public boolean isRequestFree() {
        return this.requestFree;
    }

	public void setRequestFree(boolean requestFree) {
        this.requestFree = requestFree;
    }

	public String getAddress() {
        return this.address;
    }

	public void setAddress(String address) {
        this.address = address;
    }

	public String getExternalAddress() {
        return this.externalAddress;
    }

	public void setExternalAddress(String externalAddress) {
        this.externalAddress = externalAddress;
    }

	public String getFreeField1() {
        return this.freeField1;
    }

	public void setFreeField1(String freeField1) {
        this.freeField1 = freeField1;
    }

	public String getFreeField2() {
        return this.freeField2;
    }

	public void setFreeField2(String freeField2) {
        this.freeField2 = freeField2;
    }

	public String getFreeField3() {
        return this.freeField3;
    }

	public void setFreeField3(String freeField3) {
        this.freeField3 = freeField3;
    }

	public String getFreeField4() {
        return this.freeField4;
    }

	public void setFreeField4(String freeField4) {
        this.freeField4 = freeField4;
    }

	public String getFreeField5() {
        return this.freeField5;
    }

	public void setFreeField5(String freeField5) {
        this.freeField5 = freeField5;
    }

	public String getFreeField6() {
        return this.freeField6;
    }

	public void setFreeField6(String freeField6) {
        this.freeField6 = freeField6;
    }

	public String getFreeField7() {
        return this.freeField7;
    }

	public void setFreeField7(String freeField7) {
        this.freeField7 = freeField7;
    }

	public Long getNbCards() {
        return this.nbCards;
    }

	public void setNbCards(Long nbCards) {
        this.nbCards = nbCards;
    }

	public String getUserType() {
        return this.userType;
    }

	public void setUserType(String userType) {
        this.userType = userType;
    }

	public String getTemplateKey() {
        return this.templateKey;
    }

	public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

	public TemplateCard getLastCardTemplatePrinted() {
        return this.lastCardTemplatePrinted;
    }

	public void setLastCardTemplatePrinted(TemplateCard lastCardTemplatePrinted) {
        this.lastCardTemplatePrinted = lastCardTemplatePrinted;
    }

	public Set<String> getRoles() {
        return this.roles;
    }

	public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

	public Card getExternalCard() {
        return this.externalCard;
    }

	public void setExternalCard(Card externalCard) {
        this.externalCard = externalCard;
    }

	public String getBlockUserMsg() {
        return this.blockUserMsg;
    }

	public void setBlockUserMsg(String blockUserMsg) {
        this.blockUserMsg = blockUserMsg;
    }

	public Boolean getHasCardRequestPending() {
        return this.hasCardRequestPending;
    }

	public void setHasCardRequestPending(Boolean hasCardRequestPending) {
        this.hasCardRequestPending = hasCardRequestPending;
    }

	public Long getAcademicLevel() {
        return this.academicLevel;
    }

	public void setAcademicLevel(Long academicLevel) {
        this.academicLevel = academicLevel;
    }

	public Boolean getImportExtCardRight() {
        return this.importExtCardRight;
    }

	public void setImportExtCardRight(Boolean importExtCardRight) {
        this.importExtCardRight = importExtCardRight;
    }

	public Boolean getNewCardRight() {
        return this.newCardRight;
    }

	public void setNewCardRight(Boolean newCardRight) {
        this.newCardRight = newCardRight;
    }

	public Boolean getViewRight() {
        return this.viewRight;
    }

	public void setViewRight(Boolean viewRight) {
        this.viewRight = viewRight;
    }

	public PhotoFile getDefaultPhoto() {
        return this.defaultPhoto;
    }

	public void setDefaultPhoto(PhotoFile defaultPhoto) {
        this.defaultPhoto = defaultPhoto;
    }

	public String getFullText() {
        return this.fullText;
    }

	public void setFullText(String fullText) {
        this.fullText = fullText;
    }

	public String getPic() {
        return this.pic;
    }

	public void setPic(String pic) {
        this.pic = pic;
    }

	public LocalDateTime getUpdateDate() {
        return this.updateDate;
    }

	public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }


	public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("cards").toString();
    }
}

