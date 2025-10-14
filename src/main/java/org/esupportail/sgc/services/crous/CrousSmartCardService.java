package org.esupportail.sgc.services.crous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CrousSmartCardService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	SimpleDateFormat csvDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	private Boolean inWorking = false; 
	
	@Resource
	CrousSmartCardEntryService crousSmartCardEntryService;
	

	public Boolean isInWorking() {
		return inWorking;
	}

	
	/*
	 * 
	PIX.SS;
	PIX.NN;
	AAPL;
	NUM_PROTOCOLAIRE;
	NUM_APPLICATIF;
	NFO;
	CNOUS;
	CROUS;
	EMETTEUR;
	MAPPING;
	NUM_CARTE;
	DATE_CREATION
	 */
	@Async
	public synchronized void consumeCsv(InputStream stream, Boolean inverseCsn) throws IOException {
		inWorking = true;
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String line;
		int i = 0;
		in.readLine(); // ignore header line;
		while ((line = in.readLine()) != null) {
			try {
				crousSmartCardEntryService.consumeCsvLine(line, inverseCsn);
				i++;
			} catch(Exception e) {
				log.error("Error integrating this csv line : " + line, e);
			}
		}
		log.info(i + " crous smartcards imported" );
		inWorking = false;
	}

}
