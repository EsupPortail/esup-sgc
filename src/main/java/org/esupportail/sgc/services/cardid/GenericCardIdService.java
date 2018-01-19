package org.esupportail.sgc.services.cardid;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.esupportail.sgc.domain.Card;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericCardIdService implements CardIdService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	EntityManager entityManager;
	
	private Long idCounterBegin;
	
	private String appName;
	
	private String postgresqlSequence;
	
	public void setIdCounterBegin(Long idCounterBegin) {
		this.idCounterBegin = idCounterBegin;
	}
	
	@Override
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setPostgresqlSequence(String postgresqlSequence) {
		this.postgresqlSequence = postgresqlSequence;
	}

	@Override
	public String generateCardId(Long cardId) {
		Card card = Card.findCard(cardId);
		if(card.getDesfireIds().get(appName) == null || card.getDesfireIds().get(appName).isEmpty()) {
			Session session = (Session) entityManager.getDelegate();
			SQLQuery existSequenceQuery = session.createSQLQuery("SELECT 1 FROM pg_class where relname = '" + postgresqlSequence + "'");
			if(existSequenceQuery.list().isEmpty()) {
				SQLQuery createSequenceQuery = session.createSQLQuery("CREATE SEQUENCE " + postgresqlSequence);
				int createSequenceQueryResult = createSequenceQuery.executeUpdate();
				log.info("create sequence result : " + createSequenceQueryResult);
			}
			SQLQuery nextValQuery = session.createSQLQuery("SELECT nextval('" + postgresqlSequence + "')");
			BigInteger nextVal = (BigInteger)nextValQuery.list().get(0);
			card.getDesfireIds().put(appName, Long.toString(nextVal.longValue() + getIdCounterBegin(card)));
			card.merge();
		}
		return card.getDesfireIds().get(appName);
	}

	protected Long getIdCounterBegin(Card card) {
		return idCounterBegin;
	}

	@Override
	public String encodeCardId(String desfireId) {
		return desfireId;
	}

	@Override
	public String decodeCardId(String desfireId) {
		return desfireId;
	}

}
