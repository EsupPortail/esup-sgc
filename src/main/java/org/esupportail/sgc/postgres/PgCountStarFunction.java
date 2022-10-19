package org.esupportail.sgc.postgres;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import java.util.List;

/*
    With PostgreSQL, count(*) is faster than count(id)
    This class PgCountStarFunction let to force count(*) on JPA Query
 */
public class PgCountStarFunction implements SQLFunction {

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException{
        return new LongType();
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String render(Type type, List args, SessionFactoryImplementor factory) throws QueryException {
        return "count(*)";
    }
    
}
