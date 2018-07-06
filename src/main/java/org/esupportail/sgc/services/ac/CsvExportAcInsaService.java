package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.io.InputStream;
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

public class CsvExportAcInsaService implements Export2AccessControlService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String ENCODING = "ISO-8859-1";
	
	private final static String CSV_FILENAME =  "insa-ac-from-esup-sgc.csv";
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AccessService insaVfsAccessService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	/* (non-Javadoc)
	 * @see org.esupportail.sgc.services.ac.Export2AccessControlService#sync(java.util.List)
	 */
	@Override
	public void sync(List<String> eppns) throws IOException {
		InputStream csv = IOUtils.toInputStream(sgc2csv(eppns).toString(), ENCODING);
		insaVfsAccessService.putFile(null, CSV_FILENAME, csv, true);
	} 
	
	/* (non-Javadoc)
	 * @see org.esupportail.sgc.services.ac.Export2AccessControlService#sync(java.lang.String)
	 */
	@Override
	public void sync(String eppn) throws IOException {
		
		String csvStr = sgc2csv4eppn(eppn).toString();
		
		InputStream csv = IOUtils.toInputStream(csvStr, ENCODING);
		insaVfsAccessService.putFile(null, eppn + "_" + CSV_FILENAME, csv, true);
	}
	
	private StringBuffer sgc2csv(List<String> eppns) {

		StringBuffer sBuffer = new StringBuffer();
		
		List<Card> cards = null;
		if(eppns == null) {
			cards = cardEtatService.getAllEncodedCardsWithEppnDistinct();
		} else {
			cards = cardEtatService.getAllEncodedCardsWithEppnDistinct(eppns);
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
	 * @return Leocode,NOM,Prénom,,supannEtablissement(sanslepréfixe{UAI}),verso7,Date d'activation de la carte,schacExpiryDate,13,ReversedCSN passé en décimal 
	 */
	private String sgc2csv(Card card) {
		ArrayList<String> fields = new ArrayList<String>();
		
		User user = User.findUser(card.getEppn());
		
		fields.add(user.getSecondaryId()); 
		fields.add(user.getName());	
		fields.add(user.getFirstname());
		fields.add("");
		fields.add(user.getRneEtablissement());
		fields.add(user.getVerso7());
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
	private String formatDate(Date date) {
		String dateFt = "";
		if(date!=null) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			dateFt = df.format(date);
		}
		return dateFt;
	}

}

