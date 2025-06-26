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

    /* Can't use @Async here because it self calls its own methods.
       So we simply manage threads manually.
     */
	// @Async
	@Override
	public void validateInternal(Card card) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.debug("InterruptedException when sleeping before calling validateInternal", delay);
            }
            super.validateInternal(card);
        }).start();
	}

    /* Can't use @Async here because it self calls its own methods.
        So we simply manage threads manually.
    */
    // @Async
    @Override
    public void invalidateInternal(Card card) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.debug("InterruptedException when sleeping before calling invalidateInternal", delay);
            }
            super.invalidateInternal(card);
        }).start();
    }

}


