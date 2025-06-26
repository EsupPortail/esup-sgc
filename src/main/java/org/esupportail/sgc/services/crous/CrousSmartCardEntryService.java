package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.dao.CrousSmartCardDaoService;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.tools.HexStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Transactional
@Service
public class CrousSmartCardEntryService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    DateTimeFormatter csvDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Resource
    CrousSmartCardDaoService crousSmartCardDaoService;

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
	public void consumeCsvLine(String line, Boolean inverseCsn) throws IOException, ParseException {
		line = line.replaceAll("\\s+","");
				String[] fields = line.split(";");
				String pixSs = fields[0];
				String pixNn = fields[1];
				String aapl = fields[2];
				String numProtocolaire = fields[3];
				String numApplicatif = fields[4];
				if(inverseCsn) {
					numProtocolaire = HexStringUtils.swapPairs(numProtocolaire);
					numApplicatif = HexStringUtils.swapPairs(numApplicatif);
				}
				String nfo = fields[5];
				String cnous = fields[6];
				String crous = fields[7];
				String emetteur = fields[8];
				String mapping = fields[9];
				String numCarte = fields[10];
				String dateCreation = fields[11];
				
				CrousSmartCard smartCard = crousSmartCardDaoService.findCrousSmartCard(numProtocolaire);
				if(smartCard==null) {
					smartCard = new CrousSmartCard();
				} else {
					if(!Long.valueOf(numCarte).equals(smartCard.getIdZdc())) {
						throw new SgcRuntimeException("Error : card with csn equals to " + numProtocolaire + " must have IdZdc equals to " + smartCard.getIdZdc() + " ?!", null);
					}
				}
				if(emetteur!=null && !emetteur.isEmpty()) {
					smartCard.setIdTransmitter(Long.valueOf(emetteur, 16));
				}
				if(mapping!=null && !mapping.isEmpty()) {
					smartCard.setIdMapping(Long.valueOf(mapping));
				}
				if(numCarte!=null && !numCarte.isEmpty()) {
					smartCard.setIdZdc(Long.valueOf(numCarte));
				}
                LocalDateTime zdcCreationDate = LocalDateTime.parse(dateCreation, csvDateFormat);
				smartCard.setZdcCreationDate(zdcCreationDate);
				smartCard.setPixSs(pixSs);
				smartCard.setPixNn(pixNn);
				smartCard.setAppl(aapl);
				if(numProtocolaire!=null) {
					numProtocolaire = numProtocolaire.toUpperCase();
				}
				if(numApplicatif!=null) {
					numApplicatif = numApplicatif.toUpperCase();
				}
				smartCard.setUid(numProtocolaire);
				smartCard.setRid(numApplicatif);	
				if(smartCard.getId() == null) {
                    crousSmartCardDaoService.persist(smartCard);
				} 
	}

}
