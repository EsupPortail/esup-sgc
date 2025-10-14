package org.esupportail.sgc.services.cardid;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericCardIdService implements CardIdService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	EntityManager entityManager;

    @Resource
    CardDaoService cardDaoService;
	
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
		Card card = cardDaoService.findCard(cardId);
		if(card.getDesfireIds().get(appName) == null || card.getDesfireIds().get(appName).isEmpty()) {
			Session session = (Session) entityManager.getDelegate();
            NativeQuery existSequenceQuery = session.createNativeQuery("SELECT 1 FROM pg_class where relname = '" + postgresqlSequence + "'");
			if(existSequenceQuery.list().isEmpty()) {
                NativeQuery createSequenceQuery = session.createNativeQuery("CREATE SEQUENCE " + postgresqlSequence);
				int createSequenceQueryResult = createSequenceQuery.executeUpdate();
				log.info("create sequence result : " + createSequenceQueryResult);
			}
            NativeQuery nextValQuery = session.createNativeQuery("SELECT nextval('" + postgresqlSequence + "')");
			Long nextVal = (Long)nextValQuery.list().get(0);
			String desfireId = Long.toString(nextVal.longValue() + getIdCounterBegin(card));
			card.getDesfireIds().put(appName, desfireId);
            cardDaoService.merge(card);
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
		log.info("desfireIdWithPad : " + desfireIdWithPad + " -> size : " + desfireIdWithPad.length());
		return desfireIdWithPad;
	}

	@Override
	public String decodeCardId(String desfireIdWithPad) {
		Long desfireId = Long.valueOf(desfireIdWithPad);
		return desfireId.toString();
	}

}


