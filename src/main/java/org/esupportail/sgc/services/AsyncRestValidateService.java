package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public class AsyncRestValidateService extends RestValidateService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private long delay = 0; 
	
	
	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Async
	@Override
	public void validateInternal(Card card) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			log.debug("InterruptedException when sleeping before calling validateInternal", delay);
		}
		super.validateInternal(card);
	}

	@Async
	@Override
	public void invalidateInternal(Card card) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			log.debug("InterruptedException when sleeping before calling invalidateInternal", delay);
		}
		super.invalidateInternal(card);
	}

}


