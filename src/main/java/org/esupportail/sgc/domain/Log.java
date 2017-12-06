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
@RooJpaActiveRecord(finders = { "findLogsByLoginLike", "findLogsByCibleLoginLike", "findLogsByTypeEquals", "findLogsByRetCodeEquals", "findLogsByActionEquals", "findLogsByLogDateLessThan", "findLogsByCibleLoginEqualsAndActionEquals", "findLogsByCardIdEquals", "findLogsByEppnEquals", "findLogsByEppnCibleEquals" })
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

    @Column(columnDefinition="TEXT")
    private String comment;

    private String remoteAddress;
    
    public static List<Object> countNbLogByDay(String action, String cases, List<String> banned) {
		String bannedReq = "";
		if (!banned.isEmpty()) {
			bannedReq = " AND remote_address NOT IN(:banned) ";
		}

		String sql = "SELECT to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, remote_address, count(*) FROM log WHERE action = '"
				+ action + "' " + bannedReq
				+ " AND ret_code = 'SUCCESS' AND DATE_PART('days', now() - log_date) < 31  GROUP BY day, remote_address ORDER BY day";
		if (cases != null) {
			sql = "SELECT to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, " + cases
					+ ", count(*) FROM log WHERE action = '" + action + "' " + bannedReq
					+ " AND ret_code = 'SUCCESS' AND DATE_PART('days', now() - log_date) < 31  GROUP BY day, remote_address ORDER BY day";
		}

		EntityManager em = Card.entityManager();
		Query q = em.createNativeQuery(sql);
		if (!banned.isEmpty()) {
			q.setParameter("banned", banned);
		}
		return q.getResultList();
    }
    
    public static List<Object> countNbLogByAction(String action, String cases, List<String> banned) {
		String bannedReq = "";
		if (!banned.isEmpty()) {
			bannedReq = " AND remote_address NOT IN(:banned) ";
		}

		String sql = "SELECT remote_address, count(*) FROM log WHERE action = '" + action + "' " + bannedReq
				+ " AND ret_code = 'SUCCESS' GROUP BY remote_address ORDER BY count";
		if (cases != null) {
			sql = "SELECT " + cases + ", count(*) FROM log WHERE action = '" + action + "' " + bannedReq
					+ " AND ret_code = 'SUCCESS' GROUP BY remote_address ORDER BY count";
		}

		EntityManager em = Card.entityManager();
		Query q = em.createNativeQuery(sql);
		if (!banned.isEmpty()) {
			q.setParameter("banned", banned);
		}
		return q.getResultList();
    }
    
    public static List<Object> countNbLogByDay2(String action, String cases, List<String> banned) {
		String bannedReq = "";
		if (!banned.isEmpty()) {
			bannedReq = " AND remote_address NOT IN(:banned) ";
		}

		String sql = "SELECT remote_address,  to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) FROM log WHERE action = '"
				+ action + "' " + bannedReq
				+ "AND ret_code = 'SUCCESS' AND DATE_PART('days', now() - log_date) < 31  GROUP BY remote_address, day  order by remote_address";
		if (cases != null) {
			sql = "SELECT " + cases
					+ ",  to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) FROM log WHERE action = '"
					+ action + "' " + bannedReq
					+ " AND ret_code = 'SUCCESS' AND DATE_PART('days', now() - log_date) < 31  GROUP BY remote_address, day  order by remote_address";
		}

		EntityManager em = Card.entityManager();
		Query q = em.createNativeQuery(sql);
		if (!banned.isEmpty()) {
			q.setParameter("banned", banned);
		}
		return q.getResultList();
   }
    
    public static  List<Object> countUserDeliveries() {
        EntityManager em = Log.entityManager();
        String sql = "SELECT to_date(to_char(log_date, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS day, count(*) FROM log WHERE action = 'USER_DELIVERY' GROUP BY day ORDER BY day";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
}
