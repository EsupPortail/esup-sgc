package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import eu.europeanstudentcard.esc.EscnFactory;
import eu.europeanstudentcard.esc.EscnFactoryException;


public class EscUidFactoryService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String defaultPic;
	
	private Integer prefixe;
	
	private String qrCodeUrlPrefixe;
	
	public void setPic(String pic) {
		this.defaultPic = pic;
	}

	public void setPrefixe(Integer prefixe) {
		this.prefixe = prefixe;
	}

	public void setQrCodeUrlPrefixe(String qrCodeUrlPrefixe) {
		this.qrCodeUrlPrefixe = qrCodeUrlPrefixe;
	}

	public synchronized void generateEscnUid(Card card) {
		try {
			User user = card.getUser();
			if(user==null) {
				// renouvellement ...
				user = User.findUser(card.getEppn());
			}
			String pic = this.defaultPic;
			if(!StringUtils.isEmpty(user.getPic())) {
				pic = user.getPic();
			}
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
