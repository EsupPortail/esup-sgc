package org.esupportail.sgc.dao;

import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.repositories.TemplateCardRepository;
import org.esupportail.sgc.tools.SqlDistinctHackUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class TemplateCardDaoService {

    @PersistenceContext
    transient EntityManager entityManager;

    @Resource
    TemplateCardRepository templateCardRepository;

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("key", "name", "numVersion", "cssStyle", "cssMobileStyle", "description", "modificateur", "codeBarres", "dateModification", "photoFileLogo", "photoFileMasque", "photoFileQrCode", "masque", "logo", "qrCode");
    
    public List<Object[]> countTemplateCardByNameVersion() {
        String sql = "SELECT CONCAT(name, ' / V', num_version) as nom, count(*) FROM card,template_card WHERE card.template_card= template_card.id AND etat='ENABLED' GROUP BY nom";

        EntityManager em = entityManager;
        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

    public Long countFindTemplateCardsByKeyEquals(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM TemplateCard AS o WHERE o.key = :key", Long.class);
        q.setParameter("key", key);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<TemplateCard> findTemplateCardsByKeyEquals(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        TypedQuery<TemplateCard> q = em.createQuery("SELECT o FROM TemplateCard AS o WHERE o.key = :key", TemplateCard.class);
        q.setParameter("key", key);
        return q;
    }

    public TypedQuery<TemplateCard> findTemplateCardsByKeyEquals(String key, String sortFieldName, String sortOrder) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException("The key argument is required");
        EntityManager em = entityManager;
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM TemplateCard AS o WHERE o.key = :key");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<TemplateCard> q = em.createQuery(queryBuilder.toString(), TemplateCard.class);
        q.setParameter("key", key);
        return q;
    }

    public List<TemplateCard> findAllTemplateCards() {
        return entityManager.createQuery("SELECT o FROM TemplateCard o", TemplateCard.class).getResultList();
    }

    public TemplateCard findTemplateCard(Long id) {
        if (id == null) return null;
        return entityManager.find(TemplateCard.class, id);
    }

    public Page<TemplateCard> findTemplateCardEntries(Pageable pageable) {
        return templateCardRepository.findAll(pageable);
    }

    public TemplateCard getTemplateCard(User user) {
        if(user!=null &&  user.getTemplateKey()!=null && findTemplateCardsByKeyEquals(user.getTemplateKey()).getResultList().size()>0){
            return findTemplateCardsByKeyEquals(user.getTemplateKey(), "numVersion", "DESC").getResultList().get(0);
        } else {
            return findTemplateCardsByKeyEquals("default").getResultList().get(0);
        }
    }

    public List<TemplateCard> findDistinctLastTemplateCardsPrinted() {
        List<TemplateCard> templateCards = new ArrayList<TemplateCard>();
        EntityManager em = entityManager;
        Query q = em.createNativeQuery(SqlDistinctHackUtils.selectDistinctWithLooseIndex("user_account" , "last_card_template_printed"));
        List<Long> last_card_template_ids = q.getResultList();
        for(Long id : last_card_template_ids) {
            if(id != null) {
                templateCards.add(findTemplateCard(id));
            }
        }
        return templateCards;
    }

    @Transactional
    public void persist(TemplateCard templateCard) {
        this.entityManager.persist(templateCard);
    }

    @Transactional
    public void remove(TemplateCard templateCard) {
        if (this.entityManager.contains(templateCard)) {
            this.entityManager.remove(templateCard);
        } else {
            TemplateCard attached = findTemplateCard(templateCard.getId());
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public TemplateCard merge(TemplateCard templateCard) {
        TemplateCard merged = this.entityManager.merge(templateCard);
        this.entityManager.flush();
        return merged;
    }

}
