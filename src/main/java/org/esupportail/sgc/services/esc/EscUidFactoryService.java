package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeanstudentcard.esc.EscnFactory;
import eu.europeanstudentcard.esc.EscnFactoryException;


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
			card.setEscnUid(EscnFactory.getEscn(prefixe, pic));
		} catch (InterruptedException | EscnFactoryException e) {
			throw new SgcRuntimeException("Error generating ESNC UID", e); 
		}
	}

	public String getQrCodeUrl(Card card) {
		return getQrCodeUrl(card.getEscnUid());
	}
	
	public String getQrCodeUrl(String escn) {
		return qrCodeUrlPrefixe + escn;
	}
	
}
