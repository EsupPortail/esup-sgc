package org.esupportail.sgc.services.ac;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.fs.AccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CsvExportAcInsaService implements Export2AccessControlService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String ENCODING = "ISO-8859-1";
	
	private final static String CSV_FILENAME =  "insa-ac-from-esup-sgc.csv";

	// 2524518000000 ms == 31/12/2049
	private final static LocalDateTime DATE_MAX = Instant.ofEpochMilli(2524518000000L)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

	private String eppnFilter = ".*";
	
	@Resource
	CardEtatService cardEtatService;

	AccessService accessService;
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    UserDaoService userDaoService;
	
	public CsvExportAcInsaService(AccessService accessService) {
		super();
		this.accessService = accessService;
	}

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	/* (non-Javadoc)
	 * @see org.esupportail.sgc.services.ac.Export2AccessControlService#sync(java.util.List)
	 */
	@Override
	public void sync(List<String> eppns) throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(eppns).toString(), ENCODING);
		accessService.putFile(null, CSV_FILENAME, csv, true);
	} 
	
	/* (non-Javadoc)
	 * @see org.esupportail.sgc.services.ac.Export2AccessControlService#sync(java.lang.String)
	 */
	@Override
	public void sync(String eppn) throws IOException {
		
		String csvStr = sgc2csv4eppn(eppn).toString();
		
		InputStream csv = IOUtils.toInputStream(csvStr, ENCODING);
		accessService.putFile(null, eppn + "_" + CSV_FILENAME, csv, true);
	}
	
	private StringBuffer sgc2csv(List<String> eppns) {

		StringBuffer sBuffer = new StringBuffer();
		
		List<Card> cards = null;
		if(eppns == null) {
			cards = cardEtatService.getAllEnableableCardsWithEppnDistinct();
		} else {
			cards = cardEtatService.getAllEnableableCardsWithEppnDistinct(eppns);
		}
		
        for(Card card : cards) {
        	if(card.getEtat().equals(Etat.ENABLED)) {
        		sBuffer.append(sgc2csv(card));
        		sBuffer.append("\r\n");
        	}
		}
        
        return sBuffer;
	}
	
	private StringBuffer sgc2csv4eppn(String eppn) {

		return sgc2csv(Arrays.asList(new String[] {eppn}));
	}
	
	/**
	 * @param card
	 * @return Verso6,NOM,Prénom,,supannEtablissement(sanslepréfixe{UAI}),verso7,Date d'activation de la carte,schacExpiryDate,13,ReversedCSN passé en décimal 
	 */
	private String sgc2csv(Card card) {
		ArrayList<String> fields = new ArrayList<String>();
		
		User user = userDaoService.findUser(card.getEppn());
		
		// Hack : verso6 contient identifiant base métier pour personnels et étudiants insa, ce préfixé par 08, ce sur 8 caractères
		// pour cartes extérieurs, sans verso6, on construit cet identifant en préfixant par 07 l'identifiant BD du user de esup-sgc
		
		Boolean userIsExternal = user.getHasExternalCard();
		
		if(userIsExternal) {
			String id4ac = user.getId().toString();
			// padding de 8-2->6
			id4ac = String.format("%6s", id4ac).replace(' ', '0');
			// ajout du préfixe 07
			id4ac = "07" + id4ac;
			fields.add(id4ac);
		} else {
			fields.add(user.getVerso6());
		}
		
		// nom 15 caractères max
		if(user.getName()!=null && user.getName().length()>15) {
			fields.add(user.getName().substring(0, 15));	
		} else {
			fields.add(user.getName());	
		}
		// prenom 13 caractères max
		if(user.getFirstname()!=null && user.getFirstname().length()>13) {
			fields.add(user.getFirstname().substring(0, 13));	
		} else {
			fields.add(user.getFirstname());	
		}
		fields.add("");
		fields.add(user.getRneEtablissement());
		if(userIsExternal) {
			fields.add("011");
		} else {
			fields.add(user.getVerso7());
		}
		fields.add(formatDate(card.getEnnabledDate()));
		fields.add(formatDate(card.getDueDate()));
		fields.add("13");
		String reversedCsnAsDecimal = getReversedCsnAsDecimal(card.getReverseCsn());
		fields.add(reversedCsnAsDecimal);
		
		return StringUtils.join(fields, ",");
	}

	private String getReversedCsnAsDecimal(String rcsn) {
		long hexaAsInt = 0;
		for(String s : rcsn.split("")) {
			hexaAsInt = hexaAsInt*16 + Integer.parseInt(s, 16 );
		}
		return String.valueOf(hexaAsInt);
	}

	/**
	 * -> YYYY/MM/DD
	 * @return
	 */
	private String formatDate(LocalDateTime date) {
		String dateFt = "";
		if(date!=null) {
            LocalDateTime date2print = date;
			if(date2print.isAfter(DATE_MAX)) {
				date2print = DATE_MAX;
			}
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			dateFt = "'"+df.format(date2print);
		}
		return dateFt;
	}

}

