package org.esupportail.sgc.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;


@Service
public class EncodeAndPringLongPollService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// Map avec en clef l'eppn de l'utilisateur manager potentiel éditeur de carte via imprimante+encodeur (evolis)
	private Map<String, DeferredResult<String>> suspendedEncodPrintCardPollRequests = new ConcurrentHashMap<String, DeferredResult<String>>();

	// List des eppn de l'utilisateur manager potentiel éditeur de carte via imprimante+encodeur (heartbeat)
	private Set<String> managersPrintEncodeEppns = new ConcurrentSkipListSet<>();

	public DeferredResult<String> qrcode2edit(String eppn) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		final DeferredResult<String> qrcode = new DeferredResult<String>(null, "");
		
		if(this.suspendedEncodPrintCardPollRequests.containsKey(eppn)) {
			this.suspendedEncodPrintCardPollRequests.get(eppn).setResult("stop");
		}
		this.suspendedEncodPrintCardPollRequests.put(eppn, qrcode);

		qrcode.onCompletion(new Runnable() {
			public void run() {
				synchronized (qrcode) {
					if(qrcode.equals(suspendedEncodPrintCardPollRequests.get(eppn))) {
						suspendedEncodPrintCardPollRequests.remove(eppn);
					}
				}
			}
		});
		
		log.info("this.suspendedEncodPrintCardPollRequests.size : " + this.suspendedEncodPrintCardPollRequests.size());

		return qrcode;
	}

	@Async
	public void handleCard(String printerEppn, String qrcode) {
		log.debug("handleCard : " + qrcode + " for " + printerEppn);
		try {
			// methode async, on attend 1 seconde pour que l'état de la carte soit bien en TO_ENCODE_PRINT
			// lorsque esup-sgc-client tentera d'éditer la carte
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.debug("Exception when sleeping ...");
		}
		if(this.suspendedEncodPrintCardPollRequests.containsKey(printerEppn)) {
			this.suspendedEncodPrintCardPollRequests.get(printerEppn).setResult(qrcode);
		}
	}

	public Set<String> getManagersPrintEncodeEppns() {
		return managersPrintEncodeEppns;
	}

	public DeferredResult<String> encodePrintHeartbeat(String eppnInit) {
		final DeferredResult<String> okResult = new DeferredResult<String>(null, "ok");
		this.managersPrintEncodeEppns.add(eppnInit);
		okResult.onCompletion(new Runnable() {
			public void run() {
				synchronized (eppnInit) {
					managersPrintEncodeEppns.remove(eppnInit);
				}
			}
		});
		return okResult;
	}
}
