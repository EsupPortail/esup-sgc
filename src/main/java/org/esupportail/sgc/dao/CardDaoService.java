package org.esupportail.sgc.dao;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.tools.SqlDistinctHackUtils;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CardDaoService {

    static final List<String> fieldNames4OrderCard = java.util.Arrays.asList("id", "eppn", "crous", "etat", "dateEtat", "commentaire",
            "flagAdresse", "adresse", "structure", "requestDate", "nbRejets", "lastEncodedDate", "dueDate", "etatEppn", "deliveredDate", "motifDisable", "payCmdNum");

    static final List<String> fieldNames4OrderUser = java.util.Arrays.asList("displayName", "nbCards", "address", "updateDate", "nbResyncSuccessives",
            "userType", "firstname", "name", "email", "birthday", "institute", "eduPersonPrimaryAffiliation", "rneEtablissement", "idCompagnyRate", "idRate",
            "supannEmpId", "supannEtuId", "supannEntiteAffectationPrincipale", "supannCodeINE", "secondaryId", "externalAddress",
            "recto1", "recto2", "recto3", "recto4", "recto5", "recto6", "recto7",
            "verso1", "verso2", "verso3", "verso4", "verso5", "verso6", "verso7",
            "freeField1", "freeField2", "freeField3", "freeField4", "freeField5");

    private static final Logger log = LoggerFactory.getLogger(CardDaoService.class);

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    TemplateCardDaoService templateCardDaoService;

    @Resource
    UserDaoService userDaoService;


    public Card findCardByCsn(String csn) {
        List<Card> cards = this.findCardsByCsn(csn).getResultList();
        if (cards.isEmpty()) {
            return null;
        } else {
            return cards.get(0);
        }
    }

    public TypedQuery<Card> findCardsByEtatIn(List<Card.Etat> etats) {
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.etat IN (:etats) order by dateEtat desc", Card.class);
        q.setParameter("etats", etats);
        return q;
    }

    public TypedQuery<Card> findCardsByEppnInAndEtatIn(List<String> eppns, List<Card.Etat> etats) {
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.eppn IN (:eppns) AND o.etat IN (:etats) order by dateEtat desc", Card.class);
        q.setParameter("eppns", eppns);
        q.setParameter("etats", etats);
        return q;
    }

    public Long countfindCardsByEppnEqualsAndEtatIn(String eppn, List<Card.Etat> etats) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat IN (:etats)", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etats", etats);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Card> findCardsByEtatEppnEqualsAndEtatEquals(String printerEppn, Card.Etat etat) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT o FROM Card AS o WHERE o.printerEppn = :printerEppn AND o.etat = :etat ORDER BY o.dateEtat asc", Card.class);
        q.setParameter("printerEppn", printerEppn);
        q.setParameter("etat", etat);
        return  q;
    }

    public Long countfindCardsByEtatEppnEqualsAndEtatEquals(String etatEppn, Card.Etat etat) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.etatEppn = :etatEppn AND o.etat = :etat", Long.class);
        q.setParameter("etatEppn", etatEppn);
        q.setParameter("etat", etat);
        return ((Long) q.getSingleResult());
    }

    public Long countfindCardsByEppnEqualsAndEtatNotIn(String eppn, List<Card.Etat> etats) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat NOT IN (:etats)", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etats", etats);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Card> findCardsByQrcodeAndEtatIn(String qrcode, List<Card.Etat> etats) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT o FROM Card AS o WHERE o.qrcode = :qrcode AND o.etat IN (:etats) order by dateEtat desc", Card.class);
        q.setParameter("qrcode", qrcode);
        q.setParameter("etats", etats);
        return q;
    }


    public TypedQuery<Card> findCardsByDesfireIdAndAppNameEquals(String desfireId, String appName) {
        if (desfireId == null || desfireId.length() == 0) throw new IllegalArgumentException("The desfireId argument is required");
        if (appName == null || appName.length() == 0) throw new IllegalArgumentException("The appName argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Card AS o JOIN o.desfireIds d WHERE key(d) = :appName AND value(d) = :desfireId");
        TypedQuery<Card> q = em.createQuery(queryBuilder.toString(), Card.class);
        q.setParameter("appName", appName);
        q.setParameter("desfireId", desfireId);
        return q;
    }

    /*
        * Search cards with CardSearchBean criteria
        * eppn is used to filter own or free cards if specified in searchBean
     */
    public TypedQuery<Card> findCards(CardSearchBean searchBean, String eppn, SortCriterion... sortCriteria) {
        EntityManager em = entityManager;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Card> query = cb.createQuery(Card.class);
        Root<Card> c = query.from(Card.class);

        List<Order> orders = new ArrayList<>();
        Join<Card, User> userJoin = null; // Lazy join

        // Traiter les critères de tri demandés
        for (SortCriterion criterion : sortCriteria) {
            userJoin = addSortOrders(cb, c, orders, criterion, userJoin);
        }

        // Ajouter les tris par défaut
        if (!searchBean.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchBean.getSearchText());
            Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(cb, searchString);
            orders.add(cb.desc(fullTestSearchRanking));
        }
        orders.add(cb.desc(c.get("dateEtat")));
        orders.add(cb.desc(c.get("id")));

        List<Predicate> predicates = getPredicates4CardSearchBean(searchBean, eppn, cb, c);
        query.where(cb.and(predicates.toArray(new Predicate[0])));
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

    private Join<Card, User> addSortOrders(CriteriaBuilder cb, Root<Card> c,
                                           List<Order> orders, SortCriterion criterion,
                                           Join<Card, User> existingJoin) {
        if(criterion == null || StringUtils.isBlank(criterion.getFieldName())) {
            return existingJoin;
        }
        String fieldName = criterion.getFieldName();
        boolean isDesc = criterion.isDesc();
        Join<Card, User> userJoin = existingJoin;

        // Champs directs sur Card
        if (fieldNames4OrderCard.contains(fieldName)) {
            orders.add(isDesc ? cb.desc(c.get(fieldName)) : cb.asc(c.get(fieldName)));
            return userJoin;
        }

        // Champs nécessitant un join sur User
        if (userJoin == null) {
            userJoin = c.join("userAccount");
        }

        if("displayName".equals(fieldName)) {
            orders.add(isDesc ? cb.desc(userJoin.get("name")) : cb.asc(userJoin.get("name")));
            orders.add(isDesc ? cb.desc(userJoin.get("firstname")) : cb.asc(userJoin.get("firstname")));
        } else if(fieldNames4OrderUser.contains(fieldName)) {
            orders.add(isDesc ? cb.desc(userJoin.get(fieldName)) : cb.asc(userJoin.get(fieldName)));
        }
        return userJoin;
    }

    public long countFindCards(CardSearchBean searchBean, String eppn) {
        EntityManager em = entityManager;
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<Card> c = query.from(Card.class);

        final List<Predicate> predicates = getPredicates4CardSearchBean(searchBean, eppn, criteriaBuilder, c);

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        if (searchBean.getSearchText().isEmpty() && searchBean.getFreeField() != null && searchBean.getFreeField().values().contains("desfire_ids")) {
            // hack : use distinct because of join on desfireIds
            query.select(criteriaBuilder.countDistinct(c));
        } else {
            query.select(criteriaBuilder.count(c));
        }
        return em.createQuery(query).getSingleResult();
    }


    public static String snakeToCamel(String snake){
        String camelString = "";
        camelString = WordUtils.capitalize(snake, "_".toCharArray()).replace("_", "");
        char ch[] = camelString.toCharArray();
        ch[0] = Character.toLowerCase(ch[0]);
        camelString = new String(ch);
        return camelString;
    }

    protected List<Predicate> getPredicates4CardSearchBean(CardSearchBean searchBean, String eppn,
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
                                        TemplateCard templateCard = templateCardDaoService.findTemplateCard(Long.valueOf(v));
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

    /***Stats****/
    public List<Object[]> countNbCardsByYearEtat(String userType, String etatCase) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsByDay(String userType, String typeDate) {
        String sql = "SELECT to_date(to_char(" + typeDate + ", 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - " + typeDate + ") < 31 GROUP BY day ORDER BY day";
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsByEtat(String userType) {
        EntityManager em = entityManager;
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

    public List<Card> findAllCards(List<Long> cardIds) {
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.id in (:cardIds)", Card.class);
        q.setParameter("cardIds", cardIds);
        return q.getResultList();
    }

    public List<Object[]> countNbCardsByMotifsDisable(String userType, String motifCase) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsByMonthYear(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsEncodedByMonthYear(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsEditedByYear(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbCardsEnabledEncodedByYear(String userType) {
        EntityManager em = entityManager;
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

    public List<Object[]> countNbDeliverdCardsByDay(String userType) {
        String sql = "SELECT to_date(to_char(delivered_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - delivered_date) < 31 GROUP BY day ORDER BY day";
        EntityManager em = entityManager;
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

    public List<String> findDistinctEtats() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("card", "etat"));
        return q.getResultList();
    }

    public List<String> findDistinctUserTypes(List<Long> cardIds) {
        if(cardIds == null || cardIds.isEmpty()) return new ArrayList<String>();
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT DISTINCT(u.userType) FROM User AS u WHERE u.eppn IN (SELECT c.eppn FROM Card as c WHERE c.id IN (:cardIds))", String.class);
        q.setParameter("cardIds", cardIds);
        return q.getResultList();
    }

    public Long countNBCardsByEppn(String eppn) {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery("SELECT count (*) From Card WHERE eppn=:eppn");
        q.setParameter("eppn", eppn);
        return Long.valueOf(String.valueOf(q.getSingleResult()));
    }

    public List<Object[]> countNbEncodedCardsByDay(String userType) {
        String sql = "SELECT to_date(to_char(encoded_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) as count FROM card WHERE DATE_PART('days', now() - encoded_date) < 31 GROUP BY day ORDER BY day";
        EntityManager em = entityManager;
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

    public List<Long> getDistinctNbRejets() {
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("card", "nb_rejets"));
        List<Long> distinctNbRejets = q.getResultList();
        distinctNbRejets.remove(0L);
        return distinctNbRejets;
    }

    public List<Object[]> countBrowserStats(String userType) {
        String sql = "SELECT CASE WHEN request_browser LIKE '%Firefox%' THEN 'Firefox' WHEN request_browser LIKE '%Chrome%' THEN 'Chrome' " + "WHEN request_browser LIKE '%Explorer%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%IE%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%Apple%' THEN 'Safari' " + "WHEN request_browser LIKE '%Safari%' THEN 'Safari' " + "WHEN request_browser LIKE '%Edge%' THEN 'Microsoft Edge' " + "WHEN request_browser LIKE '%Opera%' THEN 'Opera' ELSE request_browser END  AS browser , " + "COUNT(*) AS count FROM card WHERE request_browser IS NOT NULL GROUP BY browser ORDER BY count DESC";
        EntityManager em = entityManager;
        if (!userType.isEmpty()) {
            sql = "SELECT CASE WHEN request_browser LIKE '%Firefox%' THEN 'Firefox' WHEN request_browser LIKE '%Chrome%' THEN 'Chrome' " + "WHEN request_browser LIKE '%Explorer%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%IE%' THEN 'Internet Explorer' " + "WHEN request_browser LIKE '%Apple%' THEN 'Safari' " + "WHEN request_browser LIKE '%Safari%' THEN 'Safari' " + "WHEN request_browser LIKE '%Edge%' THEN 'Microsoft Edge' " + "WHEN request_browser LIKE '%Opera%' THEN 'Opera' ELSE request_browser END  AS browser , " + "COUNT(*) AS count FROM card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_browser IS NOT NULL GROUP BY browser ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public List<Object[]> countOsStats(String userType) {
        String sql = "SELECT CASE WHEN request_os LIKE '%iPhone%' THEN 'Smartphone' " + "WHEN request_os LIKE 'Android%x' THEN 'Smartphone' " + "WHEN request_os LIKE '%Phone%' THEN 'Smartphone' WHEN request_os LIKE '%iPad%' THEN 'Tablette' " + "WHEN request_os  LIKE '%Tablet%' THEN 'Tablette' WHEN request_os LIKE '%Touch%' THEN 'Tablette' " + "ELSE 'Desktop' END  AS os, count(*) as count FROM Card WHERE request_os IS NOT NULL GROUP BY os ORDER BY count DESC";
        EntityManager em = entityManager;
        if (!userType.isEmpty()) {
            sql = "SELECT CASE WHEN request_os LIKE '%iPhone%' THEN 'Smartphone' " + "WHEN request_os LIKE 'Android%x' THEN 'Smartphone' " + "WHEN request_os LIKE '%Phone%' THEN 'Smartphone' WHEN request_os LIKE '%iPad%' THEN 'Tablette' " + "WHEN request_os  LIKE '%Tablet%' THEN 'Tablette' WHEN request_os LIKE '%Touch%' THEN 'Tablette' " + "ELSE 'Desktop' END  AS os, count(*) as count FROM Card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_os IS NOT NULL GROUP BY os ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public List<Object[]> countRealOsStats(String userType) {
        String sql = "SELECT request_os, count(*) as count FROM Card WHERE request_os IS NOT NULL GROUP BY request_os ORDER BY count DESC";
        EntityManager em = entityManager;
        if (!userType.isEmpty()) {
            sql = "SELECT request_os, count(*) as count FROM Card, user_account WHERE card.user_account=user_account.id AND user_type = :userType " + "AND request_os IS NOT NULL GROUP BY request_os ORDER BY count DESC";
        }
        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public List<Object[]> countNbEditedCardNotDelivered(String typeCase) {
        EntityManager em = entityManager;

        String sql = "SELECT CASE WHEN(DATE_PART('month', encoded_date)<7) "
                + "THEN CONCAT(CAST(DATE_PART('year', encoded_date)-1 AS TEXT),'-',CAST(DATE_PART('year', encoded_date) AS TEXT)) "
                + "ELSE CONCAT(CAST(DATE_PART('year', encoded_date) AS TEXT),'-',CAST(DATE_PART('year', encoded_date)+1 AS TEXT)) END AS Saison, " + typeCase + ", count(*) FROM card, user_account WHERE card.user_account= user_account.id AND delivered_date is null AND etat IN ('ENABLED', 'ENCODED') AND user_type NOT LIKE '' GROUP BY Saison, user_type ORDER by Saison";
        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public List<Object[]> countNbCardsByRejets(String userType) {
        EntityManager em = entityManager;

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

    public TypedQuery<Card> findCardsByCsn(String csn) {
        if (csn == null || csn.length() == 0) throw new IllegalArgumentException("The csn argument is required");
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.csn = upper(:csn)", Card.class);
        q.setParameter("csn", csn);
        return q;
    }

    public List<Object[]> countDeliveryByAddress() {
        EntityManager em = entityManager;
        String sql = "SELECT address, count(*) FROM card INNER JOIN user_account ON card.user_account=user_account.id AND delivered_date is null GROUP BY address ORDER BY count DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public List<Object[]> countNonEditableByAddress() {
        EntityManager em = entityManager;
        String sql = "SELECT address, count(*) FROM card INNER JOIN user_account ON card.user_account=user_account.id AND not editable and etat='NEW' GROUP BY address ORDER BY count DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public Boolean areCardsReadyToBeDelivered(List<Long> cardIds) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.id in (:cardIds) and o.etat in (:etatsEncoded) AND o.deliveredDate IS NULL AND NOT o.external", Long.class);
        q.setParameter("cardIds", cardIds);
        q.setParameter("etatsEncoded", CardEtatService.etatsEncoded);
        return !Long.valueOf(0).equals(((Long) q.getSingleResult()));
    }

    public Boolean areCardsReadyToBeValidated(List<Long> cardIds) {
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.id in (:cardIds) and o.etat in (:etatsValidateDisabled)", Long.class);
        q.setParameter("cardIds", cardIds);
        q.setParameter("etatsValidateDisabled", Arrays.asList(new Card.Etat[] {Card.Etat.ENABLED, Card.Etat.DISABLED, Card.Etat.CADUC}));
        return !Long.valueOf(0).equals(((Long) q.getSingleResult()));
    }

    /**
     * Hack : ids are sorted so that ENABLED card are at end of the list
     */
    public List<Long> findAllCardIds() {
        EntityManager em = entityManager;
        String sql = "SELECT id FROM card order by (case etat when 'ENABLED' then 2 else 1 end)";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public List<Object[]> countNbCardRequestByMonth(String userType) {
        String sql = "SELECT to_char(request_date, 'MM-YYYY') tochar, count(*) FROM card GROUP BY tochar ORDER BY to_date(to_char(request_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(request_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(request_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = entityManager;

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public List<Object[]> countNbCardEncodedByMonth(String userType) {
        String endDate = "";
        String sql = "SELECT to_char(encoded_date, 'MM-YYYY') tochar, count(*) FROM card GROUP BY tochar ORDER BY to_date(to_char(encoded_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(encoded_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id  AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(encoded_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = entityManager;

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }
        return q.getResultList();
    }

    public List<Object[]> countNbRejetsByMonth(String userType) {
        String sql = "SELECT to_char(date_etat, 'MM-YYYY') tochar, count(*) FROM card WHERE etat='REJECTED' GROUP BY tochar ORDER BY to_date(to_char(date_etat, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(date_etat, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id " + "AND etat='REJECTED' AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(date_etat, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = entityManager;

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }

        return q.getResultList();
    }

    public List<Object[]> countDueDatesByDate(String userType) {
        String sql = "SELECT to_char(user_account.due_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account=user_account.id AND etat IN ('NEW','REJECTED','RENEWED') GROUP BY tochar ORDER BY to_date(to_char(user_account.due_date, 'MM-YYYY'), 'MM-YYYY')";
        if (!userType.isEmpty()) {
            sql = "SELECT to_char(user_account.due_date, 'MM-YYYY') tochar, count(*) FROM card, user_account WHERE card.user_account= user_account.id AND etat IN ('NEW','REJECTED','RENEW') AND user_type = :userType GROUP BY tochar ORDER BY to_date(to_char(user_account.due_date, 'MM-YYYY'), 'MM-YYYY')";
        }
        EntityManager em = entityManager;

        Query q = em.createNativeQuery(sql);
        if (!userType.isEmpty()) {
            q.setParameter("userType", userType);
        }

        return q.getResultList();
    }

    public List<String> getDistinctFreeField(String field) {
        EntityManager em = entityManager;
        // FormService.getField1List uses its preventing sql injection
        String req = "SELECT DISTINCT CAST(" + field + " AS VARCHAR) FROM card WHERE " + field  + " IS NOT NULL ORDER BY " + field;
        Query q = em.createNativeQuery(req);
        List<String> distinctResults = q.getResultList();
        return distinctResults;
    }

    public Long getCountDistinctFreeField(String field) {
        EntityManager em = entityManager;
        // FormService.getField1List uses its preventing sql injection
        String req = "SELECT count(DISTINCT(" + field + ")) FROM card WHERE " + field  + " IS NOT NULL";
        Query q = em.createNativeQuery(req);
        return (Long)q.getSingleResult();
    }

    public TypedQuery<Card> findCardsWithEscnAndCsn() {
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.escnUid IS NOT NULL and o.escnUid IS NOT EMPTY AND o.csn IS NOT NULL and o.csn IS NOT EMPTY", Card.class);
        return q;
    }

    public Card findOneCardForTemplate(TemplateCard templateCard) {
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.templateCard=:templateCard", Card.class);
        q.setParameter("templateCard", templateCard);
        List<Card> cards = q.setMaxResults(1).getResultList();
        return cards.isEmpty() ? null : cards.get(0);
    }

    public Long countFindCardsByEtatAndUserTypeAndDateEtatLessThan(Card.Etat etat, String userType, LocalDateTime dateEtat) {
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        if (dateEtat == null) throw new IllegalArgumentException("The dateEtat argument is required");
        EntityManager em = entityManager;
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

    public TypedQuery<Card> findCardsByEtatAndUserTypeAndDateEtatLessThan(Card.Etat etat, String userType, LocalDateTime dateEtat) {
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        if (dateEtat == null) throw new IllegalArgumentException("The dateEtat argument is required");
        EntityManager em = entityManager;
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


    public Long countFindCardsByEppnAndEtatEquals(String eppn, Card.Etat etat) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat = :etat", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etat", etat);
        return ((Long) q.getSingleResult());
    }

    public Long countFindCardsByEppnAndEtatNotEquals(String eppn, Card.Etat etat) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn AND o.etat != :etat", Long.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etat", etat);
        return ((Long) q.getSingleResult());
    }

    public Long countFindCardsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE o.eppn = :eppn", Long.class);
        q.setParameter("eppn", eppn);
        return ((Long) q.getSingleResult());
    }

    public Long countFindCardsByEppnLike(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        eppn = eppn.replace('*', '%');
        if (eppn.charAt(0) != '%') {
            eppn = "%" + eppn;
        }
        if (eppn.charAt(eppn.length() - 1) != '%') {
            eppn = eppn + "%";
        }
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Card AS o WHERE LOWER(o.eppn) LIKE LOWER(:eppn)", Long.class);
        q.setParameter("eppn", eppn);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<Card> findCardsByEppnAndEtatEquals(String eppn, Card.Etat etat) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.eppn = :eppn AND o.etat = :etat", Card.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etat", etat);
        return q;
    }

    public TypedQuery<Card> findCardsByEppnAndEtatNotEquals(String eppn, Card.Etat etat) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        if (etat == null) throw new IllegalArgumentException("The etat argument is required");
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.eppn = :eppn AND o.etat != :etat", Card.class);
        q.setParameter("eppn", eppn);
        q.setParameter("etat", etat);
        return q;
    }

    public TypedQuery<Card> findCardsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE o.eppn = :eppn order by requestDate DESC", Card.class);
        q.setParameter("eppn", eppn);
        return q;
    }


    public TypedQuery<Card> findCardsByEppnLike(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        eppn = eppn.replace('*', '%');
        if (eppn.charAt(0) != '%') {
            eppn = "%" + eppn;
        }
        if (eppn.charAt(eppn.length() - 1) != '%') {
            eppn = eppn + "%";
        }
        EntityManager em = entityManager;
        TypedQuery<Card> q = em.createQuery("SELECT o FROM Card AS o WHERE LOWER(o.eppn) LIKE LOWER(:eppn) order by eppn ASC", Card.class);
        q.setParameter("eppn", eppn);
        return q;
    }

    public long countCards() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Card o", Long.class).getSingleResult();
    }

    public List<Card> findAllCards() {
        return entityManager.createQuery("SELECT o FROM Card o", Card.class).getResultList();
    }

    public Card findCard(Long id) {
        if (id == null) return null;
        return entityManager.find(Card.class, id);
    }

    @Transactional
    public Card merge(Card card) {
        Card merged = this.entityManager.merge(card);
        this.entityManager.flush();
        return merged;
    }


    @Transactional
    public void persist(Card card) {
        this.entityManager.persist(card);
        card.getUserAccount().setNbCards(card.getUserAccount().getNbCards()+1);
    }

    @Transactional
    public void remove(Card card) {
        User user = userDaoService.findUser(card.getEppn());
        user.getCards().remove(card);
        EntityManager em = entityManager;
        if (em.contains(card)) {
            em.remove(card);
        } else {
            Card attached = findCard(card.getId());
            em.remove(attached);
        }
    }
}
