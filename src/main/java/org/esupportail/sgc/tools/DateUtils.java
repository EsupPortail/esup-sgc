package org.esupportail.sgc.tools;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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


    public LocalDateTime parseSchacDateOfBirth(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        log.trace("parsing of date : " + dateString);

        try {
            LocalDate date = LocalDate.parse(dateString, dateTimeFormatterSchacOfBirth);
            return date.atStartOfDay(); // transforme la date en LocalDateTime à 00:00
        } catch (DateTimeParseException e) {
            log.error("parsing of date " + dateString + " failed: " + e.getMessage(), e);
            return null;
        }
    }

	public String getGeneralizedTime(LocalDateTime date) {
		if(date == null) return "";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'");
		return date.format(dateFormatter);
	}
}
