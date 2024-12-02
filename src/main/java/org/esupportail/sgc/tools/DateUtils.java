package org.esupportail.sgc.tools;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DateUtils {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String DATE_FORMAT_FR = "dd/MM/yyyy";
	
	private final static String DATE_FORMAT_SCHACBIRTH_LDAP = "yyyyMMdd";
	
	private DateTimeFormatter dateTimeFormatterSchacOfBirth = DateTimeFormatter.ofPattern(DATE_FORMAT_SCHACBIRTH_LDAP);
	
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
				date = Date.from(LocalDate.parse(dateString, dateTimeFormatterSchacOfBirth).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			} catch (Exception e) {
				log.error("parsing of date " + dateString + " failed " + e.getMessage());
			}
		}
		return date;
	}

	public String getGeneralizedTime(Date date) {
		if(date == null) return "";
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
		return dateFormatter.format(date);
	}
}
