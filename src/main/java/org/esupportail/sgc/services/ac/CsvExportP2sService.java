package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.AppliConfig;
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

public class CsvExportP2sService implements Export2AccessControlService, SmartLifecycle {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_P2S = "ISO-8859-1";
	
	private boolean isRunning = false;
	
	private String eppnFilter = ".*";
	
	@Resource
	AccessService p2sVfsAccessService;
	
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

	public void sync() throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(null).toString(), ENCODING_P2S);
		String filename = appliConfigService.getP2sExportcsvFilename();
		Date date = new Date();
		p2sVfsAccessService.putFile(null, date.getTime() + "_" + filename, csv, false);
	} 
	
	/* with P2S we can't import a very big file, so we split it*/
	public void sync(List<String> eppns) throws IOException {
		
		if(eppns.isEmpty()) {
			return;
		}
		
		String filename = appliConfigService.getP2sExportcsvFilename();
		int nbLinesMax = appliConfigService.getP2sExportcsvNbLinesMax();
		
		String csvStr = sgc2csv(eppns).toString();
		List<String> csvStrList =  Arrays.asList(csvStr.split("\\r?\\n"));
		
		Date date = new Date();
		int i = 0;
		int j = 1;
		String csvLines = null;
		for(String csvLine: csvStrList) {
			i++;
			if(i>nbLinesMax) {
				InputStream csv = IOUtils.toInputStream(csvLines, ENCODING_P2S);
				p2sVfsAccessService.putFile(null, date.getTime() + "_" + j + "_" + filename, csv, false);
				i=1;
				j++;
				csvLines = null;
			} 
			if(csvLines == null) {
				csvLines = csvLine;
			} else {
				csvLines = csvLines + "\r\n" + csvLine;
			}
		}
		if(!"".equals(csvLines)) {
			InputStream csv = IOUtils.toInputStream(csvLines, ENCODING_P2S);
			p2sVfsAccessService.putFile(null, date.getTime() + "_" + j + "_" + filename, csv, false);
		}
	}
	
	/**
	 * Majs sur P2S par paquets - toutes les 5 minutes
	 */
	public void sync(String eppn) throws IOException {
		synchronized (queueEppns2Update) {
			log.info("Ajout de " + eppn + " à la liste des eppns à mettre à jour dans P2S - maj par paquet toutes les 5 minutes");
			queueEppns2Update.add(eppn);
		}
	}
	
	@Transactional
	@Scheduled(cron="0 0,5,10,15,20,25,30,35,40,45,50,55 * * * *")
	private void export2P2Squeue() throws IOException {
		synchronized (queueEppns2Update) {
			if(!queueEppns2Update.isEmpty()) {
				List<String> eppns = new ArrayList<String>(queueEppns2Update);
				log.info("Maj P2S par paquet pour les eppns : " + eppns);
				this.sync(eppns);
				queueEppns2Update = new HashSet<String>();
			}
		}
	}
	
	private void export2P2S4EppnWoQueue(String eppn) throws IOException {
		String filename = appliConfigService.getP2sExportcsvFilename();
		
		String csvStr = sgc2csv4eppn(eppn).toString();
		
		InputStream csv = IOUtils.toInputStream(csvStr, ENCODING_P2S);
		p2sVfsAccessService.putFile(null, eppn + "_" + filename, csv, false);
	}
	
	private StringBuffer sgc2csv(List<String> eppn4UpdateP2S) {

		StringBuffer sBuffer = new StringBuffer();

		List<Card> cards = null;
		if(eppn4UpdateP2S == null) {
			cards = cardEtatService.getAllEncodedCards();
		} else {
			cards = cardEtatService.getAllEncodedCards(eppn4UpdateP2S);
		}
		
        for(Card card : cards) {
        	if(card.getEtat().equals(Etat.ENABLED) || card.getEtat().equals(Etat.DISABLED) || card.getEtat().equals(Etat.CADUC)) {
        		sBuffer.append(sgcId2csv(card));
        		sBuffer.append("\n");
        	}
		}
        
        return sBuffer;
	}
	
	private StringBuffer sgc2csv4eppn(String eppn) {

		return sgc2csv(Arrays.asList(new String[] {eppn}));
	}
	
	/**
	 * @param card
	 * @return Léocode; NOM;Prénom;Identifiant_P2S;Début_de_validité;Fin_de_validité;Statut;Date_de_naissance;Établissement 
	 */
	private String sgcId2csv(Card card) {
		ArrayList<String> fields = new ArrayList<String>();
		
		User user = User.findUser(card.getEppn());
		
		fields.add(user.getSecondaryId());
		fields.add(user.getName());	
		fields.add(user.getFirstname());

		String desfireId = card.getDesfireIds().get(AccessControlService.AC_APP_NAME);
		fields.add(desfireId);
		fields.add(formatDate(card.getEnnabledDate()));
		fields.add(formatDate(card.getDueDate()));
		
		// dans P2S 1==bloqué
		String statusP2S = Etat.ENABLED.equals(card.getEtat()) ? "0" : "1";
		fields.add(statusP2S);
		
		// birthday can't be null in the access control
		String birthday = formatDate(user.getBirthday());
		if(birthday.isEmpty()) {
			birthday = formatDate(card.getDateEtat());
		}
		fields.add(birthday);
		
		fields.add(user.getInstitute());
		
		return StringUtils.join(fields, ";");
	}

	/**
	 * -> YYYY/MM/DD
	 * @return
	 */
	private String formatDate(Date date) {
		String dateFt = "";
		if(date!=null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
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
			log.info("Serveur stoppé ... maj P2S par paquet appellé si besoin");
			export2P2Squeue();
		} catch (IOException e) {
			log.error("Error during export2P2Squeue", e);
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
