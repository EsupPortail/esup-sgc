package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

public class CsvExportSynchronicService implements Export2AccessControlService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String ENCODING_P2S = "ISO-8859-1";
	
	final static Date DATE_MAX = new Date(2037-1900, 11, 31);
	
	public final static List<String> genericCartesLibelles = Arrays.asList(new String[] {"Exterieur", "Service"});
	
	private String eppnFilter = ".*";
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AccessService synchronicVfsAccessService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	/* Méthode en 1 seul fichier */
	public void sync(List<String> eppns) throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(eppns).toString(), ENCODING_P2S);
		String filename = appliConfigService.getSynchronicExportcsvFilename();
		synchronicVfsAccessService.putFile(null, filename, csv, true);
	} 
	
	public void sync(String eppn) throws IOException {
		
		String filename = appliConfigService.getSynchronicExportcsvFilename();
		
		String csvStr = sgc2csv4eppn(eppn).toString();
		
		InputStream csv = IOUtils.toInputStream(csvStr, ENCODING_P2S);
		synchronicVfsAccessService.putFile(null, eppn + "_" + filename, csv, true);
	}
	
	private StringBuffer sgc2csv(List<String> eppns4UpdateSynchronic) {

		StringBuffer sBuffer = new StringBuffer();
		
		/* Synchronic ne s'attend à avoir qu'une seule carte par individu - il faut donc lui transmettre la dernière en date */
		List<Card> cards = null;
		if(eppns4UpdateSynchronic == null) {
			cards = cardEtatService.getAllEnableableCardsWithEppnDistinct();
		} else {
			cards = cardEtatService.getAllEnableableCardsWithEppnDistinct(eppns4UpdateSynchronic);
		}
		
        for(Card card : cards) {
        	if(card.getEtat().equals(Etat.ENABLED) || card.getEtat().equals(Etat.DISABLED) || card.getEtat().equals(Etat.CADUC)) {
	        	sBuffer.append(sgc2csv(card));
	        	sBuffer.append("\r\n");
        	}
		}
        
        return sBuffer;
	}
	
	public StringBuffer sgc2csv4eppn(String eppn) {

		return sgc2csv(Arrays.asList(new String[] {eppn}));
	}
	
	/**
	 * @param card
	 * @return Leocode;NOM;Prénom;IDP2S;Date_début;Date_fin;Bloqué;Date_Naissance;Société 
	 * Exemple : 112340000987;DALTON;JOE;10340000987;20140101;20371231;NON;31/12/1987;Université de Ville
	 */
	private String sgc2csv(Card card) {
		ArrayList<String> fields = new ArrayList<String>();
		
		User user = User.findUser(card.getEppn());
		
		fields.add(user.getSecondaryId());
		fields.add(user.getName());	
		fields.add(user.getFirstname());
		
		String desfireId = card.getDesfireIds().get(AccessControlService.AC_APP_NAME);
		fields.add(desfireId);
		
		fields.add(formatDate4Synchronic(card.getEnnabledDate()));
		fields.add(formatDate4Synchronic(card.getDueDate()));
		
		String statusSynchronic = Etat.ENABLED.equals(card.getEtat()) ? "NON" : "OUI";
		fields.add(statusSynchronic);
		
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
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			dateFt = df.format(date);
		}
		return dateFt;
	}
	

	/**
	 * -> YYYY/MM/DD
	 * @return
	 */
	private String formatDate4Synchronic(Date date) {
		String dateFt = "";
		if(date!=null) {
			if(date.after(DATE_MAX)) {
				date = DATE_MAX;
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			dateFt = df.format(date);
		}
		return dateFt;
	}
	
	/**
	 * -> YYYY/MM/DD
	 * @return
	 */
	private String hexAsInt(String hex) {
		BigInteger value = new BigInteger(hex, 16);  
		return value.toString();
	}

}
