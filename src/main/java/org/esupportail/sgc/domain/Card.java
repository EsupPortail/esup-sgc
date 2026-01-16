package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.sgc.services.CardEtatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@JsonFilter("cardFilter")
@Table(name = "Card", indexes = {
		@Index(name = "card_user_account_request_date_id", columnList = "user_account, requestDate desc"),
		@Index(name = "card_etat_desc_id", columnList = "dateEtat desc, id desc"),
		@Index(name = "card_nb_rejets_id", columnList = "nbRejets"),
        @Index(name = "card_etat", columnList = "etat"),
        @Index(name = "card_eppn_id", columnList = "eppn")
})
public class Card {
	
	private static final Logger log = LoggerFactory.getLogger(Card.class);

    public enum FlagAdresse {
        INT, EXT
    };

    public enum Etat {
        NEW, REQUEST_CHECKED, CANCELED, IN_PRINT, PRINTED, IN_ENCODE, ENCODED, ENABLED, REJECTED, DISABLED, CADUC, DESTROYED, RENEWED
    };

    public enum MotifDisable {
        LOST, THEFT, OUT_OF_ORDER;
    	public static List<String> getMotifsList(){
    		List<String>  motifsList = new ArrayList<String>();
    		 for (MotifDisable motif : MotifDisable.values()) { 
    			 motifsList.add(motif.name());
    		 }
    		return motifsList;
    	}
    };

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

    @Column
    private String eppn;

    @ElementCollection(fetch=FetchType.LAZY)
    @Column
    @CollectionTable(
            name = "card_desfire_ids",   // nom de la table jointe
            joinColumns = @JoinColumn(name = "card")  // nom exact de la colonne FK dans la table
    )
    private Map<String, String> desfireIds = new HashMap<String, String>();

    @Column(unique=true,nullable=true)
    private String csn = null;

    @Column(name = "recto1printed")
    private String recto1Printed = "";

    @Column(name = "recto2printed")
    private String recto2Printed = "";

    @Column(name = "recto3printed")
    private String recto3Printed = "";

    @Column(name = "recto4printed")
    private String recto4Printed = "";

    @Column(name = "recto5printed")
    private String recto5Printed = "";

    @Column(name = "recto6printed")
    private String recto6Printed = "";

    @Column(name = "recto7printed")
    private String recto7Printed = "";
    
    private String versoTextPrinted;

    @ManyToOne
    @JoinColumn(name = "user_account")
    private User userAccount;
    
    @Column
    @Enumerated(EnumType.STRING)
    private Etat etat = Etat.NEW;

    @Column
    private String etatEppn;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDateTime dateEtat = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column
    @Enumerated(EnumType.STRING)
    private FlagAdresse flagAdresse;

    @Column
    private String addressRequested;

    @Column
    private String structure;

    @Column
    private String payCmdNum;

    @Column
    @Enumerated(EnumType.STRING)
    private MotifDisable motifDisable;

    @Column
    private String requestBrowser;

    @Column
    private String requestOs;

    @Column
    private LocalDateTime deliveredDate = null;

    @Column
    private LocalDateTime encodedDate = null;

    @Column
    private LocalDateTime lastEncodedDate = null;

    @Column
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime ennabledDate;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "photo_file")
    private PhotoFile photoFile = new PhotoFile();

    @Column
    private Long nbRejets = Long.valueOf(0);
    
    /**
     * European Student Card Number 
     */
    @Column
    private String escnUid;
    
    @Column
    private String qrcode;
    
    @Column
    private Boolean external = false;

    @Transient
    List<Etat> etatsAvailable = new ArrayList<Etat>();
    
    @Transient
    Map<Etat, List<CardActionMessage>> cardActionMessages = new HashMap<Etat, List<CardActionMessage>>();

    @Transient
    Boolean crousTransient = false;
    
    @Transient
    Boolean europeanTransient = false;

    @Transient
    Boolean difPhotoTransient = false;

    @Transient
    Boolean isPhotoEditable = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_card")
    private TemplateCard templateCard;
    
	private String crousError;
	
	@Column(columnDefinition="TEXT")
    public String fullText;

    @Column
    public String diversDamBaseKey;

    @Column
    private String printerEppn;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "csn", referencedColumnName = "uid", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CrousSmartCard crousSmartCard;
	
	@PreUpdate
	@PrePersist
	public void updateFullText() {
		fullText = "";
		for(String desfireId : desfireIds.values()) {
			fullText += desfireId + " ";
		}
        if(StringUtils.isNotEmpty(csn)) {
            fullText += csn + " " + getReverseCsn() + " " + getDecimalCsn() + " " + getDecimalReverseCsn() + " ";
        }
        if(getCrousSmartCard() != null) {
            fullText += getCrousSmartCard().getIdZdc() + " ";
        }
		fullText += recto1Printed + " ";
		fullText += recto2Printed + " ";
		fullText += recto3Printed + " ";
		fullText += recto4Printed + " ";
		fullText += recto5Printed + " ";
		fullText += recto6Printed + " ";
		fullText += recto7Printed + " ";
		fullText += versoTextPrinted + " ";
	}

    public Long getNbRejets() {
        return nbRejets;
    }

    public void setNbRejets(Long nbRejets) {
        this.nbRejets = nbRejets;
    }

    public Boolean getIsPhotoEditable() {
        return isPhotoEditable;
    }

    public void setIsPhotoEditable(Boolean isPhotoEditable) {
        this.isPhotoEditable = isPhotoEditable;
    }

    public Boolean getUserEditable() {
        return getUser().isEditable();
    }

    public String getName() {
        return getUser().getName();
    }
    
    public String getFirstname() {
        return getUser().getFirstname();
    }

    public String getDisplayName() {
        return getUser().getDisplayName();
    }

    public LocalDateTime getBirthday() {
        return getUser().getBirthday();
    }

    public String getSupannEmpId() {
        return getUser().getSupannEmpId();
    }

    public String getSupannEtuId() {
        return getUser().getSupannEtuId();
    }

    public String getSupannCodeINE() {
        return getUser().getSupannCodeINE();
    }
    
    public User getUser() {
        return getUserAccount();
    }

    public String getUserType() {
        return getUser().getUserType();
    }

    public Long getNbCards() {
        return getUser().getNbCards();
    }

    public Boolean getCrous() {
        return getUserAccount().getCrous();
    }

    public void setCrous(Boolean crous) {
        getUserAccount().setCrous(crous);
    }

    public Boolean getDifPhoto() {
        return getUserAccount().getDifPhoto();
    }

    public void setDifPhoto(Boolean cnil) {
        getUserAccount().setDifPhoto(cnil);
    }
    
    public Boolean getEuropeanStudentCard() {
        return getUserAccount().getEuropeanStudentCard() && this.getEscnUid() != null && !this.getEscnUid().isEmpty();
    }

    public String getAddress() {
        return getUserAccount().getAddress();
    }
    
    public String getEmail() {
    	return getUserAccount().getEmail();
    }

    public String getPrinterEppn() {
        return printerEppn;
    }

    public void setPrinterEppn(String printerEppn) {
        this.printerEppn = printerEppn;
    }

    public String getFreeField1() {
        return getUserAccount().getFreeField1();
    }

    public String getFreeField2() {
        return getUserAccount().getFreeField2();
    }

    public String getFreeField3() {
        return getUserAccount().getFreeField3();
    }

    public String getFreeField4() {
        return getUserAccount().getFreeField4();
    }

    public String getFreeField5() {
        return getUserAccount().getFreeField5();
    }

    public String getFreeField6() {
        return getUserAccount().getFreeField6();
    }

    public String getFreeField7() {
        return getUserAccount().getFreeField7();
    }

    public void removeEtatAvailable(Etat etat) {
        List<Etat> etatsAvailable = new ArrayList<Etat>(getEtatsAvailable());
        etatsAvailable.remove(etat);
        setEtatsAvailable(etatsAvailable);
    }

    public String getReverseCsn() {
    	if(StringUtils.isEmpty(csn)) {
    		return null;
    	} else {
	        String csnReverse = new String();
	        for (int i = 1; i < csn.length(); i = i + 2) {
	            csnReverse = csnReverse + csn.charAt(csn.length() - i - 1) + csn.charAt(csn.length() - i);
	        }
	        return csnReverse;
    	}
    }

    public String getDecimalCsn() {
        if(StringUtils.isEmpty(csn)) {
            return null;
        } else {
            return new BigInteger(csn, 16).toString();
        }
    }

    public String getDecimalReverseCsn() {
        if(StringUtils.isEmpty(csn)) {
            return null;
        } else {
            return new BigInteger(this.getReverseCsn(), 16).toString();
        }
    }

    public boolean isEnabled() {
        return etat.equals(Etat.ENABLED);
    }
    
    public boolean isPrinted() {
        return CardEtatService.etatsPrinted.contains(etat);
    }

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public Map<String, String> getDesfireIds() {
        return this.desfireIds;
    }

	public void setDesfireIds(Map<String, String> desfireIds) {
        this.desfireIds = desfireIds;
    }

	public String getCsn() {
        return this.csn;
    }

	public void setCsn(String csn) {
        this.csn = csn;
    }

	public String getRecto1Printed() {
        return this.recto1Printed;
    }

	public void setRecto1Printed(String recto1Printed) {
        this.recto1Printed = recto1Printed;
    }

	public String getRecto2Printed() {
        return this.recto2Printed;
    }

	public void setRecto2Printed(String recto2Printed) {
        this.recto2Printed = recto2Printed;
    }

	public String getRecto3Printed() {
        return this.recto3Printed;
    }

	public void setRecto3Printed(String recto3Printed) {
        this.recto3Printed = recto3Printed;
    }

	public String getRecto4Printed() {
        return this.recto4Printed;
    }

	public void setRecto4Printed(String recto4Printed) {
        this.recto4Printed = recto4Printed;
    }

	public String getRecto5Printed() {
        return this.recto5Printed;
    }

	public void setRecto5Printed(String recto5Printed) {
        this.recto5Printed = recto5Printed;
    }

	public String getRecto6Printed() {
        return this.recto6Printed;
    }

	public void setRecto6Printed(String recto6Printed) {
        this.recto6Printed = recto6Printed;
    }

	public String getRecto7Printed() {
        return this.recto7Printed;
    }

	public void setRecto7Printed(String recto7Printed) {
        this.recto7Printed = recto7Printed;
    }

	public String getVersoTextPrinted() {
        return this.versoTextPrinted;
    }

	public void setVersoTextPrinted(String versoTextPrinted) {
        this.versoTextPrinted = versoTextPrinted;
    }

	public User getUserAccount() {
        return this.userAccount;
    }

	public void setUserAccount(User userAccount) {
        this.userAccount = userAccount;
    }

	public Etat getEtat() {
        return this.etat;
    }

	public void setEtat(Etat etat) {
        this.etat = etat;
    }

	public String getEtatEppn() {
        return this.etatEppn;
    }

	public void setEtatEppn(String etatEppn) {
        this.etatEppn = etatEppn;
    }

	public LocalDateTime getRequestDate() {
        return this.requestDate;
    }

	public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

	public LocalDateTime getDateEtat() {
        return this.dateEtat;
    }

	public void setDateEtat(LocalDateTime dateEtat) {
        this.dateEtat = dateEtat;
    }

	public String getCommentaire() {
        return this.commentaire;
    }

	public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

	public FlagAdresse getFlagAdresse() {
        return this.flagAdresse;
    }

	public void setFlagAdresse(FlagAdresse flagAdresse) {
        this.flagAdresse = flagAdresse;
    }

	public String getAddressRequested() {
        return this.addressRequested;
    }

	public void setAddressRequested(String addressRequested) {
        this.addressRequested = addressRequested;
    }

	public String getStructure() {
        return this.structure;
    }

	public void setStructure(String structure) {
        this.structure = structure;
    }

	public String getPayCmdNum() {
        return this.payCmdNum;
    }

	public void setPayCmdNum(String payCmdNum) {
        this.payCmdNum = payCmdNum;
    }

	public MotifDisable getMotifDisable() {
        return this.motifDisable;
    }

	public void setMotifDisable(MotifDisable motifDisable) {
        this.motifDisable = motifDisable;
    }

	public String getRequestBrowser() {
        return this.requestBrowser;
    }

	public void setRequestBrowser(String requestBrowser) {
        this.requestBrowser = requestBrowser;
    }

	public String getRequestOs() {
        return this.requestOs;
    }

	public void setRequestOs(String requestOs) {
        this.requestOs = requestOs;
    }

	public LocalDateTime getDeliveredDate() {
        return this.deliveredDate;
    }

	public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

	public LocalDateTime getEncodedDate() {
        return this.encodedDate;
    }

	public void setEncodedDate(LocalDateTime encodedDate) {
        this.encodedDate = encodedDate;
    }

	public LocalDateTime getLastEncodedDate() {
        return this.lastEncodedDate;
    }

	public void setLastEncodedDate(LocalDateTime lastEncodedDate) {
        this.lastEncodedDate = lastEncodedDate;
    }

	public LocalDateTime getDueDate() {
        return this.dueDate;
    }

	public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

	public LocalDateTime getEnnabledDate() {
        return this.ennabledDate;
    }

	public void setEnnabledDate(LocalDateTime ennabledDate) {
        this.ennabledDate = ennabledDate;
    }

	public PhotoFile getPhotoFile() {
        return this.photoFile;
    }

	public void setPhotoFile(PhotoFile photoFile) {
        this.photoFile = photoFile;
    }

	public String getEscnUid() {
        return this.escnUid;
    }

	public void setEscnUid(String escnUid) {
        this.escnUid = escnUid;
    }

	public String getQrcode() {
        return this.qrcode;
    }

	public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

	public Boolean getExternal() {
        return this.external;
    }

	public void setExternal(Boolean external) {
        this.external = external;
    }

	public List<Etat> getEtatsAvailable() {
        return this.etatsAvailable;
    }

	public void setEtatsAvailable(List<Etat> etatsAvailable) {
        this.etatsAvailable = etatsAvailable;
    }

	public Map<Etat, List<CardActionMessage>> getCardActionMessages() {
        return this.cardActionMessages;
    }

	public void setCardActionMessages(Map<Etat, List<CardActionMessage>> cardActionMessages) {
        this.cardActionMessages = cardActionMessages;
    }

	public Boolean getCrousTransient() {
        return this.crousTransient;
    }

	public void setCrousTransient(Boolean crousTransient) {
        this.crousTransient = crousTransient;
    }

	public Boolean getEuropeanTransient() {
        return this.europeanTransient;
    }

	public void setEuropeanTransient(Boolean europeanTransient) {
        this.europeanTransient = europeanTransient;
    }

	public Boolean getDifPhotoTransient() {
        return this.difPhotoTransient;
    }

	public void setDifPhotoTransient(Boolean difPhotoTransient) {
        this.difPhotoTransient = difPhotoTransient;
    }

	public TemplateCard getTemplateCard() {
        return this.templateCard;
    }

	public void setTemplateCard(TemplateCard templateCard) {
        this.templateCard = templateCard;
    }

	public String getCrousError() {
        return this.crousError;
    }

	public void setCrousError(String crousError) {
        this.crousError = crousError;
    }

	public String getFullText() {
        return this.fullText;
    }

	public void setFullText(String fullText) {
        this.fullText = fullText;
    }

	public String getDiversDamBaseKey() {
        return this.diversDamBaseKey;
    }

	public void setDiversDamBaseKey(String diversDamBaseKey) {
        this.diversDamBaseKey = diversDamBaseKey;
    }


    public String getEscnUidAsHexa() {
        return getEscnUidAsHexa(getEscnUid());
    }

    static public String getEscnUidAsHexa(String escnWithDash) {
        return escnWithDash.replaceAll("-", "");
    }

    static public String getEscnWithDash(String escnHexa) {
        return String.format("%s-%s-%s-%s-%s", escnHexa.substring(0, 8), escnHexa.substring(8, 12), escnHexa.substring(12, 16), escnHexa.substring(16, 20), escnHexa.substring(20, 32));
    }

    public CrousSmartCard getCrousSmartCard() {
        return crousSmartCard;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

