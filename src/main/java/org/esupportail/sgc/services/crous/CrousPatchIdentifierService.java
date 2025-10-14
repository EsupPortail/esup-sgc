package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.dao.CrousPatchIdentifierDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class CrousPatchIdentifierService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private Boolean inWorking = false; 
	
	@Resource
	CrousPatchIdentifierEntryService crousPatchIdentifierEntryService;
	
	@Resource
	CrousService crousService;

    @Resource
    CrousPatchIdentifierDaoService crousPatchIdentifierDaoService;

    @Resource
    UserDaoService userDaoService;
	
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
		for(CrousPatchIdentifier crousPatchIdentifier : crousPatchIdentifierDaoService.findCrousPatchIdentifiersByPatchSuccessNotEquals(true).getResultList()) {
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
			crousService.patchIdentifier(patchIdentifier, EsupSgcOperation.PATCH);
			crousPatchIdentifier.setPatchSuccess(true);
			crousPatchIdentifierDaoService.merge(crousPatchIdentifier);
			log.info("crous patchIdentifier " + crousPatchIdentifier + " ok");
		} catch(Exception e) {
			crousPatchIdentifier.setPatchSuccess(false);
            crousPatchIdentifierDaoService.merge(crousPatchIdentifier);
			log.warn("Error patchIdentifier : " + crousPatchIdentifier + " - " + e.getMessage());
		}
	}


    @Transactional
	public synchronized void deletePatchIdentifiants() {
		inWorking = true;
		crousPatchIdentifierDaoService.removeAll();
		inWorking = false;
	}


	@Async
	public synchronized void generatePatchIdentifiersIne() {
		inWorking = true;
		for(User user : userDaoService.findUsers4PatchIdentifiersIne()) {
			try {
				CrousPatchIdentifier patchIdentifier = new CrousPatchIdentifier();
				patchIdentifier.setOldId(user.getCrousIdentifier());
				patchIdentifier.setEppnNewId(user.getSupannCodeINE());
				patchIdentifier.setMail(user.getEmail());
                crousPatchIdentifierDaoService.persist(patchIdentifier);
			} catch(Exception e) {
				log.error("Error trying generating Patch Identifier with Ine for " + user.getEppn() + " : " + e.getMessage(), e);
			}
		}
		inWorking = false;
	}
}

