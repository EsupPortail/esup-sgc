package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.services.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AccessControlService extends ValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static String AC_APP_NAME = "access-control";
	
	@Autowired
	List<Export2AccessControlService> acServices;
	
	public void sync(List<String> eppns) {
		for(Export2AccessControlService acService: acServices) {
			List<String> matchEppns = new ArrayList<String>();
			for(String eppn: eppns) {
				if(eppn.matches(acService.getEppnFilter())) {
					matchEppns.add(eppn);
				}
			}
			try {
				acService.sync(matchEppns);
			} catch (IOException e) {
				log.error("IOException during synchronization of " + matchEppns + " on " + acService, e);
			}
		}
	}

	public void sync(String eppn) {
		log.debug("accessControlService.sync called for " + eppn);
		for(Export2AccessControlService acService: acServices) {
			if(eppn.matches(acService.getEppnFilter())) {
				try {
					acService.sync(eppn);
				} catch (IOException e) {
					log.error("IOException during synchronization of " + eppn + " on " + acService, e);
				}
			}
		}
	}

	@Override
	public void validateInternal(Card card) {
		sync(card.getEppn());
	}

	@Override
	public void invalidateInternal(Card card) {
		sync(card.getEppn());
	}
	
}
