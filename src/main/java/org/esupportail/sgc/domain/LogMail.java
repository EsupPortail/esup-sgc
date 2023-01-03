package org.esupportail.sgc.domain;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = {"findLogMailsByLogDateLessThan"})
public class LogMail {

    @ManyToOne
    private CardActionMessage cardActionMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    private Date logDate;

    private String eppn;

    private String subject;

    private String mailTo;

    @Column(columnDefinition = "TEXT")
    private String message;

    public static Long countFindLogMails(CardActionMessage cardActionMessage, String eppn, Date date1, Date date2) {
        EntityManager em = LogMail.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM LogMail AS o WHERE o.cardActionMessage=:cardActionMessage AND o.eppn=:eppn AND o.logDate > :date1 AND o.logDate < :date2", Long.class);
        q.setParameter("cardActionMessage", cardActionMessage);
        q.setParameter("eppn", eppn);
        q.setParameter("date1", date1);
        q.setParameter("date2", date2);
        return ((Long) q.getSingleResult());
    }

}
