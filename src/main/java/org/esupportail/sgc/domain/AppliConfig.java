package org.esupportail.sgc.domain;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "AppliConfig", finders = { "findAppliConfigsByKeyEquals", "findAppliConfigsByValueLike", "findAppliConfigsByKeyLike", "findAppliConfigsByType" })
@Table(name = "AppliConfig", indexes = {
        @Index(name = "appli_config_key_id", columnList = "key"),
})
public class AppliConfig {

    @Column(name = "key", length = 120, unique = true)
    private String key;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    public static AppliConfig findAppliConfigByKey(String key) {
        List<AppliConfig> configs = AppliConfig.findAppliConfigsByKeyEquals(key).getResultList();
        if (configs.isEmpty()) {
            throw new SgcRuntimeException("Configuration with key " + key + " not found - please create it", null);
        } else {
            return configs.get(0);
        }
    }

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public static enum TypeConfig {

        HTML, TEXT, BOOLEAN
    }

    ;

    @Column
    @Enumerated(EnumType.STRING)
    private TypeConfig type = TypeConfig.HTML;
    
    public static TypedQuery<AppliConfig> findAppliConfigsByType(TypeConfig type, String sortFieldName, String sortOrder) {
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = AppliConfig.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM AppliConfig AS o WHERE o.type = :type");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<AppliConfig> q = em.createQuery(queryBuilder.toString(), AppliConfig.class);
        q.setParameter("type", type);
        return q;
    }
    
    public static Long countFindAppliConfigsByType(TypeConfig type) {
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = AppliConfig.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM AppliConfig AS o WHERE o.type = :type", Long.class);
        q.setParameter("type", type);
        return ((Long) q.getSingleResult());
    }
}
