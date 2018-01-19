package org.esupportail.sgc.services.cardid;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific COMUE Normandie Université AccessControl Identifiant
 */
public class ComueNuAccessControlCardIdService extends GenericCardIdService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected Long getIdCounterBegin(Card card) {
		long userTypeCardNfcIdCounterBegin = super.getIdCounterBegin(card);
		User user = card.getUser();
		if("E".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("100000000000");
		} else if("P".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("200000000000");
		} else if("I".equals(user.getUserType())) {
			userTypeCardNfcIdCounterBegin += Long.valueOf("900000000000");
		} else {
			userTypeCardNfcIdCounterBegin += Long.valueOf("300000000000");
		}	
		return userTypeCardNfcIdCounterBegin;
	}

	@Override
	public String encodeCardId(String desfireId) {

		/*  on formatte l'ID façon COMUE Normandie Université
		 * cf http://wiki.univ-rouen.fr/dsi/systeme/services/controle_acces?s[]=leocarte
		 * commence par un octet représentant la version du mapping du fichier (valeur '30', correspondant au code ASCII de '0' pour commencer)
		 * 15 car encodés en ASCII ⇒ 15 octets
		 * 15 car encodés en Hexa ⇒ 7 octets
		 * 15 car encodés en BCD ⇒ 8 octets
		 */

		String cardNfcIdFormatted = "30";

		for (char ch : desfireId.toCharArray()) {
			cardNfcIdFormatted = cardNfcIdFormatted + Integer.toHexString(ch);
		}
		String desfireHex = Long.toHexString(Long.valueOf(desfireId));
		for(int i = 0 ; 14-desfireHex.length()>0 ; i++) {
			desfireHex = "0" + desfireHex;
		}
		cardNfcIdFormatted = cardNfcIdFormatted + desfireHex;
		for(int i = 0 ; 16-desfireId.length()>0 && i<16-desfireId.length() ; i++) {
			desfireId = "0" + desfireId;
		}
		cardNfcIdFormatted = cardNfcIdFormatted + desfireId;

		return cardNfcIdFormatted;
	}

	@Override
	public String decodeCardId(String desfireId) {
		return desfireId.substring(47, 62);
	}

}
