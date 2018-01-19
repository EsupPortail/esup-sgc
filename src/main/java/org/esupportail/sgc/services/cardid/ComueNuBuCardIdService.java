package org.esupportail.sgc.services.cardid;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;

public class ComueNuBuCardIdService implements CardIdService {

	private String appName;
	
	public void setAppName(String appName) {
		this.appName = appName;
	}

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public String generateCardId(Long cardId) {
		Card card = Card.findCard(cardId);
		User user = card.getUser();
		String leocode = user.getSecondaryId();
		return leocode;
	}

	@Override
	public String encodeCardId(String leocode) {
		String fullLeocode = StringUtils.leftPad(leocode, 13, "0");
		String idComueBu = fullLeocode.substring(5) + fullLeocode;
		String idComueBuFormatted = "";
		for (char ch : idComueBu.toCharArray()) {
			idComueBuFormatted = idComueBuFormatted + Integer.toHexString(ch);
		}
		return idComueBuFormatted;
	}

	@Override
	public String decodeCardId(String desfireId) {
		return desfireId;
	}

}
