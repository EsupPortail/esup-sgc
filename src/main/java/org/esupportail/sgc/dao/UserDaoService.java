package org.esupportail.sgc.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.tools.SqlDistinctHackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserDaoService {

    private final static Logger log = LoggerFactory.getLogger(UserDaoService.class);

    @PersistenceContext
    transient EntityManager entityManager;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("log", "DUE_DATE_INCLUDED_DELAY", "BOOLEAN_FIELDS", "eppn", "cards", "crous", "crousError", "crousIdentifier", "europeanStudentCard", "difPhoto", "name", "firstname", "birthday", "institute", "eduPersonPrimaryAffiliation", "email", "rneEtablissement", "cnousReferenceStatut", "indice", "dueDate", "idCompagnyRate", "idRate", "supannEmpId", "supannEtuId", "supannEntiteAffectationPrincipale", "supannCodeINE", "secondaryId", "recto1", "recto2", "recto3", "recto4", "recto5", "recto6", "recto7", "verso1", "verso2", "verso3", "verso4", "verso5", "verso6", "verso7", "editable", "requestFree", "address", "externalAddress", "freeField1", "freeField2", "freeField3", "freeField4", "freeField5", "freeField6", "freeField7", "nbCards", "userType", "templateKey", "lastCardTemplatePrinted", "roles", "externalCard", "blockUserMsg", "hasCardRequestPending", "academicLevel", "importExtCardRight", "newCardRight", "viewRight", "defaultPhoto", "fullText", "pic", "updateDate");

    public long countUsers() {
        return entityManager.createQuery("SELECT COUNT(o) FROM User o", Long.class).getSingleResult();
    }

    public List<User> findAllUsers() {
        return entityManager.createQuery("SELECT o FROM User o", User.class).getResultList();
    }

    public List<User> findAllUsers(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM User o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager.createQuery(jpaQuery, User.class).getResultList();
    }

    public User findUser(Long id) {
        if (id == null) return null;
        return entityManager.find(User.class, id);
    }
    
    public List<String> findAllUsersEppns() {
        return entityManager.createQuery("SELECT o.eppn FROM User o", String.class).getResultList();
    }
    
    public User findUser(String eppn) {
        User user = null;
        List<User> users = findUsersByEppnEquals(eppn).getResultList();
        if(!users.isEmpty()) {
            user = users.get(0);
        }
        return user;
    }


    public List<String> findAllEppns() {
        EntityManager em = entityManager;
        TypedQuery<String> q = em.createQuery("SELECT o.eppn FROM User o", String.class);
        return q.getResultList();
    }


    public TypedQuery<User> findUsersWithNoCards() {
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User o where o.cards IS EMPTY", User.class);
        return q;
    }

    public long countFindUsersWithNoCards() {
        EntityManager em = entityManager;
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM User o where o.cards IS EMPTY", Long.class);
        return q.getSingleResult();
    }


    public List<String> findAllEppnsWithRole(String roleName) {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery("select eppn from user_account u, roles r where r.role=:roleName and u.id=r.user_account");
        q.setParameter("roleName", roleName);
        return q.getResultList();
    }

    public List<String> findDistinctAddresses(String userType, Card.Etat etat) {
        if(etat==null && (userType==null || userType.isEmpty() || "All".equals(userType))) {
            return findDistinctAddresses();
        }
        EntityManager em = entityManager;
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<User> u = query.from(User.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(criteriaBuilder.notEqual(u.get("address"), ""));
        if(etat!=null) {
            Join<User, Card> c = u.join("cards");
            predicates.add(criteriaBuilder.equal(c.get("etat"), etat));
        }
        if(userType != null && !userType.isEmpty() && !"All".equals(userType)) {
            predicates.add(criteriaBuilder.equal(u.get("userType"), userType));
        }
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(criteriaBuilder.asc(u.get("address")));
        query.select(u.get("address")).groupBy(u.get("address"));
        return em.createQuery(query).getResultList();
    }

    public List<String> findDistinctRnecodes() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery("select distinct rne_etablissement from user_account where rne_etablissement is not null and rne_etablissement != ''");
        return q.getResultList();
    }


    public List<Object[]> countNbCrous(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbDifPhoto(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countYesterdayCardsByPopulationCrous(String isMonday, String typeDate) {
        EntityManager em = entityManager;

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

    public List<Object[]> countMonthCardsByPopulationCrous(String date, String typeDate) {
        EntityManager em = entityManager;
        String sql = "SELECT cnous_reference_statut AS eppa, count(*) as count "
                + "FROM card, user_account WHERE card.user_account=user_account.id AND etat IN ('ENABLED','DISABLED','CADUC','ENCODED') "
                + "AND to_char(" + typeDate + ", 'yyyy-mm-dd') like '" + date + "' GROUP BY cnous_reference_statut";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countYearEnabledCardsByPopulationCrous(String date, String typeDate, Date dateFin) {
        EntityManager em = entityManager;
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

    public List<Long> getDistinctNbCards() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("user_account", "nb_cards"));
        List<Long> distinctNbCards = q.getResultList();
        distinctNbCards.remove(0L);
        return distinctNbCards;
    }

    public List<Object[]> countNbCardsByuser(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbEditable() {
        EntityManager em = entityManager;
        String sql = "SELECT CASE WHEN editable = 't' THEN 'Editable' ELSE 'Non editable' END AS etat , count(*) FROM card, user_account WHERE card.user_account=user_account.id AND etat='NEW' GROUP BY editable ORDER BY count";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public Query selectEditableCsv() {
        EntityManager em = entityManager;
        String sql = "SELECT CASE WHEN editable = 't' THEN 'Editable' ELSE 'Non editable' END AS etat, name, firstname, email  FROM card, user_account WHERE card.user_account=user_account.id AND etat='NEW' ORDER BY etat DESC, name";
        Query q = em.createNativeQuery(sql);
        return q;
    }

    public List<String> findDistinctUserType() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("user_account", "user_type"));
        return q.getResultList();
    }

    public List<String> findDistinctAddresses() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("user_account", "address"));
        List<String> addresses = q.getResultList();
        addresses.remove(null);
        addresses.remove("");
        return addresses;
    }

    public List<Object[]> countTarifCrousByType() {
        EntityManager em = entityManager;
        String sql = "SELECT CONCAT(id_compagny_rate, '/', id_rate) as rate, user_type, COUNT(id_rate) FROM user_account WHERE due_date > now() AND id_rate IS NOT NULL GROUP BY rate, user_type ORDER BY rate";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countNextDueDatesOneYearByType() {
        EntityManager em = entityManager;
        String sql = "SELECT to_char(due_date, 'MM-YYYY') tochar, user_type, count(*) FROM user_account WHERE due_date > now() AND due_date < now() + INTERVAL '1 YEAR' GROUP BY tochar, user_type ORDER BY to_date(to_char(due_date, 'MM-YYYY'), 'MM-YYYY')";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countNextDueDatesOneMonthByType() {
        EntityManager em = entityManager;
        String sql = "SELECT to_char(due_date, 'DD-MM-YYYY') tochar, user_type, count(*) FROM user_account WHERE due_date > now() AND due_date < now() + INTERVAL '1 MONTH' GROUP BY tochar, user_type ORDER BY to_date(to_char(due_date, 'DD-MM-YYYY'), 'DD-MM-YYYY')";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }


    public List<Object[]> countNbRequestFree() {
        EntityManager em = entityManager;
        String sql = "SELECT CASE WHEN request_free THEN 'GRATUIT' ELSE 'PAYANT' END AS request_free, user_type, count(*) FROM user_account WHERE user_type IS NOT NULL GROUP BY request_free, user_type ORDER BY request_free";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<String> getDistinctFreeField(String field) {
        EntityManager em = entityManager;
        // FormService.getField1List uses its preventing sql injection
        String req = "SELECT DISTINCT CAST(" + field + " AS VARCHAR) FROM user_account WHERE " + field  + " IS NOT NULL ORDER BY " + field;
        Query q = em.createNativeQuery(req);
        List<String> distinctResults = q.getResultList();
        return distinctResults;
    }

    public Long getCountDistinctFreeField(String field) {
        EntityManager em = entityManager;
        // FormService.getField1List uses its preventing sql injection
        String req = "SELECT count(DISTINCT(" + field + ")) FROM user_account WHERE " + field  + " IS NOT NULL";
        Query q = em.createNativeQuery(req);
        return ((Long)q.getSingleResult());
    }

    public List<Object[]> countNbEuropenCards() {
        EntityManager em = entityManager;
        String sql = "SELECT european_student_card, count(*) as count FROM user_account, card WHERE user_account.id= card.user_account AND etat='ENABLED' AND user_type='E' GROUP BY european_student_card ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countNbRoles() {
        EntityManager em = entityManager;
        String sql = "SELECT role, count(role) AS count FROM roles, user_account WHERE roles.user_account=user_account.id GROUP BY role ORDER BY count DESC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countNbPendingCards(String userType) {
        EntityManager em = entityManager;
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

    public TypedQuery<User> findUsersByEppnOrEmailEquals(String eppnOrEmail) {
        if (eppnOrEmail == null || eppnOrEmail.length() == 0) throw new IllegalArgumentException("The eppnOrEmail argument is required");
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.eppn = :eppnOrEmail or o.email = :eppnOrEmail", User.class);
        q.setParameter("eppnOrEmail", eppnOrEmail);
        return q;
    }

    public List<User> findUsers4PatchIdentifiersIne() {
        EntityManager em = entityManager;
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

    public Long countFindUsersWithCrousAndWithCardEnabled() {
        EntityManager em = entityManager;
        TypedQuery<Long> q = em.createQuery("SELECT count(o) FROM User AS o, Card AS c WHERE "
                + "o.crous = true "
                + "and c.userAccount = o "
                + "and c.etat = 'ENABLED'", Long.class);
        return q.getSingleResult();
    }


    public List<User> findUsersWithCrousAndWithCardEnabled() {
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o, Card AS c WHERE "
                + "o.crous = true "
                + "and c.userAccount = o "
                + "and c.etat = 'ENABLED'", User.class);
        return q.getResultList();
    }

    public TypedQuery<User> findAllUsersQuery() {
        EntityManager em = entityManager;
        return em.createQuery("SELECT o FROM User o", User.class);
    }

    public TypedQuery<User> findAllUsersWithDueDateBeforeAndDueDateAfterNow(LocalDateTime date) {
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User o WHERE dueDate < :date and dueDate > :now", User.class);
        q.setParameter("date", date);
        q.setParameter("now", LocalDateTime.now());
        return q;
    }


    public Long countFindUsersByCrous(Boolean crous) {
        if (crous == null) throw new IllegalArgumentException("The crous argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM User AS o WHERE o.crous = :crous", Long.class);
        q.setParameter("crous", crous);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<User> findUsersByCrous(Boolean crous) {
        if (crous == null) throw new IllegalArgumentException("The crous argument is required");
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.crous = :crous", User.class);
        q.setParameter("crous", crous);
        return q;
    }

    public TypedQuery<User> findUsersByCrousIdentifier(String crousIdentifier) {
        if (crousIdentifier == null || crousIdentifier.length() == 0) throw new IllegalArgumentException("The crousIdentifier argument is required");
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.crousIdentifier = :crousIdentifier", User.class);
        q.setParameter("crousIdentifier", crousIdentifier);
        return q;
    }

    public TypedQuery<User> findUsersByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.eppn = :eppn", User.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public TypedQuery<User> findUsersByEuropeanStudentCard(Boolean europeanStudentCard) {
        if (europeanStudentCard == null) throw new IllegalArgumentException("The europeanStudentCard argument is required");
        EntityManager em = entityManager;
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.europeanStudentCard = :europeanStudentCard", User.class);
        q.setParameter("europeanStudentCard", europeanStudentCard);
        return q;
    }


    @Transactional
    public void persist(User user) {
        this.entityManager.persist(user);
    }

    @Transactional
    public void remove(User user) {
        if (this.entityManager.contains(user)) {
            this.entityManager.remove(user);
        } else {
            User attached = findUser(user.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public User merge(User user) {
        User merged = this.entityManager.merge(user);
        this.entityManager.flush();
        return merged;
    }

}
