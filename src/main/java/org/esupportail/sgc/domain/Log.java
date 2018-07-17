package org.esupportail.sgc.domain;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findLogsByLoginLike", "findLogsByCibleLoginLike", "findLogsByTypeEquals", "findLogsByRetCodeEquals", "findLogsByActionEquals", "findLogsByLogDateLessThan", "findLogsByCibleLoginEqualsAndActionEquals", "findLogsByCardIdEquals", "findLogsByEppnEquals", "findLogsByEppnCibleEquals", "findLogsByEppnLike", "findLogsByEppnCibleLike" })
public class Log {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm")
    private Date logDate;

    private String eppn;

    private String eppnCible;

    private String type;

    private Long cardId;

    private String action;

    private String retCode;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String remoteAddress;


    public static List<Object> countUserDeliveries() {
        EntityManager em = Log.entityManager();
        String sql = "SELECT to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) FROM log WHERE action = 'USER_DELIVERY' GROUP BY day ORDER BY day";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
}
