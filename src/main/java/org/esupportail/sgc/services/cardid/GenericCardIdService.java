package org.esupportail.sgc.services.cardid;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
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
	
	private int desfireFileLength = 64;
	
	public void setIdCounterBegin(Long idCounterBegin) {
		this.idCounterBegin = idCounterBegin;
	}

	public void setDesfireFileLength(String desfireFileLengthString) {
		this.desfireFileLength = Integer.valueOf(desfireFileLengthString);
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
			String desfireId = Long.toString(nextVal.longValue() + getIdCounterBegin(card));
			card.getDesfireIds().put(appName, desfireId);
			card.merge();
			log.info("generate card Id for " + card.getEppn() + " : " + appName + " -> "  + desfireId);
		}
		return card.getDesfireIds().get(appName);
	}

	protected Long getIdCounterBegin(Card card) {
		return idCounterBegin;
	}

	@Override
	public String encodeCardId(String desfireId) {
		String desfireIdWithPad = StringUtils.leftPad(desfireId, desfireFileLength, "0");
		return desfireIdWithPad;
	}

	@Override
	public String decodeCardId(String desfireIdWithPad) {
		Long desfireId = Long.valueOf(desfireIdWithPad);
		return desfireId.toString();
	}

}


