package org.esupportail.sgc.services.cardid;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;

public class ComueNuBuCardIdService implements CardIdService {

	private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    CardDaoService cardDaoService;
	
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
		Card card = cardDaoService.findCard(cardId);
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
	public String decodeCardId(String desfireIdWithPad) {
		boolean onPadding = true;
		int index = 0;
		String desfireId = "";
		while (index < desfireIdWithPad.length()) {
		    String hexString = desfireIdWithPad.substring(index, Math.min(index + 2, desfireIdWithPad.length()));
		    int charLong = Integer.parseInt(hexString, 16);
		    char ch = (char) charLong;
		    if(!onPadding || "0".charAt(0) != ch) {
		    	desfireId = desfireId + (char)charLong;
		    	onPadding = false;
		    }
		    
		    index += 2;
		}
		log.info("desfireId decoded : " + desfireId);
		return desfireId.toString();
	}

}
