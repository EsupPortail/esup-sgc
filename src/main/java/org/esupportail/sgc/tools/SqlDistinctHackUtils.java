package org.esupportail.sgc.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDistinctHackUtils {

    private final static Logger log = LoggerFactory.getLogger(SqlDistinctHackUtils.class);

    /* Hack - optimisation distinct - cf https://wiki.postgresql.org/wiki/Loose_indexscan */
    public static String selectDistinctWithLooseIndex(String tbl, String col) {
        // String sql = String.format("SELECT DISTINCT col FROM tbl where col is not null");
        String sql = String.format("WITH RECURSIVE t AS ( " +
                "(SELECT col FROM tbl ORDER BY col LIMIT 1) " +
                "UNION ALL " +
                "SELECT (SELECT col FROM tbl WHERE col > t.col ORDER BY col LIMIT 1) " +
                "FROM t WHERE t.col IS NOT NULL" +
                ") SELECT col FROM t WHERE col IS NOT NULL;");
        sql = sql.replaceAll("tbl", tbl).replaceAll("col", col);
        log.trace("distinct sql request opimized : {}", sql);
        return sql;
    }

}
