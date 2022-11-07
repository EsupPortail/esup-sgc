package org.esupportail.sgc.services.papercut;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class PapercutService extends ValidateService implements InitializingBean {

	private final Logger log = LoggerFactory.getLogger(getClass());

	String authToken;
	
	String server;
	
	String scheme = "http";
	
	int port;
	
	String accountName = "";
	
	String cardNumberAttribute = "card-number";
	
	String papercutUidFromEppnRegex = "(.*)";
	
	boolean useReverseCsn = false;

    ServerCommandProxy serverProxy;    

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public void setCardNumberAttribute(String cardNumberAttribute) {
		this.cardNumberAttribute = cardNumberAttribute;
	}

	public void setPapercutUidFromEppnRegex(String papercutUidFromEppnRegex) {
		this.papercutUidFromEppnRegex = papercutUidFromEppnRegex;
	}

	public void setUseReverseCsn(boolean useReverseCsn) {
		this.useReverseCsn = useReverseCsn;
	}

	public void afterPropertiesSet() {
		serverProxy = new ServerCommandProxy(server, scheme, port, authToken);
	}

	@Override
	public void validateInternal(Card card) {
		String uid = getPapercutUid4User(card.getUser());
		if(serverProxy.isUserExists(uid)) {
			String cardNumber = (useReverseCsn ? card.getReverseCsn() : card.getCsn());
			serverProxy.setUserProperty(uid, cardNumberAttribute, cardNumber);
			log.debug("Carte papercut de " + uid + " -> " + cardNumber);
		} else {
			log.debug("Validation papercut : utilisateur " + uid + " non trouvé dans papercut");
		}
	}

	@Override
	public void invalidateInternal(Card card) {
		String uid = getPapercutUid4User(card.getUser());
		if(serverProxy.isUserExists(uid)) {		
			serverProxy.setUserProperty(uid, cardNumberAttribute,"");
			log.debug("Carte papercut de " + uid + " -> ''");
		} else {
			log.debug("Invalidation papercut : utilisateur " + uid + " non trouvé dans papercut");
		}
	}

	protected String getPapercutUid4User(User user) {
		String uid = user.getEppn().replaceAll(papercutUidFromEppnRegex, "$1");
		return uid;
	}
    
}


