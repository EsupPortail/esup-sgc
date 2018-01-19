package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.util.List;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class AccessControlService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static String AC_APP_NAME = "access-control";
	
	@Autowired
	List<Export2AccessControlService> acServices;
	
	public void sync(List<String> eppns) {
		for(Export2AccessControlService acService: acServices) {
			try {
				acService.sync(eppns);
			} catch (IOException e) {
				log.error("IOException during synchronization of " + eppns + " on " + acService, e);
			}
		}
	}

	public void sync(String eppn) {
		for(Export2AccessControlService acService: acServices) {
			try {
				acService.sync(eppn);
			} catch (IOException e) {
				log.error("IOException during synchronization of " + eppn + " on " + acService, e);
			}
		}
	}

	@Override
	public void validate(Card card) {
		sync(card.getEppn());
	}

	@Override
	public void invalidate(Card card) {
		sync(card.getEppn());
	}
	
}
