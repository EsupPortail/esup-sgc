package org.esupportail.sgc.postgres;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.DoubleType;
import org.hibernate.type.ObjectType;

public class PgFullTextDialect extends PostgreSQL9Dialect{

    public PgFullTextDialect() {
        registerFunction("fts", new PgFullTextFunction());
        registerFunction("ts_rank", new PgFullTextRankFunction());
        registerFunction("count_star", new PgCountStarFunction());
    }
    
}
