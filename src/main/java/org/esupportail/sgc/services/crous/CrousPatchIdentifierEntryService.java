package org.esupportail.sgc.services.crous;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.tools.HexStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class CrousPatchIdentifierEntryService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());


	/*
	/*
	 * 
	oldId;eppnNewId;mail
	 */
	public void consumeCsvLine(String line) throws IOException, ParseException {
		line = line.replaceAll("\\s+","");
		String[] fields = line.split(";");
		String oldId = fields[0];
		String eppnNewId = fields[1];
		String mail = fields[2];
		
		CrousPatchIdentifier crousPatchIdentifier = new CrousPatchIdentifier();
		crousPatchIdentifier.setOldId(oldId);
		crousPatchIdentifier.setEppnNewId(eppnNewId);
		crousPatchIdentifier.setMail(mail);
		crousPatchIdentifier.persist();
	}

}
