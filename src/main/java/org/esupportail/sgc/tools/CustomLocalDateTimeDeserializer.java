package org.esupportail.sgc.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final Logger log = LoggerFactory.getLogger(CustomLocalDateTimeDeserializer.class);

    static String FULL_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    static String DATE_ONLY_PATTERN = "yyyy-MM-dd";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(FULL_PATTERN);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_ONLY_PATTERN);

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        if(value.length()>=FULL_PATTERN.length()) {
            String value4dateTime = value.substring(0, FULL_PATTERN.length());
            try {
                return LocalDateTime.parse(value4dateTime, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
               log.trace("Failed to parse LocalDateTime with full pattern, trying date only: " + value);
            }
        }
        if(value.length()>=DATE_ONLY_PATTERN.length()) {
            String value4date = value.substring(0, DATE_ONLY_PATTERN.length());
            try {
                LocalDate date = LocalDate.parse(value4date, DATE_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IOException("Invalid date format for LocalDate: " + value, e);
            }
        }
        throw new IOException("Invalid date format for LocalDateTime: " + value);
    }
}

