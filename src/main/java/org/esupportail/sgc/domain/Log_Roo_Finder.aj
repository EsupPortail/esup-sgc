// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.domain;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.esupportail.sgc.domain.Log;

privileged aspect Log_Roo_Finder {
    
    public static Long Log.countFindLogsByActionEquals(String action) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.action = :action", Long.class);
        q.setParameter("action", action);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByCardIdEquals(Long cardId) {
        if (cardId == null) throw new IllegalArgumentException("The cardId argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.cardId = :cardId", Long.class);
        q.setParameter("cardId", cardId);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByEppnCibleEquals(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.eppnCible = :eppnCible", Long.class);
        q.setParameter("eppnCible", eppnCible);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByEppnCibleLike(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        eppnCible = eppnCible.replace('*', '%');
        if (eppnCible.charAt(0) != '%') {
            eppnCible = "%" + eppnCible;
        }
        if (eppnCible.charAt(eppnCible.length() - 1) != '%') {
            eppnCible = eppnCible + "%";
        }
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE LOWER(o.eppnCible) LIKE LOWER(:eppnCible)", Long.class);
        q.setParameter("eppnCible", eppnCible);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.eppn = :eppn", Long.class);
        q.setParameter("eppn", eppn);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByEppnLike(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        eppn = eppn.replace('*', '%');
        if (eppn.charAt(0) != '%') {
            eppn = "%" + eppn;
        }
        if (eppn.charAt(eppn.length() - 1) != '%') {
            eppn = eppn + "%";
        }
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE LOWER(o.eppn) LIKE LOWER(:eppn)", Long.class);
        q.setParameter("eppn", eppn);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByLogDateLessThan(Date logDate) {
        if (logDate == null) throw new IllegalArgumentException("The logDate argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.logDate < :logDate", Long.class);
        q.setParameter("logDate", logDate);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByRetCodeEquals(String retCode) {
        if (retCode == null || retCode.length() == 0) throw new IllegalArgumentException("The retCode argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.retCode = :retCode", Long.class);
        q.setParameter("retCode", retCode);
        return ((Long) q.getSingleResult());
    }
    
    public static Long Log.countFindLogsByTypeEquals(String type) {
        if (type == null || type.length() == 0) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM Log AS o WHERE o.type = :type", Long.class);
        q.setParameter("type", type);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<Log> Log.findLogsByActionEquals(String action) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.action = :action", Log.class);
        q.setParameter("action", action);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByActionEquals(String action, String sortFieldName, String sortOrder) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.action = :action");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("action", action);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByCardIdEquals(Long cardId) {
        if (cardId == null) throw new IllegalArgumentException("The cardId argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.cardId = :cardId", Log.class);
        q.setParameter("cardId", cardId);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByCardIdEquals(Long cardId, String sortFieldName, String sortOrder) {
        if (cardId == null) throw new IllegalArgumentException("The cardId argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.cardId = :cardId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("cardId", cardId);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnCibleEquals(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.eppnCible = :eppnCible", Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnCibleEquals(String eppnCible, String sortFieldName, String sortOrder) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.eppnCible = :eppnCible");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnCibleLike(String eppnCible) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        eppnCible = eppnCible.replace('*', '%');
        if (eppnCible.charAt(0) != '%') {
            eppnCible = "%" + eppnCible;
        }
        if (eppnCible.charAt(eppnCible.length() - 1) != '%') {
            eppnCible = eppnCible + "%";
        }
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE LOWER(o.eppnCible) LIKE LOWER(:eppnCible)", Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnCibleLike(String eppnCible, String sortFieldName, String sortOrder) {
        if (eppnCible == null || eppnCible.length() == 0) throw new IllegalArgumentException("The eppnCible argument is required");
        eppnCible = eppnCible.replace('*', '%');
        if (eppnCible.charAt(0) != '%') {
            eppnCible = "%" + eppnCible;
        }
        if (eppnCible.charAt(eppnCible.length() - 1) != '%') {
            eppnCible = eppnCible + "%";
        }
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE LOWER(o.eppnCible) LIKE LOWER(:eppnCible)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppnCible", eppnCible);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnEquals(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.eppn = :eppn", Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnEquals(String eppn, String sortFieldName, String sortOrder) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.eppn = :eppn");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnLike(String eppn) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        eppn = eppn.replace('*', '%');
        if (eppn.charAt(0) != '%') {
            eppn = "%" + eppn;
        }
        if (eppn.charAt(eppn.length() - 1) != '%') {
            eppn = eppn + "%";
        }
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE LOWER(o.eppn) LIKE LOWER(:eppn)", Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByEppnLike(String eppn, String sortFieldName, String sortOrder) {
        if (eppn == null || eppn.length() == 0) throw new IllegalArgumentException("The eppn argument is required");
        eppn = eppn.replace('*', '%');
        if (eppn.charAt(0) != '%') {
            eppn = "%" + eppn;
        }
        if (eppn.charAt(eppn.length() - 1) != '%') {
            eppn = eppn + "%";
        }
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE LOWER(o.eppn) LIKE LOWER(:eppn)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("eppn", eppn);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByLogDateLessThan(Date logDate) {
        if (logDate == null) throw new IllegalArgumentException("The logDate argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.logDate < :logDate", Log.class);
        q.setParameter("logDate", logDate);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByLogDateLessThan(Date logDate, String sortFieldName, String sortOrder) {
        if (logDate == null) throw new IllegalArgumentException("The logDate argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.logDate < :logDate");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("logDate", logDate);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByRetCodeEquals(String retCode) {
        if (retCode == null || retCode.length() == 0) throw new IllegalArgumentException("The retCode argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.retCode = :retCode", Log.class);
        q.setParameter("retCode", retCode);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByRetCodeEquals(String retCode, String sortFieldName, String sortOrder) {
        if (retCode == null || retCode.length() == 0) throw new IllegalArgumentException("The retCode argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.retCode = :retCode");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("retCode", retCode);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByTypeEquals(String type) {
        if (type == null || type.length() == 0) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Log.entityManager();
        TypedQuery<Log> q = em.createQuery("SELECT o FROM Log AS o WHERE o.type = :type", Log.class);
        q.setParameter("type", type);
        return q;
    }
    
    public static TypedQuery<Log> Log.findLogsByTypeEquals(String type, String sortFieldName, String sortOrder) {
        if (type == null || type.length() == 0) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Log.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Log AS o WHERE o.type = :type");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Log> q = em.createQuery(queryBuilder.toString(), Log.class);
        q.setParameter("type", type);
        return q;
    }
    
}
