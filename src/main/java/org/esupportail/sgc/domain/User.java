package org.esupportail.sgc.domain;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.security.SgcRoleHierarchy;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@RooJavaBean
@RooToString(excludeFields={"cards"})
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "UserAccount", finders={"findUsersByEppnEquals", "findUsersByCrous", "findUsersByEuropeanStudentCard", "findUsersByCrousIdentifier"})
@JsonFilter("userFilter")
public class User {
	
	private final static Logger log = LoggerFactory.getLogger(User.class);

	// @see getDueDateIncluded()
	public final static int DUE_DATE_INCLUDED_DELAY = +0;
	
	public static enum CnousReferenceStatut {
		psg, etd, prs, hbg, fct, fpa, stg;
	};
	
	public final static List<String> BOOLEAN_FIELDS = Arrays.asList(new String[] {"crous", "europeanStudentCard", "difPhoto", "editable", "requestFree", "hasCardRequestPending"});

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
	private Date birthday;
	
	private String institute;
	
	private String eduPersonPrimaryAffiliation;
	
	private String email;
	
	private String rneEtablissement = "";
	
	@Column
	@Enumerated(EnumType.STRING)
	private CnousReferenceStatut cnousReferenceStatut = CnousReferenceStatut.psg;
	
	private Long indice = Long.valueOf(0);
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private Date dueDate;
	
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
	
	private Long nbCards = new Long(0);
	
	private String userType;
	
	private String templateKey;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private TemplateCard lastCardTemplatePrinted;

	@ElementCollection
    @CollectionTable(name="roles", joinColumns=@JoinColumn(name="user_account"))
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
    private PhotoFile defaultPhoto = new PhotoFile();
    
	@Column(columnDefinition="TEXT")
	private String fullText;
	
	private String pic = "";
	
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
	
	public static User findUser(String eppn) {
		User user = null;
		List<User> users = User.findUsersByEppnEquals(eppn).getResultList();
		if(!users.isEmpty()) {
			user = users.get(0);
		}
		return user;
	}


	public static List<String> findAllEppns() {
		EntityManager em = User.entityManager();
		TypedQuery<String> q = em.createQuery("SELECT o.eppn FROM User o", String.class);
		return q.getResultList();
	}
	
	
	public static TypedQuery<User> findUsersWithNoCards() {
		EntityManager em = User.entityManager();
		TypedQuery<User> q = em.createQuery("SELECT o FROM User o where o.cards IS EMPTY", User.class);
		return q;
	}
	
	public static long countFindUsersWithNoCards() {
		EntityManager em = User.entityManager();
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM User o where o.cards IS EMPTY", Long.class);
		return q.getSingleResult();
	}
	
	
	public static List<String> findAllEppnsWithRole(String roleName) {
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("select eppn from user_account u, roles r where r.role=:roleName and u.id=r.user_account");
		q.setParameter("roleName", roleName);
		return q.getResultList();
	}
	
	public static List<String> findDistinctAddresses(String userType, Etat etat) {
		EntityManager em = User.entityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<Card> c = query.from(Card.class);
        Join<Card, User> u = c.join("userAccount");
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(criteriaBuilder.notEqual(u.get("address"), ""));
        if(etat!=null) {
        	predicates.add(criteriaBuilder.equal(c.get("etat"), etat));
        }
        if(userType != null && !userType.isEmpty() && !"All".equals(userType)) {
        	predicates.add(criteriaBuilder.equal(u.get("userType"), userType));
        }
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(criteriaBuilder.asc(u.get("address")));
        query.select(u.get("address")).distinct(true);
        return em.createQuery(query).getResultList();
	}
	

	public String getFieldNotEquals(Object obj) {
		if (this == obj)
			return null;
		if (obj == null)
			return "null";
		if (getClass() != obj.getClass())
			return "null";
		User other = (User) obj;

		if (birthday == null) {
			if (other.birthday != null) {
				return "birthday";
			}
		} 
		// compare only day (without time) for birthday 
		else if (DateTimeComparator.getDateOnlyInstance().compare(birthday, other.birthday) != 0) {
			return "birthday";
		}

		if (dueDate == null) {
			if (other.dueDate != null) {
				return "dueDate";
			}
		} else if (!dueDate.equals(other.dueDate)) {
			return "dueDate";
		}
		
		if (externalCard == null) {
			if (other.externalCard != null) {
				return "externalCard";
			}
		} else if (!externalCard.equals(other.externalCard)) {
			return "externalCard";
		}
		
		if (this.getDefaultPhoto() == null || this.getDefaultPhoto().getBigFile() == null || this.getDefaultPhoto().getBigFile().getMd5() == null) {
			if (!(other.getDefaultPhoto() == null || other.getDefaultPhoto().getBigFile() == null || other.getDefaultPhoto().getBigFile().getMd5() == null)) {
				return "defaultPhoto";
			}
		} else if (other.getDefaultPhoto() == null || other.getDefaultPhoto().getBigFile() == null || !this.getDefaultPhoto().getBigFile().getMd5().equals(other.getDefaultPhoto().getBigFile().getMd5())) {
			return "defaultPhoto";
		}
		
		for(String varStringName : Arrays.asList(new String[] {"address", "externalAddress", "cnousReferenceStatut", "crous", "difPhoto", "editable", "eduPersonPrimaryAffiliation", "email",
				"eppn", "europeanStudentCard", "firstname", "idCompagnyRate", "indice", "institute", "name", 
				"recto1", "recto2", "recto3", "recto4", "recto5", "recto6", "recto7", 
				"freeField1", "freeField2", "freeField3", 
				"requestFree", "rneEtablissement", "secondaryId", "supannCodeINE", "supannEmpId", "supannEntiteAffectationPrincipale", "supannEtuId", "userType", 
				"verso1", "verso2", "verso3", "verso4", "verso5", "verso6", "verso7", 
				"templateKey", "blockUserMsg", "academicLevel", "pic"})) {
			if(!this.checkEqualsVar(varStringName, other)) {
				return varStringName;
			}
		}
		
		// hack idRate pour student : si idCompagnyRate == 10 (student), idRate pas pris en compte pour getFieldNotEquals
		if (idRate == null) {
			if (other.idRate != null) {
				return "idRate";
			}
		} else if(idCompagnyRate==null || other.idCompagnyRate==null || !idCompagnyRate.equals(other.idCompagnyRate) || !idCompagnyRate.equals(Long.valueOf(10))) {
			if(!this.checkEqualsVar("idRate", other)) {
				return "idRate";
			}
		}

		return null;
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

    public static List<Object[]> countNbCrous(String userType) {
        EntityManager em = User.entityManager();
        String sql = "SELECT crous, count(*) as count FROM user_account GROUP BY crous ORDER BY count DESC";
        if (!userType.isEmpty()) {
            sql = "SELECT crous, count(*) as count FROM user_account WHERE user_type = :userType GROUP BY crous ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countNbDifPhoto(String userType) {
        EntityManager em = User.entityManager();
        String sql = "SELECT dif_photo, count(*) as count FROM user_account GROUP BY dif_photo ORDER BY count DESC";
        if (!userType.isEmpty()) {
            sql = "SELECT dif_photo, count(*) as count FROM user_account WHERE user_type = :userType GROUP BY dif_photo ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static List<Object[]> countYesterdayCardsByPopulationCrous(String isMonday, String typeDate) {
        EntityManager em = User.entityManager();
        
        String mondayorNot = " AND to_date(to_char(" + typeDate + ", 'DD-MM-YYYY'), 'DD-MM-YYYY')= TIMESTAMP 'yesterday'";
        if("true".equals(isMonday)){
        	mondayorNot = " AND to_char(" + typeDate + ", 'DD-MM-YYYY') = to_char((now() - interval '3 day'), 'DD-MM-YYYY')";
        }
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count "
        		+ "FROM card, user_account WHERE card.user_account=user_account.id AND etat IN ('ENABLED','DISABLED','CADUC','ENCODED') "
        		+ mondayorNot + " GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countMonthCardsByPopulationCrous(String date, String typeDate) {
        EntityManager em = User.entityManager();
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count "
        		+ "FROM card, user_account WHERE card.user_account=user_account.id AND etat IN ('ENABLED','DISABLED','CADUC','ENCODED') "
        		+ "AND to_char(" + typeDate + ", 'yyyy-mm-dd') like '" + date + "' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countYearEnabledCardsByPopulationCrous(String date, String typeDate, Date dateFin) {
        EntityManager em = User.entityManager();
        String endDate = "";
        if(dateFin != null){
        	endDate = " AND " + typeDate + "<:dateFin ";
        }
        String majCond = "";
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count FROM card, user_account "
        		+ " WHERE card.user_account=user_account.id AND etat IN ('ENABLED','DISABLED','CADUC','ENCODED') "
        		+ "AND " + typeDate + " >='" + date + "' " + endDate  +  "GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);
        if(dateFin != null){
        	q.setParameter("dateFin", dateFin);
        }
        return q.getResultList();
    }

	public static List<BigInteger> getDistinctNbCards() {
		EntityManager em = Card.entityManager();
		Query q = em.createNativeQuery("SELECT DISTINCT(nb_cards) FROM user_account where nb_cards <> 0 ORDER BY nb_cards");
		 List<BigInteger> distinctNbCards = q.getResultList();
		 return distinctNbCards;
	}
	
	public static List<Object[]> countNbCardsByuser(String userType) {
        EntityManager em = User.entityManager();
        String sql = "SELECT nb_cards, count(*) as count FROM user_account GROUP BY nb_cards ORDER BY count DESC";
        if (!userType.isEmpty()) {
            sql = "SELECT nb_cards, count(*) as count FROM user_account WHERE user_type = :userType GROUP BY nb_cards ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
	
    public static List<Object[]> countNbEditable() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CASE WHEN editable = 't' THEN 'Editable' ELSE 'Non editable' END AS etat , count(*) FROM card, user_account WHERE card.user_account=user_account.id AND etat='NEW' GROUP BY editable ORDER BY count";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static Query selectEditableCsv() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CASE WHEN editable = 't' THEN 'Editable' ELSE 'Non editable' END AS etat, name, firstname, email  FROM card, user_account WHERE card.user_account=user_account.id AND etat='NEW' ORDER BY etat DESC, name";
        Query q = em.createNativeQuery(sql);
        return q;
    }
    
	public static List<String> findDistinctUserType() {
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("SELECT DISTINCT user_type FROM user_account where user_type is not null");
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
            cal.add(Calendar.HOUR, DUE_DATE_INCLUDED_DELAY);
            dueDateIncluded = cal.getTime();
        }
        return dueDateIncluded;
    }
	
    public static List<Object[]> countTarifCrousByType() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CONCAT(id_compagny_rate, '/', id_rate) as rate, user_type, COUNT(id_rate) FROM user_account WHERE due_date > now() AND id_rate IS NOT NULL GROUP BY rate, user_type ORDER BY rate";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countNextDueDatesOneYearByType() {
        EntityManager em = User.entityManager();
        String sql = "SELECT to_char(due_date, 'MM-YYYY') tochar, user_type, count(*) FROM user_account WHERE due_date > now() AND due_date < now() + INTERVAL '1 YEAR' GROUP BY tochar, user_type ORDER BY to_date(to_char(due_date, 'MM-YYYY'), 'MM-YYYY')";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countNextDueDatesOneMonthByType() {
        EntityManager em = User.entityManager();
        String sql = "SELECT to_char(due_date, 'DD-MM-YYYY') tochar, user_type, count(*) FROM user_account WHERE due_date > now() AND due_date < now() + INTERVAL '1 MONTH' GROUP BY tochar, user_type ORDER BY to_date(to_char(due_date, 'DD-MM-YYYY'), 'DD-MM-YYYY')";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    
    public static List<Object[]> countNbRequestFree() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CASE WHEN request_free THEN 'GRATUIT' ELSE 'PAYANT' END AS request_free, user_type, count(*) FROM user_account WHERE user_type IS NOT NULL GROUP BY request_free, user_type ORDER BY request_free";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

	public TemplateCard getTemplateCard() {
		String domain =  this.getTemplateKey();
		if(domain!=null && TemplateCard.findTemplateCardsByKeyEquals(domain).getResultList().size()>0){
			return TemplateCard.findTemplateCardsByKeyEquals(domain, "numVersion", "DESC").getResultList().get(0);
		} else {
			return TemplateCard.findTemplateCardsByKeyEquals("default").getResultList().get(0);	
		}
	}
    
	public static List<String> getDistinctFreeField(String field) {
		EntityManager em = User.entityManager();
		// FormService.getField1List uses its preventing sql injection
		String req = "SELECT DISTINCT CAST(" + field + " AS VARCHAR) FROM user_account WHERE " + field  + " IS NOT NULL ORDER BY " + field;
		Query q = em.createNativeQuery(req);
		List<String> distinctResults = q.getResultList();
		return distinctResults;
	}
	
	public static Long getCountDistinctFreeField(String field) {
		EntityManager em = User.entityManager();
		// FormService.getField1List uses its preventing sql injection
		String req = "SELECT count(DISTINCT(" + field + ")) FROM user_account WHERE " + field  + " IS NOT NULL";
		Query q = em.createNativeQuery(req);
		return ((BigInteger)q.getSingleResult()).longValue();
	}

	public static List<TemplateCard> findDistinctLastTemplateCardsPrinted() {
		List<TemplateCard> templateCards = new ArrayList<TemplateCard>();
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("SELECT DISTINCT last_card_template_printed FROM user_account");
		List<BigInteger> last_card_template_ids = q.getResultList();
		for(BigInteger id : last_card_template_ids) {
			if(id != null) {
				templateCards.add(TemplateCard.findTemplateCard(id.longValue()));
			}
		}	
		return templateCards;
	}
	
    public static List<Object[]> countNbEuropenCards() {
        EntityManager em = User.entityManager();
        String sql = "SELECT european_student_card, count(*) as count FROM user_account, card WHERE user_account.id= card.user_account AND etat='ENABLED' AND user_type='E' GROUP BY european_student_card ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countNbRoles() {
        EntityManager em = User.entityManager();
        String sql = "SELECT role, count(role) AS count FROM roles, user_account WHERE roles.user_account=user_account.id GROUP BY role ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countNbPendingCards(String userType) {
        EntityManager em = User.entityManager();
        String sql = "SELECT has_card_request_pending, count(*) as count FROM user_account GROUP BY has_card_request_pending ORDER BY count DESC";
        if (!userType.isEmpty()) {
            sql = "SELECT has_card_request_pending, count(*) as count FROM user_account WHERE user_type = :userType GROUP BY has_card_request_pending ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }
    
    public static TypedQuery<User> findUsersByEppnOrEmailEquals(String eppnOrEmail) {
        if (eppnOrEmail == null || eppnOrEmail.length() == 0) throw new IllegalArgumentException("The eppnOrEmail argument is required");
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.eppn = :eppnOrEmail or o.email = :eppnOrEmail", User.class);
        q.setParameter("eppnOrEmail", eppnOrEmail);
        return q;
    }
    
    public static List<User> findUsers4PatchIdentifiersIne() {
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE "
        		+ "o.crous = true "
        		+ "and o.supannCodeINE is not empty "
        		+ "and o.supannCodeINE <> '' "
        		+ "and o.crousIdentifier is not empty "
        		+ "and o.crousIdentifier <> '' "
        		+ "and o.supannCodeINE <> o.crousIdentifier "
        		+ "and o.dueDate > current_date()", User.class);
        return q.getResultList();
    }

	public static Long countFindUsersWithCrousAndWithCardEnabled() {
        EntityManager em = User.entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT count(o) FROM User AS o, Card AS c WHERE "
        		+ "o.crous = true "
        		+ "and c.userAccount = o "
        		+ "and c.etat = 'ENABLED'", Long.class);
        return q.getSingleResult();
	}
	

	public static List<User> findUsersWithCrousAndWithCardEnabled() {
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o, Card AS c WHERE "
        		+ "o.crous = true "
        		+ "and c.userAccount = o "
        		+ "and c.etat = 'ENABLED'", User.class);
        return q.getResultList();
	}
	
    public static TypedQuery<User> findAllUsersQuery() {
    	EntityManager em = User.entityManager();
        return em.createQuery("SELECT o FROM User o", User.class);
    }
    
}

