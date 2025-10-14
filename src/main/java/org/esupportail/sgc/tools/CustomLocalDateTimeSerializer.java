package org.esupportail.sgc.tools;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> implements ContextualSerializer {

    private final DateTimeFormatter formatter;
    private final boolean forceUtc;

    public CustomLocalDateTimeSerializer() {
        this(null, false);
    }

    public CustomLocalDateTimeSerializer(DateTimeFormatter formatter, boolean forceUtc) {
        this.formatter = formatter;
        this.forceUtc = forceUtc;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (formatter != null) {
            if (forceUtc) {
                OffsetDateTime odt = value.atOffset(ZoneOffset.UTC);
                gen.writeString(odt.format(formatter));
            } else {
                gen.writeString(value.format(formatter));
            }
        } else {
            gen.writeString(value.toString());
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, com.fasterxml.jackson.databind.BeanProperty property) {
        if (property != null) {
            JsonFormat format = property.getAnnotation(JsonFormat.class);
            if (format != null) {
                String pattern = format.pattern();
                if (!pattern.isEmpty()) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                    boolean hasOffset = pattern.contains("X") || pattern.contains("Z");
                    return new CustomLocalDateTimeSerializer(dtf, hasOffset);
                }
            }
        }
        return this;
    }
}

