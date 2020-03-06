package org.esupportail.sgc.domain;
import java.util.Date;
import java.util.List;

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
@RooJpaActiveRecord(finders = { "findPayboxTransactionLogsByIdtransEquals", "findPayboxTransactionLogsByReferenceEquals", "findPayboxTransactionLogsByEppnEquals" })
public class PayboxTransactionLog {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date transactionDate;

    private String eppn;

    private String reference;

    private String montant;

    private String auto;

    private String erreur;

    private String idtrans;

    private String signature;

    public String getMontantDevise() {
        Double mnt = new Double(montant) / 100.0;
        return mnt.toString();
    }
    
    public static List<Object> countNbPayboxByYearEtat() {
        EntityManager em = PayboxTransactionLog.entityManager();
        String sql = "SELECT CASE WHEN(DATE_PART('month', transaction_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', transaction_date)-1 AS TEXT),'-',CAST(DATE_PART('year', transaction_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', transaction_date) AS TEXT),'-',CAST(DATE_PART('year', transaction_date)+1 AS TEXT)) END AS Saison, CASE WHEN montant = '1' THEN '' ELSE 'FINALISE' END AS etat, count(*) FROM paybox_transaction_log "
        		+ "WHERE reference IN (SELECT pay_cmd_num FROM card WHERE pay_cmd_num IS NOT NULL) GROUP BY Saison, etat UNION "
        		+ "SELECT CASE WHEN(DATE_PART('month', transaction_date)<7) "
        		+ "THEN CONCAT(CAST(DATE_PART('year', transaction_date)-1 AS TEXT),'-',CAST(DATE_PART('year', transaction_date) AS TEXT)) "
        		+ "ELSE CONCAT(CAST(DATE_PART('year', transaction_date) AS TEXT),'-',CAST(DATE_PART('year', transaction_date)+1 AS TEXT)) END AS Saison, CASE WHEN montant = '1' THEN '' ELSE 'PAYE' END AS etat, count(*) FROM paybox_transaction_log "
        		+ "WHERE reference NOT IN (SELECT pay_cmd_num FROM card WHERE pay_cmd_num IS NOT NULL) GROUP BY Saison, etat ORDER BY Saison ASC";

        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }
}
