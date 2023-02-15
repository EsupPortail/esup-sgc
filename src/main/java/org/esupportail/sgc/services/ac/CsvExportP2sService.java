package org.esupportail.sgc.services.ac;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.fs.AccessService;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvExportP2sService implements Export2AccessControlService, ApplicationListener<ContextRefreshedEvent> {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_P2S = "ISO-8859-1";
	
	private String eppnFilter = ".*";

	AccessService accessService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AppliConfigService appliConfigService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	Map<String, String> queueEppns2Update = new HashMap<>();

	String filename;
	int nbLinesMax;
	
	public CsvExportP2sService(AccessService accessService) {
		super();
		this.accessService = accessService;
	}
	
	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// Hack : @PostConstruct / @PreDestroy : pas d'entitymanager dans les beans
		setupConfig();
	}

	public void setupConfig() {
		filename = appliConfigService.getP2sExportcsvFilename();
		nbLinesMax = appliConfigService.getP2sExportcsvNbLinesMax();
	}

	public void sync(List<String> eppns) throws IOException {
		if (eppns.isEmpty()) {
			return;
		}
		log.info("Ajout de " + eppns + " à la liste des eppns à mettre à jour dans P2S - maj par paquet toutes les 5 minutes");
		for(String eppn : eppns) {
			queueEppns2Update.put(eppn, sgc2csv(Arrays.asList(eppn)).toString());
		}
		flush();
	}
	/* with P2S we can't import a very big file, so we split it*/
	synchronized void flush() throws IOException {
		
		if(queueEppns2Update.keySet().isEmpty()) {
			return;
		}
		
		Date date = new Date();
		int i = 0;
		int j = 1;
		String csvLines = null;
		for(String csvLine: queueEppns2Update.values()) {
			i++;
			if(i>nbLinesMax) {
				InputStream csv = IOUtils.toInputStream(csvLines, ENCODING_P2S);
				accessService.putFile(null, date.getTime() + "_" + j + "_" + filename, csv, false);
				i=1;
				j++;
				csvLines = null;
			} 
			if(csvLines == null) {
				csvLines = csvLine;
			} else {
				csvLines = csvLines + csvLine;
			}
		}
		if(!"".equals(csvLines)) {
			InputStream csv = IOUtils.toInputStream(csvLines, ENCODING_P2S);
			accessService.putFile(null, date.getTime() + "_" + j + "_" + filename, csv, false);
		}
		queueEppns2Update = new HashMap<>();
	}
	
	/**
	 * Majs sur P2S par paquets - toutes les 5 minutes
	 */
	public void sync(String eppn) throws IOException {
		log.info("Ajout de " + eppn + " à la liste des eppns à mettre à jour dans P2S - maj par paquet toutes les 5 minutes");
		queueEppns2Update.put(eppn, sgc2csv(Arrays.asList(eppn)).toString());
	}
	
	@Transactional
	@Scheduled(cron="0 0,5,10,15,20,25,30,35,40,45,50,55 * * * *")
	void export2P2Squeue() throws IOException {
		setupConfig();
		if(!queueEppns2Update.isEmpty()) {
			log.info("Maj P2S par paquet pour les eppns : " + queueEppns2Update.keySet());
			this.flush();
		}
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
				if (card.getEnnabledDate() != null) {
					sBuffer.append(sgcId2csv(card));
					sBuffer.append("\n");
				}
        	}
		}
        
        return sBuffer;
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
	 */
	private String formatDate(Date date) {
		String dateFt = "";
		if(date!=null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
			dateFt = df.format(date);
		}
		return dateFt;
	}

	@PreDestroy
	public void stop() {
		// Hack : @PostConstruct / @PreDestroy : pas d'entitymanager dans les beans
		// et log4j HS
		try {
			System.out.println("Serveur stoppé ... maj P2S par paquet appellé si besoin");
			if(!queueEppns2Update.isEmpty()) {
				System.out.println("Maj P2S par paquet pour les eppns : " + queueEppns2Update.keySet());
				this.flush();
			}
		} catch (Exception e) {
			System.err.println("Error during export2P2Squeue : " + e.getMessage());
		}
	}

}
