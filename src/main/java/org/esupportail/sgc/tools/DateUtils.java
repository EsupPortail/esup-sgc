package org.esupportail.sgc.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DateUtils {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String DATE_FORMAT_FR = "dd/MM/yyyy";
	
	private final static String DATE_FORMAT_SCHACBIRTH_LDAP = "yyyyMMdd";
	
	private SimpleDateFormat dateFormatterSchacOfBirth = new SimpleDateFormat(DATE_FORMAT_SCHACBIRTH_LDAP);
	
	private SimpleDateFormat dateFormatterFr = new SimpleDateFormat(DATE_FORMAT_FR);

	
	public String schadDateOfBirthDay2FrenchDate(String schadDateOfBirthDay) {
    	Date date = parseSchacDateOfBirth(schadDateOfBirthDay);
    	String dateFr = "";
    	if(date != null) {
    		dateFr = dateFormatterFr.format(date);
    	}
    	return dateFr;
    }
	
	public Date parseSchacDateOfBirth(String dateString) {
		Date date = null;
		if(dateString!=null && !dateString.isEmpty()) {
			log.trace("parsing of date : " + dateString);
			try {
				date = dateFormatterSchacOfBirth.parse(dateString);
			} catch (ParseException | NumberFormatException e) {
				log.error("parsing of date " + dateString + " failed");
			}
		}
		return date;
	}
	
}
