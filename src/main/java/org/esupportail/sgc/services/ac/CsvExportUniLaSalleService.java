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

public class CsvExportUniLaSalleService implements Export2AccessControlService, SmartLifecycle {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_AC = "ISO-8859-1";
	
	private final static String CSV_FILENAME =  "uniLaSalle-ac.csv";
	
	private boolean isRunning = false;
	
	private String eppnFilter = ".*";
	
	@Resource
	AccessService uniLaSalleVfsAccessService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AppliConfigService appliConfigService;

	private Set<String> queueEppns2Update = new HashSet<String>();
	

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	public void sync(List<String> eppns) throws IOException {
		synchronized (queueEppns2Update) {
			log.info("Ajout de " + eppns + " à la liste des eppns à mettre à jour dans le contrôle d'accès - maj par paquet tous les jours");
			queueEppns2Update.addAll(eppns);
		}
	} 
	
	public boolean syncNow(List<String> eppns) throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(eppns).toString(), ENCODING_AC);
		String filename = CSV_FILENAME;
		return uniLaSalleVfsAccessService.putFile(null, filename, csv, false);
	} 
	
	
	/**
	 * Majs sur le contrôle d'accès par paquets - tous les jours
	 */
	public void sync(String eppn) throws IOException {
		synchronized (queueEppns2Update) {
			log.info("Ajout de " + eppn + " à la liste des eppns à mettre à jour dans le contrôle d'accès - maj par paquet tous les jours");
			queueEppns2Update.add(eppn);
		}
	}
	
	@Transactional
	@Scheduled(cron="0 0,5,10,15,20,25,30,35,40,45,50,55 * * * *")
	private void export2AcSqueue() throws IOException {
		synchronized (queueEppns2Update) {
			if(!queueEppns2Update.isEmpty()) {
				List<String> eppns = new ArrayList<String>(queueEppns2Update);
				log.info("Maj le contrôle d'accès par paquet pour les eppns : " + eppns);
				if(this.syncNow(eppns)) {
					queueEppns2Update = new HashSet<String>();
				}
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
	 * @return
	 */
	private String formatDate(Date date) {
		String dateFt = "";
		if(date!=null) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
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
			log.info("Serveur stoppé ... maj le contrôle d'accès par paquet appellé si besoin");
			export2AcSqueue();
		} catch (IOException e) {
			log.error("Error during export2AcSqueue", e);
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
