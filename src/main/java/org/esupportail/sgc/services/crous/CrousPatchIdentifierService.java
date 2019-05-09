package org.esupportail.sgc.services.crous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrousPatchIdentifierService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private Boolean inWorking = false; 
	
	@Resource
	CrousPatchIdentifierEntryService crousPatchIdentifierEntryService;
	
	@Resource
	CrousService crousService;
	
	public Boolean isInWorking() {
		return inWorking;
	}

	
	/*
	 * 
	oldId;eppnNewId;mail
	 */
	@Async
	public synchronized void consumeCsv(InputStream stream) throws IOException {
		inWorking = true;
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String line;
		int i = 0;
		in.readLine(); // ignore header line;
		while ((line = in.readLine()) != null) {
			try {
				crousPatchIdentifierEntryService.consumeCsvLine(line);
				i++;
			} catch(Exception e) {
				log.error("Error integrating this csv line : " + line, e);
			}
		}
		log.info(i + " CrousPatchIdentifier imported" );
		inWorking = false;
	}

	@Async
	public synchronized void patchIdentifiers() {
		inWorking = true;
		for(CrousPatchIdentifier crousPatchIdentifier : CrousPatchIdentifier.findCrousPatchIdentifiersByPatchSuccessNotEquals(true).getResultList()) {
			patchIdentifier(crousPatchIdentifier);
		}	
		inWorking = false;
	}

	public void patchIdentifier(CrousPatchIdentifier crousPatchIdentifier) {
		try {
			log.info("appel du crous patchIdentifier : " + crousPatchIdentifier);
			PatchIdentifier patchIdentifier = new PatchIdentifier();
			patchIdentifier.setCurrentIdentifier(crousPatchIdentifier.getOldId());
			patchIdentifier.setNewIdentifier(crousPatchIdentifier.getEppnNewId());
			patchIdentifier.setEmail(crousPatchIdentifier.getMail());
			crousService.patchIdentifier(patchIdentifier);
			crousPatchIdentifier.setPatchSuccess(true);
			crousPatchIdentifier.merge();
			log.info("crous patchIdentifier " + crousPatchIdentifier + " ok");
		} catch(Exception e) {
			crousPatchIdentifier.setPatchSuccess(false);
			crousPatchIdentifier.merge();
			log.warn("Error patchIdentifier : " + crousPatchIdentifier + " - " + e.getMessage());
		}
	}


    @Transactional
	public synchronized void deletePatchIdentifiants() {
		inWorking = true;
		CrousPatchIdentifier.removeAll();
		inWorking = false;
	}


	@Async
	public synchronized void generatePatchIdentifiersIne() {
		inWorking = true;
		for(User user : User.findUsers4PatchIdentifiersIne()) {
			try {
				CrousPatchIdentifier patchIdentifier = new CrousPatchIdentifier();
				patchIdentifier.setOldId(user.getCrousIdentifier());
				patchIdentifier.setEppnNewId(user.getSupannCodeINE());
				patchIdentifier.setMail(user.getEmail());
				patchIdentifier.persist();
			} catch(Exception e) {
				log.error("Error trying generating Patch Identifier with Ine for " + user.getEppn() + " : " + e.getMessage(), e);
			}
		}
		inWorking = false;
	}
}

