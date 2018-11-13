package org.esupportail.sgc.domain;
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
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

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
@RooJpaActiveRecord(versionField = "", table = "UserAccount", finders={"findUsersByEppnEquals", "findUsersByCrous" })
@JsonFilter("userFilter")
public class User {
	
	private final static Logger log = LoggerFactory.getLogger(User.class);

	// @see getDueDateIncluded()
	public final static int DUE_DATE_INCLUDED_DELAY = +30;
	
	public static enum CnousReferenceStatut {
		psg, etd, prs, hbg, fct, fpa, stg;
	};

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
	
	public static List<String> findAllEppnsWithRole(String roleName) {
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("select eppn from user_account u, roles r where r.role=:roleName and u.id=r.user_account");
		q.setParameter("roleName", roleName);
		return q.getResultList();
	}
	
	public static List<String> findDistinctAddresses(String userType, String etat) {
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("SELECT distinct(address) FROM user_account WHERE address <> '' order by address");
		if(etat==null || etat.isEmpty()){
			if(userType != null && !userType.isEmpty()) {
				 q = em.createNativeQuery("SELECT distinct(address) FROM user_account WHERE address <> '' and user_type = (:userType) order by address");
				 q.setParameter("userType", userType);
			}
		}else{
			String sql = "SELECT DISTINCT address FROM user_account, card WHERE user_account.id= card.user_account AND address <> '' AND etat=:etat";
			if(userType != null && !userType.isEmpty()) {
				sql+= " AND user_type = (:userType)";
			}
			sql+= " ORDER BY address";
			q = em.createNativeQuery(sql);
			q.setParameter("etat", etat);
			if(userType != null && !userType.isEmpty()) {
				 q.setParameter("userType", userType);
			}
		}
		return q.getResultList();
	}
	

	public boolean fieldsEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (address == null) {
			if (other.address != null)
				{log.trace("address <>"); return false;}
		} else if (!address.equals(other.address))
			{log.trace("address <>"); return false;}
		if (birthday == null) {
			if (other.birthday != null)
				{log.trace("birthday <>"); return false;}
		} 
		// compare only day (without time) for birthday 
		else if (DateTimeComparator.getDateOnlyInstance().compare(birthday, other.birthday) != 0)
			{log.trace("birthday <>"); return false;}
		if (cnousReferenceStatut != other.cnousReferenceStatut)
			{log.trace("cnousReferenceStatut <>"); return false;}
		if (crous == null) {
			if (other.crous != null)
				{log.trace("crous <>"); return false;}
		} else if (!crous.equals(other.crous))
			{log.trace("crous <>"); return false;}
		if (difPhoto == null) {
			if (other.difPhoto != null)
				{log.trace("difPhoto <>"); return false;}
		} else if (!difPhoto.equals(other.difPhoto))
			{log.trace("difPhoto <>"); return false;}
		if (dueDate == null) {
			if (other.dueDate != null)
				{log.trace("dueDate <>"); return false;}
		} else if (!dueDate.equals(other.dueDate))
			{log.trace("dueDate <>"); return false;}
		if (editable != other.editable)
			{log.trace("editable <>"); return false;}
		if (eduPersonPrimaryAffiliation == null) {
			if (other.eduPersonPrimaryAffiliation != null)
				{log.trace("eduPersonPrimaryAffiliation <>"); return false;}
		} else if (!eduPersonPrimaryAffiliation.equals(other.eduPersonPrimaryAffiliation))
			{log.trace("eduPersonPrimaryAffiliation <>"); return false;}
		if (email == null) {
			if (other.email != null)
				{log.trace("email <>"); return false;}
		} else if (!email.equals(other.email))
			{log.trace("email <>"); return false;}
		if (eppn == null) {
			if (other.eppn != null)
				{log.trace("eppn <>"); return false;}
		} else if (!eppn.equals(other.eppn))
			{log.trace("eppn <>"); return false;}
		if (europeanStudentCard == null) {
			if (other.europeanStudentCard != null)
				{log.trace("europeanStudentCard <>"); return false;}
		} else if (!europeanStudentCard.equals(other.europeanStudentCard))
			{log.trace("europeanStudentCard <>"); return false;}
		if (externalCard == null) {
			if (other.externalCard != null)
				{log.trace("externalCard <>"); return false;}
		} else if (!externalCard.equals(other.externalCard))
			{log.trace("externalCard <>"); return false;}
		if (firstname == null) {
			if (other.firstname != null)
				{log.trace("firstname <>"); return false;}
		} else if (!firstname.equals(other.firstname))
			{log.trace("firstname <>"); return false;}
		if (idCompagnyRate == null) {
			if (other.idCompagnyRate != null)
				{log.trace("idCompagnyRate <>"); return false;}
		} else if (!idCompagnyRate.equals(other.idCompagnyRate))
			{log.trace("idCompagnyRate <>"); return false;}
		if (idRate == null) {
			if (other.idRate != null)
				{log.trace("idRate <>"); return false;}
		} else if (!idRate.equals(other.idRate))
			{log.trace("idRate <>"); return false;}
		if (indice == null) {
			if (other.indice != null)
				{log.trace("indice <>"); return false;}
		} else if (!indice.equals(other.indice))
			{log.trace("indice <>"); return false;}
		if (institute == null) {
			if (other.institute != null)
				{log.trace("institute <>"); return false;}
		} else if (!institute.equals(other.institute))
			{log.trace("institute <>"); return false;}
		if (name == null) {
			if (other.name != null)
				{log.trace("name <>"); return false;}
		} else if (!name.equals(other.name))
			{log.trace("name <>"); return false;}
		if (nbCards == null) {
			if (other.nbCards != null)
				{log.trace("nbCards <>"); return false;}
		} else if (!nbCards.equals(other.nbCards))
			{log.trace("nbCards <>"); return false;}
		if (recto1 == null) {
			if (other.recto1 != null)
				{log.trace("recto1 <>"); return false;}
		} else if (!recto1.equals(other.recto1))
			{log.trace("recto1 <>"); return false;}
		if (recto2 == null) {
			if (other.recto2 != null)
				{log.trace("recto2 <>"); return false;}
		} else if (!recto2.equals(other.recto2))
			{log.trace("recto2 <>"); return false;}
		if (recto3 == null) {
			if (other.recto3 != null)
				{log.trace("recto3 <>"); return false;}
		} else if (!recto3.equals(other.recto3))
			{log.trace("recto3 <>"); return false;}
		if (recto4 == null) {
			if (other.recto4 != null)
				{log.trace("recto4 <>"); return false;}
		} else if (!recto4.equals(other.recto4))
			{log.trace("recto4 <>"); return false;}
		if (recto5 == null) {
			if (other.recto5 != null)
				{log.trace("recto5 <>"); return false;}
		} else if (!recto5.equals(other.recto5))
			{log.trace("recto5 <>"); return false;}
		if (recto6 == null) {
			if (other.recto6 != null)
				{log.trace("recto6 <>"); return false;}
		} else if (!recto6.equals(other.recto6))
			{log.trace("recto6 <>"); return false;}
		if (recto7 == null) {
			if (other.recto7 != null)
				{log.trace("recto7 <>"); return false;}
		} else if (!recto7.equals(other.recto7))
			{log.trace("recto7 <>"); return false;}
		if (requestFree != other.requestFree)
			{log.trace("requestFree <>"); return false;}
		if (rneEtablissement == null) {
			if (other.rneEtablissement != null)
				{log.trace("rneEtablissement <>"); return false;}
		} else if (!rneEtablissement.equals(other.rneEtablissement))
			{log.trace("requestFree <>"); return false;}
		if (secondaryId == null) {
			if (other.secondaryId != null)
				{log.trace("secondaryId <>"); return false;}
		} else if (!secondaryId.equals(other.secondaryId))
			{log.trace("secondaryId <>"); return false;}
		if (supannCodeINE == null) {
			if (other.supannCodeINE != null)
				{log.trace("supannCodeINE <>"); return false;}
		} else if (!supannCodeINE.equals(other.supannCodeINE))
			{log.trace("supannCodeINE <>"); return false;}
		if (supannEmpId == null) {
			if (other.supannEmpId != null)
				{log.trace("supannEmpId <>"); return false;}
		} else if (!supannEmpId.equals(other.supannEmpId))
			{log.trace("supannEmpId <>"); return false;}
		if (supannEntiteAffectationPrincipale == null) {
			if (other.supannEntiteAffectationPrincipale != null)
				{log.trace(" <>"); return false;}
		} else if (!supannEntiteAffectationPrincipale.equals(other.supannEntiteAffectationPrincipale))
			{log.trace("supannEntiteAffectationPrincipale <>"); return false;}
		if (supannEtuId == null) {
			if (other.supannEtuId != null)
				{log.trace("supannEtuId <>"); return false;}
		} else if (!supannEtuId.equals(other.supannEtuId))
			{log.trace("supannEtuId <>"); return false;}
		if (userType == null) {
			if (other.userType != null)
				{log.trace("userType <>"); return false;}
		} else if (!userType.equals(other.userType))
			{log.trace("userType <>"); return false;}
		if (verso1 == null) {
			if (other.verso1 != null)
				{log.trace("verso1 <>"); return false;}
		} else if (!verso1.equals(other.verso1))
			{log.trace("verso1 <>"); return false;}
		if (verso2 == null) {
			if (other.verso2 != null)
				{log.trace("verso2 <>"); return false;}
		} else if (!verso2.equals(other.verso2))
			{log.trace("verso2 <>"); return false;}
		if (verso3 == null) {
			if (other.verso3 != null)
				{log.trace("verso3 <>"); return false;}
		} else if (!verso3.equals(other.verso3))
			{log.trace("verso3 <>"); return false;}
		if (verso4 == null) {
			if (other.verso4 != null)
				{log.trace("verso4 <>"); return false;}
		} else if (!verso4.equals(other.verso4))
			{log.trace("verso4 <>"); return false;}
		if (verso5 == null) {
			if (other.verso5 != null)
				{log.trace("verso5 <>"); return false;}
		} else if (!verso5.equals(other.verso5))
			{log.trace("verso5 <>"); return false;}
		if (verso6 == null) {
			if (other.verso6 != null)
				{log.trace("verso6 <>"); return false;}
		} else if (!verso6.equals(other.verso6))
			{log.trace("verso6 <>"); return false;}
		if (verso7 == null) {
			if (other.verso7 != null)
				{log.trace("verso7 <>"); return false;}
		} else if (!verso7.equals(other.verso7))
			{log.trace("verso7 <>"); return false;}
		if (templateKey == null) {
			if (other.templateKey != null)
				{log.trace("templateKey <>"); return false;}
		} else if (!templateKey.equals(other.templateKey))
			{log.trace("templateKey <>"); return false;}
		if (blockUserMsg == null) {
			if (other.blockUserMsg != null)
				{log.trace("blockUserMsg <>"); return false;}
		} else if (!blockUserMsg.equals(other.blockUserMsg))
			{log.trace("blockUserMsg <>"); return false;}
		return true;
	}

	/***Stats****/
	
    public static List<Object> countNbCrous(String userType) {
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
    
    public static List<Object> countNbDifPhoto(String userType) {
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
	
	public static List<Object> countNbCardsByuser(String userType) {
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
	
    public static List<Object> countNbEditable() {
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
     * @return Date + 30H00 - so that 31/08/2017 is 01/09/2017 - 06H00
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
	
    public static List<Object> countTarifCrousByType() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CONCAT(id_rate, '/', id_compagny_rate) as rate, user_type, COUNT(id_rate) FROM user_account WHERE id_rate IS NOT NULL GROUP BY rate, user_type ORDER BY rate";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object> countNbRequestFree() {
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
		EntityManager em = Card.entityManager();
		// FormService.getField1List uses its preventing sql injection
		String req = "SELECT DISTINCT CAST(" + field + " AS VARCHAR) FROM user_account WHERE " + field  + " IS NOT NULL ORDER BY " + field;
		Query q = em.createNativeQuery(req);
		List<String> distinctResults = q.getResultList();
		return distinctResults;
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
	
    public static List<Object> countNbEuropenCards() {
        EntityManager em = User.entityManager();
        String sql = "SELECT european_student_card, count(*) as count FROM user_account, card WHERE user_account.id= card.user_account AND etat='ENABLED' AND user_type='E' GROUP BY european_student_card ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object> countNbRoles() {
        EntityManager em = User.entityManager();
        String sql = "SELECT role, count(role) AS count FROM roles, user_account WHERE roles.user_account=user_account.id GROUP BY role ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object> countNbPendingCards(String userType) {
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
    
}

