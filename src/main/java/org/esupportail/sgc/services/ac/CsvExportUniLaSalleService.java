package org.esupportail.sgc.services.ac;

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

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

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

public class CsvExportUniLaSalleService implements Export2AccessControlService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_AC = "ISO-8859-1";
	
	private final static String CSV_FILENAME =  "uniLaSalle-ac.csv";
	
	private String eppnFilter = ".*";

	@Resource
	AccessService accessService;
	
	@Resource
	CardEtatService cardEtatService;

	private Map<String, String> queueEppns2Update = new HashMap<>();
	
	public CsvExportUniLaSalleService(AccessService accessService) {
		super();
		this.accessService = accessService;
	}

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}
	
	public synchronized void sync(List<String> eppns) throws IOException {
		log.info("Ajout de " + eppns + " à la liste des eppns à mettre à jour dans le contrôle d'accès - maj par paquet tous les jours");
		for(String eppn : eppns) {
			queueEppns2Update.put(eppn, sgc2csv(Arrays.asList(eppn)).toString());
		}
	} 
	
	synchronized boolean flush() throws IOException {
		String csvString = "";
		for(String csv : queueEppns2Update.values()) {
			csvString += csv;
		}
		InputStream csv = IOUtils.toInputStream(csvString, ENCODING_AC);
		String filename = CSV_FILENAME;
		return accessService.putFile(null, filename, csv, false);
	} 
	
	
	/**
	 * Majs sur le contrôle d'accès par paquets - tous les jours
	 */
	public void sync(String eppn) throws IOException {
		sync(Arrays.asList(eppn));
	}
	
	@Transactional
	@Scheduled(cron="0 0,5,10,15,20,25,30,35,40,45,50,55 * * * *")
	void export2AcSqueue() throws IOException {
		if(!queueEppns2Update.isEmpty()) {
			log.info("Maj le contrôle d'accès par paquet pour les eppns : " + queueEppns2Update.keySet());
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
        		sBuffer.append(sgcId2csv(card));
        		sBuffer.append("\r\n");
        	}
		}
        return sBuffer;
	}

	/**
	 * @param card
	 * @return access-control;supannEmpId/supannEtuId;nom;prenom;verso1 (titreCarteLibelle);date début validité; date fin validité; date délivrance;uid
	 */
	private String sgcId2csv(Card card) {

		ArrayList<String> fields = new ArrayList<String>();
		User user = card.getUser();

		fields.add(card.getDesfireIds().get(AccessControlService.AC_APP_NAME));
		if(user.getSupannEmpId() != null && !user.getSupannEmpId().isEmpty()) {
			fields.add(user.getSupannEmpId());	
		} else {
			fields.add(user.getSupannEtuId());	
		}
		
		fields.add(user.getName());	
		fields.add(user.getFirstname());
		fields.add(user.getVerso1());
		fields.add(formatDate(card.getEnnabledDate()));
		
		if(card.isEnabled()) {
			fields.add(formatDate(card.getDueDate()));
		} else {
			fields.add(formatDate(card.getDateEtat()));
		}
		
		if(card.getDeliveredDate() != null) {
			fields.add(formatDate(card.getDeliveredDate()));
		} else {
			fields.add(formatDate(card.getEnnabledDate()));
		}
		
		return StringUtils.join(fields, ";");
	}

	/**
	 * -> dd/MM/yyyy hh:mm
	 */
	private String formatDate(LocalDateTime date) {
		String dateFt = "";
		if(date!=null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");
			dateFt = df.format(date);
		}
		return dateFt;
	}


	@PreDestroy
	public void stop() {
		// Hack : @PostConstruct / @PreDestroy : pas d'entitymanager dans les beans
		// et log4j HS
		try {
			System.out.println("Serveur stoppé ... maj AC UniLasalle par paquet appellé si besoin");
			if(!queueEppns2Update.isEmpty()) {
				System.out.println("Maj PCPASS par paquet pour les eppns : " + queueEppns2Update.keySet());
				this.flush();
			}
		} catch (Exception e) {
			System.err.println("Error during export2AcSqueue : " + e.getMessage());
		}
	}

}
