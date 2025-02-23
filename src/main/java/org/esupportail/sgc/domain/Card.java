package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "Card", finders = { "findCardsByEppnEquals", "findCardsByEppnAndEtatEquals", "findCardsByEppnLike", "findCardsByEtatEqualsAndDateDemandeLessThan", "findCardsByDesfireId", "findCardsByCsn", "findCardsByEppnAndEtatNotEquals",  "findCardsByEtatAndUserTypeAndDateEtatLessThan"})
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

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "eppn", "crous", "etat", "dateEtat", "commentaire", "flagAdresse", "adresse", "structure", "requestDate", "nbRejets", "lastEncodedDate", "dueDate");

    public static enum FlagAdresse {
        INT, EXT
    };

    public static enum Etat {
        NEW, REQUEST_CHECKED, CANCELED, IN_PRINT, PRINTED, IN_ENCODE, ENCODED, ENABLED, REJECTED, DISABLED, CADUC, DESTROYED, RENEWED
    };

    public static enum MotifDisable {
    	
        LOST, THEFT, OUT_OF_ORDER;
        
    	public static List<String> getMotifsList(){
    		List<String>  motifsList = new ArrayList<String>();
    		 for (MotifDisable motif : MotifDisable.values()) { 
    			 motifsList.add(motif.name());
    		 }
    		return motifsList;
    		
    	}
        
    };

    @Column
    private String eppn;

    @ElementCollection(fetch=FetchType.LAZY)
    @Column
    private Map<String, String> desfireIds = new HashMap<String, String>();

    @Column(unique=true,nullable=true)
    private String csn = null;

    private String recto1Printed = "";

    private String recto2Printed = "";

    private String recto3Printed = "";

    private String recto4Printed = "";

    private String recto5Printed = "";

    private String recto6Printed = "";

    private String recto7Printed = "";
    
    private String versoTextPrinted;

    @ManyToOne
    private User userAccount;
    
    @Column
    @Enumerated(EnumType.STRING)
    private Etat etat = Etat.NEW;

    @Column
    private String etatEppn;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date requestDate = new Date();

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private Date dateEtat = new Date();

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
    private Date deliveredDate = null;

    @Column
    private Date encodedDate = null;

    @Column
    private Date lastEncodedDate = null;

    @Column
    private Date dueDate;

    @Column
    private Date ennabledDate;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
    private TemplateCard templateCard;
    
	private String crousError;
	
	@Column(columnDefinition="TEXT")
    public String fullText;

    @Column
    public String diversDamBaseKey;

    @Column
    private String printerEppn;
	
	@PreUpdate
	@PrePersist
	public void updateFullText() {
		fullText = "";
		for(String desfireId : desfireIds.values()) {
			fullText += desfireId + " ";
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

    public Date getBirthday() {
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
    @Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
        this.userAccount.setNbCards(this.getUserAccount().getNbCards()+1);
    }
    
    public static Card findCardByCsn(String csn) {
        List<Card> cards = Card.findCardsByCsn(csn).getResultList();
        if (cards.isEmpty()) {
            return null;
        } else {
            return cards.get(0);
        }
    }
    
    public static Card findCardByEscnUid(String escn) {
        if (escn == null || escn.length() == 0) throw new IllegalArgumentException("The escn argument is required");
        EntityManager em = entityManager();
        if(!escn.contains("-")) {
        	escn = Card.getEscnWithDash(escn);
        }
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE upper(o.escnUid) = upper(:escn)", Card.class);
        q.setParameter("escn", escn);
        List<Card> cards = q.getResultList();
        if (cards.isEmpty()) {
            return null;
        } else {
            return cards.get(0);
        }
    }

    public static TypedQuery<Card> findCardsByEtatIn(List<Etat> etats) {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.etat IN (:etats) order by dateEtat desc", Card.class);
        q.setParameter("etats", etats);
        return q;
    }

    public static TypedQuery<Card> findCardsByEppnInAndEtatIn(List<String> eppns, List<Etat> etats, String sortFieldName, String sortOrder) {
        EntityManager em = Card.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o WHERE o.eppn IN (:eppns) AND o.etat IN (:etats)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("eppns", eppns);
        q.setParameter("etats", etats);
        return q;
    }
    
    public static TypedQuery<Card> findCardsByEppnInAndEtatIn(List<String> eppns, List<Etat> etats) {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.eppn IN (:eppns) AND o.etat IN (:etats) order by date_etat desc", Card.class);
        q.setParameter("eppns", eppns);
        q.setParameter("etats", etats);
        return q;
    }

    public static Long countfindCardsByEppnEqualsAndEtatIn(String eppn, List<Etat> etats) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat IN (:etats)", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etats", etats);
        return ((Long) q.getSingleResult());
    }

    public static TypedQuery<Card> findCardsByEtatEppnEqualsAndEtatEquals(String printerEppn, Etat etat) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT o FROM Card AS o WHERE o.printerEppn = :printerEppn AND o.etat = :etat ORDER BY o.dateEtat asc", Card.class);
        q.setParameter("printerEppn", printerEppn);
        q.setParameter("etat", etat);
        return  q;
    }

    public static Long countfindCardsByEtatEppnEqualsAndEtatEquals(String etatEppn, Etat etat) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.etatEppn = :etatEppn AND o.etat = :etat", Long.class);
        q.setParameter("etatEppn", etatEppn);
        q.setParameter("etat", etat);
        return ((Long) q.getSingleResult());
    }
    
    public static Long countfindCardsByEppnEqualsAndEtatNotIn(String eppn, List<Etat> etats) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat NOT IN (:etats)", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etats", etats);
        return ((Long) q.getSingleResult());
    }

    public static TypedQuery<Card> findCardsByQrcodeAndEtatIn(String qrcode, List<Etat> etats) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT o FROM Card AS o WHERE o.qrcode = :qrcode AND o.etat IN (:etats) order by date_etat desc", Card.class);
        q.setParameter("qrcode", qrcode);
        q.setParameter("etats", etats);
        return q;
    }

    
    
    public static TypedQuery<Card> findCardsByEppnAndEtatEquals(String eppn, Etat etat, String sortFieldName, String sortOrder) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        EntityManager em = Card.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o WHERE o.eppn = :eppn AND o.etat = :etat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etat", etat);
        return q;
    }

    public static TypedQuery<Card> findCardByTypeLike(String type) {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.userAccount.eduPersonPrimaryAffiliation LIKE :type", Card.class);
        q.setParameter("type", "%" + type + "%");
        return q;
    }

    public static TypedQuery<Card> findCardByTypeLike(String type, String sortFieldName, String sortOrder) {
        EntityManager em = Card.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o WHERE o.userAccount.eduPersonPrimaryAffiliation LIKE :type");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("type", "%" + type + "%");
        return q;
    }

    public static Long countFindCardsByTypeLike(String type) {
        if (type == null || type.length() == 0) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o  WHERE o.userAccount.eduPersonPrimaryAffiliation LIKE :type", Long.class);
        q.setParameter("type", type);
        return ((Long) q.getSingleResult());
    }

    public static List<Object> countFindCardsByEtat() {
        EntityManager em = Card.entityManager();
        Query q = em.createQuery("SELECT etat, COUNT(o) FROM Card AS o  GROUP BY etat ORDER BY etat");
        return q.getResultList();
    }
    
    public static TypedQuery<Card> findCardsByDesfireIdAndAppNameEquals(String desfireId, String appName) {
        if (desfireId == null || desfireId.length() == 0) throw new IllegalArgumentException("The desfireId argument is required");
        if (appName == null || appName.length() == 0) throw new IllegalArgumentException("The appName argument is required");
        EntityManager em = Card.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o JOIN o.desfireIds d WHERE key(d) = :appName AND d.id = :desfireId");
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("appName", appName);
        q.setParameter("desfireId", desfireId);
        return q;
    }

    public String getReverseCsn() {
    	if(csn==null) {
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
        if(csn==null) {
            return null;
        } else {
            return new BigInteger(csn, 16).toString();
        }
    }

    public String getDecimalReverseCsn() {
        if(csn==null) {
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

    public CrousSmartCard getCrousSmartCard() {
        if (this.getCsn() != null && !this.getCsn().isEmpty()) {
            return CrousSmartCard.findCrousSmartCard(this.getCsn());
        } else {
            return new CrousSmartCard();
        }
    }

    @Transactional
    public void remove() {
        User user = User.findUser(this.getEppn());
        user.getCards().remove(this);
        EntityManager em = Card.entityManager();
        if (em.contains(this)) {
            em.remove(this);
        } else {
            Card attached = Card.findCard(this.getId());
            em.remove(attached);
        }
    }
    
	public static String snakeToCamel(String snake){
        String camelString = "";
		camelString = WordUtils.capitalize(snake, "_".toCharArray()).replace("_", "");
        char ch[] = camelString.toCharArray();
        ch[0] = Character.toLowerCase(ch[0]);
        camelString = new String(ch);     
        return camelString;
	}

    public static TypedQuery<Card> findCards(CardSearchBean searchBean, String eppn, String sortFieldName, String sortOrder) {
        EntityManager em = Card.entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Card> query = criteriaBuilder.createQuery(Card.class);
        Root<Card> c = query.from(Card.class);
        
        final List<Order> orders = new ArrayList<Order>();
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
                orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
            } else {
                if ("nbCards".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.desc(u.get("nbCards")));
                } else if ("displayName".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.desc(u.get("name")));
                    orders.add(criteriaBuilder.desc(u.get("firstname")));
                }else if ("address".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.desc(u.get("address")));
                }
            }
        } else {
            if(fieldNames4OrderClauseFilter.contains(sortFieldName)) {
                orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
            } else {
                if("nbCards".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.asc(u.get("nbCards")));
                } else if("displayName".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.asc(u.get("name")));
                    orders.add(criteriaBuilder.asc(u.get("firstname")));
                } else if("address".equals(sortFieldName)) {
                    Join<Card, User> u = c.join("userAccount");
                    orders.add(criteriaBuilder.asc(u.get("address")));
                }
            }
        }
 
        if (!searchBean.getSearchText().isEmpty()) {
        	String searchString = computeSearchString(searchBean.getSearchText());
            Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(criteriaBuilder, searchString);
            orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }
        orders.add(criteriaBuilder.desc(c.get("dateEtat")));
        orders.add(criteriaBuilder.desc(c.get("id")));
        
        final List<Predicate> predicates = getPredicates4CardSearchBean(searchBean, eppn, criteriaBuilder, c);
        
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);
        if (searchBean.getSearchText().isEmpty() && searchBean.getFreeField() != null && searchBean.getFreeField().values().contains("desfire_ids")) {
        	// hack : use distinct because of join on desfireIds
        	// but can't use distinct with searchText :  
        	//  org.postgresql.util.PSQLException: ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list
        	query.distinct(true);
        }
        return em.createQuery(query);
    }
    

    public static long countFindCards(CardSearchBean searchBean, String eppn) {
        EntityManager em = Card.entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<Card> c = query.from(Card.class);
        
        final List<Predicate> predicates = getPredicates4CardSearchBean(searchBean, eppn, criteriaBuilder, c);

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        if (searchBean.getSearchText().isEmpty() && searchBean.getFreeField() != null && searchBean.getFreeField().values().contains("desfire_ids")) {
        	// hack : use distinct because of join on desfireIds
        	query.select(criteriaBuilder.countDistinct(c));
        } else {
        	query.select(countStar(criteriaBuilder));
        }
        return em.createQuery(query).getSingleResult();
    }

	protected static List<Predicate> getPredicates4CardSearchBean(CardSearchBean searchBean, String eppn,
			CriteriaBuilder criteriaBuilder, Root<Card> c) {
		
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		if (searchBean.getType()!=null && !searchBean.getType().isEmpty() && !"All".equals(searchBean.getType())) {
            Join<Card, User> u = c.join("userAccount");
            predicates.add(u.get("userType").in(searchBean.getType()));
        }
        if (searchBean.getFreeField() != null && searchBean.getFreeFieldValue()!= null) {
        	if(!searchBean.getFreeField().values().isEmpty() && !searchBean.getFreeFieldValue().isEmpty()){
	            Join<Card, User> u = c.join("userAccount");
	            for(Map.Entry<Integer, List<String>> entry : searchBean.getFreeFieldValue().entrySet()){
	            	if(!entry.getValue().isEmpty() && searchBean.getFreeField().get(entry.getKey())!=null){
		            	List<Predicate> orPredicates = new ArrayList<Predicate>();
	            		String camelString = snakeToCamel(searchBean.getFreeField().get(entry.getKey()));
	            		for(String v : entry.getValue()){
	            			log.trace(String.format("%s -> %s", camelString, v));
		            		if(!searchBean.getFreeField().get(entry.getKey()).isEmpty() && v!=null){
		            			if(camelString.startsWith("card.")) {
		            				if(camelString.equals("card.templateCard")) {
		            					Join<Card, TemplateCard> tc = c.join("templateCard");
		            					TemplateCard templateCard = TemplateCard.findTemplateCard(Long.valueOf(v));
		            					orPredicates.add(criteriaBuilder.equal(tc, templateCard));
		            				} else {
		            					orPredicates.add(criteriaBuilder.equal(c.get(camelString.substring("card.".length())), v));
		            				}
		            			} else if(camelString.startsWith("userAccount.")) {
		            				if(User.BOOLEAN_FIELDS.contains(camelString.substring("userAccount.".length()))) {
		            					if("true".equalsIgnoreCase(v)) {
		            						orPredicates.add(criteriaBuilder.isTrue(u.get(camelString.substring("userAccount.".length()))));
		            					} else {
		            						orPredicates.add(criteriaBuilder.isFalse(u.get(camelString.substring("userAccount.".length()))));
		            					}
		            				} else {
		            					orPredicates.add(criteriaBuilder.equal(u.get(camelString.substring("userAccount.".length())), v));
		            				}
		            			} else if(camelString.startsWith("desfireIds")) {
		            				orPredicates.add(criteriaBuilder.equal(c.joinMap("desfireIds").value(), v));
		            			} else {
		            				orPredicates.add(criteriaBuilder.equal(u.get(camelString), v));
		            			}
		            		}
	            		}
	            		predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[] {})));
	            	}
	    		}
        	}
        }
        if (searchBean.getAddress()!=null && !searchBean.getAddress().isEmpty()) {
            Join<Card, User> u = c.join("userAccount");
            predicates.add(u.get("address").in(searchBean.getAddress()));
        }
        if (searchBean.getLastTemplateCardPrinted() != null) {
        	Join<Card, User> u = c.join("userAccount");
            predicates.add(u.get("lastCardTemplatePrinted").in(searchBean.getLastTemplateCardPrinted()));
        }
        if (searchBean.getEtat() != null) {
            predicates.add(criteriaBuilder.equal(c.get("etat"), searchBean.getEtat()));
        }
        if (searchBean.getFlagAdresse() != null) {
            predicates.add(criteriaBuilder.equal(c.get("flagAdresse"), searchBean.getFlagAdresse()));
        }
        if (searchBean.getNbRejets() != null) {
            predicates.add(criteriaBuilder.equal(c.get("nbRejets"), searchBean.getNbRejets()));
        }
        if (!searchBean.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchBean.getSearchText());
            Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
        }
        if (searchBean.getOwnOrFreeCard() != null && searchBean.getOwnOrFreeCard()) {
            predicates.add(criteriaBuilder.or(criteriaBuilder.equal(c.get("etatEppn"), eppn), criteriaBuilder.isNull(c.get("etatEppn")), criteriaBuilder.equal(c.get("etatEppn"), "")));
        }
        if ("true".equals(searchBean.getEditable()) || "false".equals(searchBean.getEditable())) {
            Join<Card, User> u = c.join("userAccount");
            Expression<Boolean> editableExpr = u.get("editable");
            if ("true".equals(searchBean.getEditable())) {
                predicates.add(criteriaBuilder.isTrue(editableExpr));
            } else if ("false".equals(searchBean.getEditable())) {
                predicates.add(criteriaBuilder.isFalse(editableExpr));
            }
        }
        if (searchBean.getNbCards() != null) {
            Join<Card, User> u = c.join("userAccount");
            predicates.add(criteriaBuilder.equal(u.get("nbCards"), searchBean.getNbCards()));
        }
        if (searchBean.getNbRejets() != null) {
            predicates.add(criteriaBuilder.equal(c.get("nbRejets"), searchBean.getNbRejets()));
        }
        if (searchBean.getHasRequestCard() != null) {
        	Join<Card, User> u = c.join("userAccount");
            Expression<Boolean> hasRequestCardExpr = u.get("hasCardRequestPending");
            if ("true".equals(searchBean.getHasRequestCard())) {
                predicates.add(criteriaBuilder.isTrue(hasRequestCardExpr));
            } else if ("false".equals(searchBean.getHasRequestCard())) {
                predicates.add(criteriaBuilder.isFalse(hasRequestCardExpr));
            }
        }
        
        return predicates;
	}


    private static String computeSearchString(String searchString) {
        List<String> searchStrings = Arrays.asList(StringUtils.splitByWholeSeparator(searchString, null));
        List<String> searchStringsExpr = new ArrayList<String>();
        for (String s : searchStrings) {
            searchStringsExpr.add(s + ":*");
        }
        searchString = StringUtils.join(searchStringsExpr, "|");
        return searchString;
    }

    private static Expression<Boolean> getFullTestSearchExpression(CriteriaBuilder cb, String searchString) {
        return cb.function("fts", Boolean.class, cb.literal(searchString));
    }

    private static Expression<Double> getFullTestSearchRanking(CriteriaBuilder cb, String searchString) {
        return cb.function("ts_rank", Double.class, cb.literal(searchString));
    }

    private static Expression<Long> countStar(CriteriaBuilder cb) {
        return cb.function("count_star", Long.class);
    }

    /***Stats****/
    public static List<Object[]> countNbCardsByYearEtat(String userType, String etatCase) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT CASE WHEN(DATE_PART('month', request_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', request_date)-1 AS TEXT),'-',CAST(DATE_PART('year', request_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', request_date) AS TEXT),'-',CAST(DATE_PART('year', request_date)+1 AS TEXT)) END AS Saison, " + etatCase + ", count(*) as count FROM card GROUP BY Saison, etat ORDER BY Saison ASC";
        if (!userType.isEmpty()) {
            sql = "SELECT CASE WHEN(DATE_PART('month', request_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', request_date)-1 AS TEXT),'-',CAST(DATE_PART('year', request_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', request_date) AS TEXT),'-',CAST(DATE_PART('year', request_date)+1 AS TEXT)) END AS Saison, " + etatCase + ", count(*) as count FROM card, user_account WHERE card.user_account= user_account.id " + "AND user_type =:userType GROUP BY Saison, etat ORDER BY Saison ASC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<Object[]> countNbCardsByDay(String userType, String typeDate) {
        String sql = "SELECT to_date(to_char(" + typeDate + ", 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - " + typeDate + ") < 31 GROUP BY day ORDER BY day";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT to_date(to_char(" + typeDate + ", 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card, user_account WHERE card.user_account=user_account.id " + "AND "
            		+ "user_type =:userType AND DATE_PART('days', now() - " + typeDate + ") < 31 GROUP BY day ORDER BY day";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countNbCardsByEtat(String userType) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT etat, count(*) as count FROM card GROUP BY etat ORDER BY count DESC ";
        if (!userType.isEmpty()) {
            sql = "SELECT etat, count(*) as count FROM card, user_account WHERE card.user_account= user_account.id " + "AND user_type =:userType GROUP BY etat ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<Card> findAllCards(List<Long> cardIds) {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.id in (:cardIds)", Card.class);
        q.setParameter("cardIds", cardIds);
        return q.getResultList();
    }

    public static List<Object[]> countNbCardsByMotifsDisable(String userType, String motifCase) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT " + motifCase + ", COUNT(*) FROM card WHERE motif_disable IS NOT NULL GROUP BY motif_disable ORDER BY motif_disable";
        if (!userType.isEmpty()) {
            sql = "SELECT " + motifCase + ", COUNT(*) FROM card, user_account WHERE card.user_account= user_account.id " + "AND motif_disable IS NOT NULL AND user_type =:userType GROUP BY motif_disable";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<Object[]> countNbCardsByMonthYear(String userType) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT CAST(DATE_PART('month', request_date) AS INTEGER) AS month, CAST(DATE_PART('year', request_date) AS INTEGER) AS year, count(*) AS count FROM card GROUP BY month, year ORDER BY month, year";
        if (!userType.isEmpty()) {
            sql = "SELECT CAST(DATE_PART('month', request_date) AS INTEGER) AS month, CAST(DATE_PART('year', request_date) AS INTEGER) AS year, count(*) AS count FROM card, user_account "
            		+ "WHERE card.user_account= user_account.id " + " AND user_type =:userType GROUP BY month, year ORDER BY month, year";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countNbCardsEncodedByMonthYear(String userType) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT CAST(DATE_PART('month', encoded_date) AS INTEGER) AS month, CAST(DATE_PART('year', encoded_date) AS INTEGER) AS year, count(*) AS count FROM card WHERE encoded_date is not null GROUP BY month, year ORDER BY month, year";
        if (!userType.isEmpty()) {
            sql = "SELECT CAST(DATE_PART('month', encoded_date) AS INTEGER) AS month, CAST(DATE_PART('year', encoded_date) AS INTEGER) AS year, count(*) AS count FROM card, user_account "
            		+ "WHERE encoded_date is not null AND card.user_account= user_account.id " + " AND user_type =:userType GROUP BY month, year ORDER BY month, year";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }  
    
	public static List<Object[]> countNbCardsEditedByYear(String userType) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, "
        		+ "count(*) AS count FROM card where encoded_date is not null GROUP BY Saison order by Saison";
        if (!userType.isEmpty()) {
        	sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
            		+ "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
            		+ "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, "
            		+ "count(*) AS count FROM card, user_account where card.encoded_date is not null AND card.user_account=user_account.id AND user_type =:userType GROUP BY Saison order by Saison"; 
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
	}
	
	public static List<Object[]> countNbCardsEnabledEncodedByYear(String userType) {
        EntityManager em = Card.entityManager();
        String sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, "
        		+ "count(*) AS count FROM card where encoded_date is not null AND etat IN ('ENABLED', 'ENCODED') GROUP BY Saison order by Saison";
        if (!userType.isEmpty()) {
        	sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
            		+ "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
            		+ "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, "
            		+ "count(*) AS count FROM card, user_account where card.encoded_date is not null AND etat IN ('ENABLED', 'ENCODED') AND card.user_account=user_account.id AND user_type =:userType GROUP BY Saison order by Saison"; 
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
	}

    public static List<Object[]> countNbDeliverdCardsByDay(String userType) {
        String sql = "SELECT to_date(to_char(delivered_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - delivered_date) < 31 GROUP BY day ORDER BY day";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT to_date(to_char(delivered_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card, user_account WHERE card.user_account=user_account.id " + 
            		"AND user_type =:userType AND DATE_PART('days', now() - delivered_date) < 31 GROUP BY day ORDER BY day";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<String> findDistinctEtats() {
        EntityManager em = Card.entityManager();
        Query q = em.createNativeQuery(User.selectDistinctWithLooseIndex("card", "etat"));
        return q.getResultList();
    }
    
    public static List<String> findDistinctUserTypes(List<Long> cardIds) {
    	if(cardIds == null || cardIds.isEmpty()) return new ArrayList<String>();
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT DISTINCT(u.userType) FROM User AS u WHERE u.eppn IN (SELECT c.eppn FROM Card as c WHERE c.id IN (:cardIds))", String.class);
        q.setParameter("cardIds", cardIds);
        return q.getResultList();
    }

    public static Long countNBCardsByEppn(String eppn) {
        EntityManager em = Card.entityManager();
        Query q = em.createNativeQuery("SELECT count (*) From Card WHERE eppn=:eppn");
        q.setParameter("eppn", eppn);
        return Long.valueOf(String.valueOf(q.getSingleResult()));
    }

    public static List<Object[]> countNbEncodedCardsByDay(String userType) {
        String sql = "SELECT to_date(to_char(encoded_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - encoded_date) < 31 GROUP BY day ORDER BY day";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT to_date(to_char(encoded_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card, user_account WHERE card.user_account=user_account.id " + 
            		"AND user_type =:userType AND DATE_PART('days', now() - encoded_date) < 31 GROUP BY day ORDER BY day";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<BigInteger> getDistinctNbRejets() {
        EntityManager em = Card.entityManager();
        Query q = em.createNativeQuery(User.selectDistinctWithLooseIndex("card", "nb_rejets"));
        List<BigInteger> distinctNbRejets = q.getResultList();
        distinctNbRejets.remove(BigInteger.valueOf(0));
        return distinctNbRejets;
    }

    public static List<Object[]> countBrowserStats(String userType) {
        String sql = "SELECT CASE WHEN request_browser LIKE '%Firefox%' THEN 'Firefox' WHEN request_browser LIKE '%Chrome%' THEN 'Chrome' " + "WHEN request_browser LIKE '%Explorer%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%IE%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%Apple%' THEN 'Safari' " + "WHEN request_browser LIKE '%Safari%' THEN 'Safari' " + "WHEN request_browser LIKE '%Edge%' THEN 'Microsoft Edge' " + "WHEN request_browser LIKE '%Opera%' THEN 'Opera' ELSE request_browser END  AS browser , " + "COUNT(*) AS count FROM card WHERE request_browser IS NOT NULL GROUP BY browser ORDER BY count DESC";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT CASE WHEN request_browser LIKE '%Firefox%' THEN 'Firefox' WHEN request_browser LIKE '%Chrome%' THEN 'Chrome' " + "WHEN request_browser LIKE '%Explorer%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%IE%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%Apple%' THEN 'Safari' " + "WHEN request_browser LIKE '%Safari%' THEN 'Safari' " + "WHEN request_browser LIKE '%Edge%' THEN 'Microsoft Edge' " + "WHEN request_browser LIKE '%Opera%' THEN 'Opera' ELSE request_browser END  AS browser , " + "COUNT(*) AS count FROM card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_browser IS NOT NULL GROUP BY browser ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<Object[]> countOsStats(String userType) {
        String sql = "SELECT CASE WHEN request_os LIKE '%iPhone%' THEN 'Smartphone' " + "WHEN request_os LIKE 'Android%x' THEN 'Smartphone' " + "WHEN request_os LIKE '%Phone%' THEN 'Smartphone' WHEN request_os LIKE '%iPad%' THEN 'Tablette' " + "WHEN request_os  LIKE '%Tablet%' THEN 'Tablette' WHEN request_os LIKE '%Touch%' THEN 'Tablette' " + "ELSE 'Desktop' END  AS os, count(*) as count FROM Card WHERE request_os IS NOT NULL GROUP BY os ORDER BY count DESC";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT CASE WHEN request_os LIKE '%iPhone%' THEN 'Smartphone' " + "WHEN request_os LIKE 'Android%x' THEN 'Smartphone' " + "WHEN request_os LIKE '%Phone%' THEN 'Smartphone' WHEN request_os LIKE '%iPad%' THEN 'Tablette' " + "WHEN request_os  LIKE '%Tablet%' THEN 'Tablette' WHEN request_os LIKE '%Touch%' THEN 'Tablette' " + "ELSE 'Desktop' END  AS os, count(*) as count FROM Card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_os IS NOT NULL GROUP BY os ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countRealOsStats(String userType) {
        String sql = "SELECT request_os, count(*) as count FROM Card WHERE request_os IS NOT NULL GROUP BY request_os ORDER BY count DESC";
        EntityManager em = Card.entityManager();
        if (!userType.isEmpty()) {
            sql = "SELECT request_os, count(*) as count FROM Card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_os IS NOT NULL GROUP BY request_os ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public static List<Object[]> countNbEditedCardNotDelivered(String typeCase) {
        EntityManager em = Card.entityManager();
     
        String sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, " + typeCase + ", count(*) FROM card, user_account WHERE card.user_account= user_account.id AND delivered_date is null AND etat IN ('ENABLED', 'ENCODED') AND user_type NOT LIKE '' GROUP BY Saison, user_type ORDER by Saison";
        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public static List<Object[]> countNbCardsByRejets(String userType) {
        EntityManager em = Card.entityManager();

        String sql = "SELECT nb_rejets, count(*) FROM card GROUP BY nb_rejets";
        if (!userType.isEmpty()) {
            sql = "SELECT nb_rejets, count(*) FROM card, user_account WHERE card.user_account= user_account.id AND user_type = :userType GROUP BY nb_rejets";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    /**
     * @deprecated - use directly getDueDate() : DUE_DATE_INCLUDED_DELAY = 0 now
     * @return Date + DUE_DATE_INCLUDED_DELAY
     */
    public Date getDueDateIncluded() {
        Date dueDateIncluded = null;
        Date dueDate = this.getDueDate();
        if (dueDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dueDate);
            cal.add(Calendar.HOUR, User.DUE_DATE_INCLUDED_DELAY);
            dueDateIncluded = cal.getTime();
        }
        return dueDateIncluded;
    }

    public static TypedQuery<Card> findCardsByCsn(String csn) {
        if (csn == null || csn.length() == 0) throw new IllegalArgumentException("The csn argument is required");
        EntityManager em = entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.csn = upper(:csn)", Card.class);
        q.setParameter("csn", csn);
        return q;
    }

    public static TypedQuery<Card> findCardsByCsn(String csn, String sortFieldName, String sortOrder) {
        if (csn == null || csn.length() == 0) throw new IllegalArgumentException("The csn argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o WHERE o.csn = upper(:csn)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("csn", csn);
        return q;
    }

    public static Long countFindCardsByCsn(String csn) {
        if (csn == null || csn.length() == 0) throw new IllegalArgumentException("The csn argument is required");
        EntityManager em = entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.csn = upper(:csn)", Long.class);
        q.setParameter("csn", csn);
        return ((Long) q.getSingleResult());
    }

    public static List<Object[]> countDeliveryByAddress() {
        EntityManager em = User.entityManager();
        String sql = "SELECT address, count(*) FROM card INNER JOIN user_account ON card.user_account=user_account.id AND delivered_date is null GROUP BY address ORDER BY count DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static List<Object[]> countNonEditableByAddress() {
        EntityManager em = User.entityManager();
        String sql = "SELECT address, count(*) FROM card INNER JOIN user_account ON card.user_account=user_account.id AND not editable and etat='NEW' GROUP BY address ORDER BY count DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public static Boolean areCardsReadyToBeDelivered(List<Long> cardIds) {
        EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.id in (:cardIds) and o.etat in (:etatsEncoded) AND o.deliveredDate IS NULL AND o.external='f'", Long.class);
        q.setParameter("cardIds", cardIds);
        q.setParameter("etatsEncoded", CardEtatService.etatsEncoded);
        return !Long.valueOf(0).equals(((Long) q.getSingleResult()));
    }
    
    public static Boolean areCardsReadyToBeValidated(List<Long> cardIds) {
    	EntityManager em = Card.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.id in (:cardIds) and o.etat in (:etatsValidateDisabled)", Long.class);
        q.setParameter("cardIds", cardIds);
        q.setParameter("etatsValidateDisabled", Arrays.asList(new Etat[] {Etat.ENABLED, Etat.DISABLED, Etat.CADUC}));
        return !Long.valueOf(0).equals(((Long) q.getSingleResult()));
    }

	/**
	 * Hack : ids are sorted so that ENABLED card are at end of the list 
	 */
	public static List<BigInteger> findAllCardIds() {
        EntityManager em = Card.entityManager();
        String sql = "SELECT id FROM card order by (case etat when 'ENABLED' then 2 else 1 end)";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
	}
	
    public static List<Object[]> countNbCardRequestByMonth(String userType) {
        String sql = "SELECT to_char(request_date, 'MM-YYYY') tochar, count(*) FROM card GROUP BY tochar ORDER BY to_date(to_char(request_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(request_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(request_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = Card.entityManager();

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countNbCardEncodedByMonth(String userType) {
        String endDate = "";
        String sql = "SELECT to_char(encoded_date, 'MM-YYYY') tochar, count(*) FROM card GROUP BY tochar ORDER BY to_date(to_char(encoded_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(encoded_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id  AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(encoded_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = Card.entityManager();

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }        
        return q.getResultList();
    }
    
    public static List<Object[]> countNbRejetsByMonth(String userType) {
        String sql = "SELECT to_char(date_etat, 'MM-YYYY') tochar, count(*) FROM card WHERE etat='REJECTED' GROUP BY tochar ORDER BY to_date(to_char(date_etat, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(date_etat, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id " + "AND etat='REJECTED' AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(date_etat, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = Card.entityManager();

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }        

        return q.getResultList();
    }
    
    public static List<Object[]> countDueDatesByDate(String userType) {
        String sql = "SELECT to_char(user_account.due_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account=user_account.id AND etat IN ('NEW','REJECTED','RENEWED') GROUP BY tochar ORDER BY to_date(to_char(user_account.due_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(user_account.due_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id AND etat IN ('NEW','REJECTED','RENEW') AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(user_account.due_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = Card.entityManager();

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }        

        return q.getResultList();
    }

    public static List<String> getDistinctFreeField(String field) {
    	EntityManager em = Card.entityManager();
    	// FormService.getField1List uses its preventing sql injection
    	String req = "SELECT DISTINCT CAST(" + field + " AS VARCHAR) FROM card WHERE " + field  + " IS NOT NULL ORDER BY " + field;
    	Query q = em.createNativeQuery(req);
    	List<String> distinctResults = q.getResultList();
    	return distinctResults;
    }
    
    public static Long getCountDistinctFreeField(String field) {
    	EntityManager em = Card.entityManager();
    	// FormService.getField1List uses its preventing sql injection
    	String req = "SELECT count(DISTINCT(" + field + ")) FROM card WHERE " + field  + " IS NOT NULL";
    	Query q = em.createNativeQuery(req);
    	return ((BigInteger)q.getSingleResult()).longValue();
    }
    
    public static TypedQuery<Card> findCardsWithEscnAndCsn() {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.escnUid IS NOT NULL and o.escnUid IS NOT EMPTY AND o.csn IS NOT NULL and o.csn IS NOT EMPTY", Card.class);
        return q;
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

    public static Card findOneCardForTemplate(TemplateCard templateCard) {
        EntityManager em = Card.entityManager();
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.templateCard=:templateCard", Card.class);
        q.setParameter("templateCard", templateCard);
        List<Card> cards = q.setMaxResults(1).getResultList();
        return cards.isEmpty() ? null : cards.get(0);
    }

    public static Long countFindCardsByEtatAndUserTypeAndDateEtatLessThan(Etat etat, String userType, Date dateEtat) {
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        if (dateEtat == null) throw new IllegalArgumentException("The dateEtat argument is required");
        EntityManager em = Card.entityManager();
        String jpql = "SELECT COUNT(o) FROM Card AS o WHERE o.etat = :etat AND o.dateEtat < :dateEtat";
        if (!userType.isEmpty()) {
            jpql = "SELECT COUNT(o) FROM Card AS o WHERE o.etat = :etat AND o.dateEtat < :dateEtat AND o.userAccount.userType = :userType";
        }
        TypedQuery q = em.createQuery(jpql, Long.class);
        q.setParameter("etat", etat);
        q.setParameter("dateEtat", dateEtat);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return ((Long) q.getSingleResult());
    }

    public static TypedQuery<Card> findCardsByEtatAndUserTypeAndDateEtatLessThan(Etat etat, String userType, Date dateEtat) {
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        if (dateEtat == null) throw new IllegalArgumentException("The dateEtat argument is required");
        EntityManager em = Card.entityManager();
        String jpql = "SELECT o FROM Card AS o WHERE o.etat = :etat AND o.dateEtat < :dateEtat";
        if (!userType.isEmpty()) {
            jpql = "SELECT o FROM Card AS o WHERE o.etat = :etat AND o.dateEtat < :dateEtat AND o.userAccount.userType = :userType";
        }
        TypedQuery<Card> q = em.createQuery(jpql, Card.class);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        q.setParameter("etat", etat);
        q.setParameter("dateEtat", dateEtat);
        return q;
    }

}

