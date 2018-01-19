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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString(excludeFields={"cards"})
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "UserAccount", finders={"findUsersByEppnEquals", "findUsersByCrous" })
public class User {

	public static enum CnousReferenceStatut {
		psg, etd, prs, hbg, fct, fpa, stg;
	};

    @Column(unique=true)
    private String eppn;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "userAccount")
    @Column
    @OrderBy("requestDate DESC")
    private List<Card> cards = new ArrayList<Card>();
    
	@Column
	private Boolean crous = false;
	
	@Column
	private Boolean europeanStudentCard = false;
	
	@Column
	private Boolean difPhoto = false;

	private String name;
	
	private String firstname;
	
	private Date birthday;
	
	private String institute;
	
	private String eduPersonPrimaryAffiliation;
	
	private String email;
	
	private String rneEtablissement = "";
	
	@Column
	@Enumerated(EnumType.STRING)
	private CnousReferenceStatut cnousReferenceStatut = CnousReferenceStatut.psg;
	
	private Long indice = Long.valueOf(0);
	
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
	
	private String verso1 = "";
	
	private String verso2 = "";
	
	private String verso3 = "";
	
	private String verso4 = "";
	
	private String verso5 = "";
	
	private boolean editable = true;
	
	private boolean requestFree = true;
	
	private String address = "";
	
	private Long nbCards = new Long(0);
	
	private String userType;

	@ElementCollection
    @CollectionTable(name="roles", joinColumns=@JoinColumn(name="user_account"))
    @Column(name="role")
    private Set<String> roles = new HashSet<String>();
	
	@Transient
	private Card externalCard = new Card();
	
	public String getDisplayName() {
		return getName() + " " + getFirstname();
	}
	
	public Boolean hasExternalCard() {
		for(Card card : cards) {
			if(card.getExternal()) {
				return true;
			}
		}
		return false;
	}

	public List<String> getVersoText() {
		String[] versoText = new String[] {verso1, verso2, verso3, verso4, verso5};
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
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (birthday == null) {
			if (other.birthday != null)
				return false;
		} else if (!birthday.equals(other.birthday))
			return false;
		if (cnousReferenceStatut != other.cnousReferenceStatut)
			return false;
		if (crous == null) {
			if (other.crous != null)
				return false;
		} else if (!crous.equals(other.crous))
			return false;
		if (difPhoto == null) {
			if (other.difPhoto != null)
				return false;
		} else if (!difPhoto.equals(other.difPhoto))
			return false;
		if (dueDate == null) {
			if (other.dueDate != null)
				return false;
		} else if (!dueDate.equals(other.dueDate))
			return false;
		if (editable != other.editable)
			return false;
		if (eduPersonPrimaryAffiliation == null) {
			if (other.eduPersonPrimaryAffiliation != null)
				return false;
		} else if (!eduPersonPrimaryAffiliation.equals(other.eduPersonPrimaryAffiliation))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (eppn == null) {
			if (other.eppn != null)
				return false;
		} else if (!eppn.equals(other.eppn))
			return false;
		if (europeanStudentCard == null) {
			if (other.europeanStudentCard != null)
				return false;
		} else if (!europeanStudentCard.equals(other.europeanStudentCard))
			return false;
		if (externalCard == null) {
			if (other.externalCard != null)
				return false;
		} else if (!externalCard.equals(other.externalCard))
			return false;
		if (firstname == null) {
			if (other.firstname != null)
				return false;
		} else if (!firstname.equals(other.firstname))
			return false;
		if (idCompagnyRate == null) {
			if (other.idCompagnyRate != null)
				return false;
		} else if (!idCompagnyRate.equals(other.idCompagnyRate))
			return false;
		if (idRate == null) {
			if (other.idRate != null)
				return false;
		} else if (!idRate.equals(other.idRate))
			return false;
		if (indice == null) {
			if (other.indice != null)
				return false;
		} else if (!indice.equals(other.indice))
			return false;
		if (institute == null) {
			if (other.institute != null)
				return false;
		} else if (!institute.equals(other.institute))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nbCards == null) {
			if (other.nbCards != null)
				return false;
		} else if (!nbCards.equals(other.nbCards))
			return false;
		if (recto1 == null) {
			if (other.recto1 != null)
				return false;
		} else if (!recto1.equals(other.recto1))
			return false;
		if (recto2 == null) {
			if (other.recto2 != null)
				return false;
		} else if (!recto2.equals(other.recto2))
			return false;
		if (recto3 == null) {
			if (other.recto3 != null)
				return false;
		} else if (!recto3.equals(other.recto3))
			return false;
		if (recto4 == null) {
			if (other.recto4 != null)
				return false;
		} else if (!recto4.equals(other.recto4))
			return false;
		if (recto5 == null) {
			if (other.recto5 != null)
				return false;
		} else if (!recto5.equals(other.recto5))
			return false;
		if (requestFree != other.requestFree)
			return false;
		if (rneEtablissement == null) {
			if (other.rneEtablissement != null)
				return false;
		} else if (!rneEtablissement.equals(other.rneEtablissement))
			return false;
		if (secondaryId == null) {
			if (other.secondaryId != null)
				return false;
		} else if (!secondaryId.equals(other.secondaryId))
			return false;
		if (supannCodeINE == null) {
			if (other.supannCodeINE != null)
				return false;
		} else if (!supannCodeINE.equals(other.supannCodeINE))
			return false;
		if (supannEmpId == null) {
			if (other.supannEmpId != null)
				return false;
		} else if (!supannEmpId.equals(other.supannEmpId))
			return false;
		if (supannEntiteAffectationPrincipale == null) {
			if (other.supannEntiteAffectationPrincipale != null)
				return false;
		} else if (!supannEntiteAffectationPrincipale.equals(other.supannEntiteAffectationPrincipale))
			return false;
		if (supannEtuId == null) {
			if (other.supannEtuId != null)
				return false;
		} else if (!supannEtuId.equals(other.supannEtuId))
			return false;
		if (userType == null) {
			if (other.userType != null)
				return false;
		} else if (!userType.equals(other.userType))
			return false;
		if (verso1 == null) {
			if (other.verso1 != null)
				return false;
		} else if (!verso1.equals(other.verso1))
			return false;
		if (verso2 == null) {
			if (other.verso2 != null)
				return false;
		} else if (!verso2.equals(other.verso2))
			return false;
		if (verso3 == null) {
			if (other.verso3 != null)
				return false;
		} else if (!verso3.equals(other.verso3))
			return false;
		if (verso4 == null) {
			if (other.verso4 != null)
				return false;
		} else if (!verso4.equals(other.verso4))
			return false;
		if (verso5 == null) {
			if (other.verso5 != null)
				return false;
		} else if (!verso5.equals(other.verso5))
			return false;
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
        		+ "FROM card, user_account WHERE card.user_account=user_account.id "
        		+ mondayorNot + " GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    
    public static List<Object[]> countYesterdayMajCardsByPopulationCrous(String isMonday) {
        EntityManager em = User.entityManager();
        
        String mondayorNot = " AND to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY')= TIMESTAMP 'yesterday'";
        if("true".equals(isMonday)){
        	mondayorNot = " AND to_char(log_date, 'DD-MM-YYYY') = to_char((now() - interval '3 day'), 'DD-MM-YYYY')";
        }
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count FROM log, user_account "
        			+ " WHERE log.eppn_cible=user_account.eppn " + mondayorNot + " AND action='MAJVERSO' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countMonthCardsByPopulationCrous(String date, String typeDate) {
        EntityManager em = User.entityManager();
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count "
        		+ "FROM card, user_account WHERE card.user_account=user_account.id "
        		+ "AND to_char(" + typeDate + ", 'yyyy-mm-dd') like '" + date + "' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countMonthMajCardsByPopulationCrous(String date) {
        EntityManager em = User.entityManager();

        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count FROM log, user_account " 
        		+ " WHERE log.eppn_cible=user_account.eppn AND to_char(log_date, 'yyyy-mm-dd') LIKE '" + date + "' AND action='MAJVERSO' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countYearEnabledCardsByPopulationCrous(String date, String typeDate) {
        EntityManager em = User.entityManager();
        String majCond = "";
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count FROM card, user_account "
        		+ " WHERE card.user_account=user_account.id AND etat IN ('ENABLED','DISABLED','CADUC') "
        		+ "AND " + typeDate + " >='" + date + "' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    public static List<Object[]> countYearMajEnabledCardsByPopulationCrous(Date date) {
        EntityManager em = User.entityManager();

        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count FROM log, user_account "
        		+ " WHERE log.eppn_cible=user_account.eppn AND log_date >= :date  AND action='MAJVERSO' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);
        q.setParameter("date", date);

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
    
    public static List<Object> countNbVerso5() {
        EntityManager em = User.entityManager();
        String sql = "SELECT CASE WHEN verso5 like '' THEN 'VIDE' ELSE verso5  END AS verso5, COUNT(*) FROM user_account GROUP BY verso5 ORDER BY COUNT DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
	public static List<String> findDistinctUserType() {
		EntityManager em = User.entityManager();
		Query q = em.createNativeQuery("SELECT DISTINCT user_type FROM user_account");
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
            cal.add(Calendar.HOUR, +30);
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
        String sql = "SELECT CASE WHEN request_free THEN 'GRATUIT' ELSE 'PAYANT' END AS request_free, user_type, count(*) FROM user_account GROUP BY request_free, user_type ORDER BY request_free";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
    
    
}

