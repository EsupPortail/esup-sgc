package org.esupportail.sgc.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.HashSet;
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
	private Map<String, DeferredResult<String>>  suspendedEncodManagersPrintEncodeEppns = new ConcurrentHashMap<String, DeferredResult<String>>();

	// Hack pour gestion des eppn requêtant un nouveau heartbeat alors que le hertbeat courant n'a pas été finalisé :
	// Ce Set permet de les préserver en mémoire durant la méthode encodePrintHeartbeat
	// et donc de leur laisser l'accès au bouton encodage+impression
	private Set<String> currentEncodManagersPrintEncodeEppns =  Collections.synchronizedSet(new HashSet());

	public DeferredResult<String> qrcode2edit(String eppn) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		final DeferredResult<String> qrcode = new DeferredResult<String>(null, "");
		
		if(this.suspendedEncodPrintCardPollRequests.containsKey(eppn)) {
			this.suspendedEncodPrintCardPollRequests.get(eppn).setResult("stop");
		}
		this.suspendedEncodPrintCardPollRequests.put(eppn, qrcode);

		qrcode.onCompletion(new Runnable() {
			public void run() {
				if(qrcode.equals(suspendedEncodPrintCardPollRequests.get(eppn))) {
					suspendedEncodPrintCardPollRequests.remove(eppn);
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
		Set<String> managersPrintEncodeEppns = new HashSet<>(currentEncodManagersPrintEncodeEppns);
		managersPrintEncodeEppns.addAll(suspendedEncodManagersPrintEncodeEppns.keySet());
		return managersPrintEncodeEppns;
	}

	public DeferredResult<String> encodePrintHeartbeat(String eppnInit) {

		currentEncodManagersPrintEncodeEppns.add(eppnInit);

		final DeferredResult<String> okResult = new DeferredResult<String>(null, "ok");

		if(this.suspendedEncodManagersPrintEncodeEppns.containsKey(eppnInit)) {
			this.suspendedEncodManagersPrintEncodeEppns.get(eppnInit).setResult("ok");
		}

		try {
			// hack ... sleep 2 sec so that onCompletion with suspendedEncodManagersPrintEncodeEppns.remove is called before adding same eppn ...
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//
		}
		log.info("Heartbeat for {} started ; add possibility to encode+print", eppnInit);
		this.suspendedEncodManagersPrintEncodeEppns.put(eppnInit, okResult);

		okResult.onCompletion(new Runnable() {
			public void run() {
				log.info("Heartbeat for {} stopped ; remove possibility to encode+print", eppnInit);
				suspendedEncodManagersPrintEncodeEppns.remove(eppnInit);
			}
		});

		log.info("this.suspendedEncodManagersPrintEncodeEppns.size : " + this.suspendedEncodManagersPrintEncodeEppns.size());

		currentEncodManagersPrintEncodeEppns.remove(eppnInit);

		return okResult;
	}
}
