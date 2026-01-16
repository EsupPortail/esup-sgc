package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

/*
    * A deserializer for LocalDateTime that can handle "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSZ",...
    * Examples :
        * 2024-06-15T10:30:30.000Z
        * 2024-06-15T10:30:00.000
        * 2031-04-16T01:59:59.000+02
        * 2024-06-15T10:30:45
        * 2024-06-15T10:30:45.123456789+02:00
        * ... and other variations with or without milliseconds and timezone offsets.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .optionalStart()
            .appendPattern("X")  // Offset optionnel : Z, +02, +02:00, etc.
            .optionalEnd()
            .toFormatter();

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        return LocalDateTime.parse(dateString, FORMATTER);
    }
}