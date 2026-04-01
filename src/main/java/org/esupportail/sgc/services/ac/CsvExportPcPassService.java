package org.esupportail.sgc.services.ac;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.fs.AccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvExportPcPassService implements Export2AccessControlService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_PCPASS = "UTF-8";
	
	private final static String CSV_FILENAME =  "pcpass.csv";

	private String eppnFilter = ".*";
	
	AccessService accessService;
	
	@Resource
	CardEtatService cardEtatService;

	Map<String, String> queueEppns2Update = new HashMap<>();

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}
	
	public void sync(List<String> eppns) throws IOException {
		log.info("Ajout de " + eppns + " à la liste des eppns à mettre à jour dans PCPASS - maj par paquet tous les jours");
		for(String eppn : eppns) {
			queueEppns2Update.put(eppn, sgc2csv(Arrays.asList(eppn)).toString());
		}
	}

	synchronized boolean flush() throws IOException {
		log.info("Maj PCPASS par paquet pour les eppns : " + queueEppns2Update.keySet());
		String csvString = "nom;prenom;CSN;type population;Leocode;date début validité; date fin validité;uid\r\n";
		for(String csv : queueEppns2Update.values()) {
			csvString += csv;
		}
		InputStream csv = IOUtils.toInputStream(csvString, ENCODING_PCPASS);
		String filename = CSV_FILENAME;
		return accessService.putFile(null, filename, csv, false);
	} 
	
	
	/**
	 * Majs sur PCPASS par paquets - tous les jours
	 */
	public void sync(String eppn) throws IOException {
		sync(Arrays.asList(eppn));
	}
	
	@Transactional
	@Scheduled(cron="0 0 10 * * *")
	void export2PcPassSqueue() throws IOException {
		if(!queueEppns2Update.isEmpty()) {
			if(this.flush()) {
				queueEppns2Update = new HashMap<>();
			}
		}
	}
	
	private StringBuffer sgc2csv(List<String> eppn4Update) {
		StringBuffer sBuffer = new StringBuffer();
		List<Card> cards = cardEtatService.getAllEncodedCards(eppn4Update);
        for(Card card : cards) {
        	if(card.getEtat().equals(Etat.ENABLED) || card.getEtat().equals(Etat.DISABLED) || card.getEtat().equals(Etat.CADUC)) {
				if (card.getEnnabledDate() != null) {
					sBuffer.append(sgcId2csv(card));
					sBuffer.append("\r\n");
				}
        	}
		}
        return sBuffer;
	}

	/**
	 * @param card
	 * @return nom;prenom;CSN;type population;Leocode;date début validité; date fin validité;uid
	 */
	private String sgcId2csv(Card card) {

		ArrayList<String> fields = new ArrayList<String>();
		
		User user = card.getUser();
		
		fields.add(user.getName());	
		fields.add(user.getFirstname());
		fields.add(card.getReverseCsn());
		
		String population = "E".equals(card.getUserType()) ? "ETUDIANT" : "P".equals(card.getUserType()) ? "PERSONNEL" : "I".equals(card.getUserType()) ? "LECTEUR" : "EXTERIEUR";
		fields.add(population);
		
		fields.add(user.getSecondaryId());
		fields.add(formatDate(card.getEnnabledDate()));
		
		if(card.isEnabled()) {
			fields.add(formatDate(card.getDueDate()));
		} else {
			fields.add(formatDate(card.getDateEtat()));
		}

		fields.add(user.getEppn());
		
		return StringUtils.join(fields, ";");
	}

	/**
	 * -> YYYY/MM/DD
	 * @return
	 */
	private String formatDate(LocalDateTime date) {
		String dateFt = "";
		if(date!=null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
			dateFt = date.format(df);
		}
		return dateFt;
	}

	@PreDestroy
	public void stop() {
		// Hack : @PostConstruct / @PreDestroy : pas d'entitymanager dans les beans
		// et log4j HS
		try {
			System.out.println("Serveur stoppé ... maj PCPASS par paquet appellé si besoin");
			if(!queueEppns2Update.isEmpty()) {
				System.out.println("Maj PCPASS par paquet pour les eppns : " + queueEppns2Update);
				this.flush();
			}
		} catch (Exception e) {
			System.err.println("Error during export2PcPassSqueue : " + e.getMessage());
		}
	}

}
