package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cnous.esc.UuidFactory;
import com.cnous.esc.UuidFactoryException;

public class EscUidFactoryService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String pic;
	
	private Integer prefixe;
	
	private String qrCodeUrlPrefixe;
	
	public void setPic(String pic) {
		this.pic = pic;
	}

	public void setPrefixe(Integer prefixe) {
		this.prefixe = prefixe;
	}

	public void setQrCodeUrlPrefixe(String qrCodeUrlPrefixe) {
		this.qrCodeUrlPrefixe = qrCodeUrlPrefixe;
	}

	public synchronized void generateEscnUid(Card card) {
		try {
			card.setEscnUid(UuidFactory.getUuid(prefixe, pic));
		} catch (InterruptedException | UuidFactoryException e) {
			throw new SgcRuntimeException("Error generating ESNC UID", e); 
		}
	}

	public String getQrCodeUrl(Card card) {
		return qrCodeUrlPrefixe + card.getEscnUid();
	}
	
}
