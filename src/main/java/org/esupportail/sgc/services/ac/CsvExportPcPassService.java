package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

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

public class CsvExportPcPassService implements Export2AccessControlService, SmartLifecycle {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_PCPASS = "UTF-8";
	
	private final static String CSV_FILENAME =  "pcpass.csv";
	
	private boolean isRunning = false;
	
	@Resource
	AccessService pcPassVfsAccessService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AppliConfigService appliConfigService;

	private Set<String> queueEppns2Update = new HashSet<String>();
	

	public void sync(List<String> eppns) throws IOException {
		synchronized (queueEppns2Update) {
			log.info("Ajout de " + eppns + " à la liste des eppns à mettre à jour dans PCPASS - maj par paquet tous les jours");
			queueEppns2Update.addAll(eppns);
		}
	} 
	
	public boolean syncNow(List<String> eppns) throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(eppns).toString(), ENCODING_PCPASS);
		String filename = CSV_FILENAME;
		return pcPassVfsAccessService.putFile(null, filename, csv, false);
	} 
	
	
	/**
	 * Majs sur PCPASS par paquets - tous les jours
	 */
	public void sync(String eppn) throws IOException {
		synchronized (queueEppns2Update) {
			log.info("Ajout de " + eppn + " à la liste des eppns à mettre à jour dans PCPASS - maj par paquet tous les jours");
			queueEppns2Update.add(eppn);
		}
	}
	
	@Transactional
	@Scheduled(cron="0 0 10 * * *")
	private void export2PcPassSqueue() throws IOException {
		synchronized (queueEppns2Update) {
			if(!queueEppns2Update.isEmpty()) {
				List<String> eppns = new ArrayList<String>(queueEppns2Update);
				log.info("Maj PCPASS par paquet pour les eppns : " + eppns);
				if(this.syncNow(eppns)) {
					queueEppns2Update = new HashSet<String>();
				}
			}
		}
	}
	
	private StringBuffer sgc2csv(List<String> eppn4Update) {

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("nom;prenom;CSN;type population;Leocode;date début validité; date fin validité;uid\r\n");
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
	private String formatDate(Date date) {
		String dateFt = "";
		if(date!=null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			dateFt = df.format(date);
		}
		return dateFt;
	}
	

	@Override
	public void start() {
		isRunning = true;
	}

	@Override
	public void stop() {
		try {
			log.info("Serveur stoppé ... maj PCPASS par paquet appellé si besoin");
			export2PcPassSqueue();
		} catch (IOException e) {
			log.error("Error during export2PcPassSqueue", e);
		}
		isRunning = false;
	}
	

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	/** Run as early as possible so the shutdown method can still use transactions. */
	@Override
	public int getPhase() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}


}
