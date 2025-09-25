package org.esupportail.sgc.tools;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.annotation.Resource;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.EscCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.crous.RightHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml", "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml"})
@WebAppConfiguration
public class JsonObjectMapperTest {

    @Resource
    ObjectMapper objectMapper;

    @Test
    void shouldSerializeLocalDateTimeWithOffsetPattern() throws Exception {
        LocalDateTime date = LocalDateTime.of(2017, 1, 1, 0, 0, 0, 1_000_000); // 2017-01-01T00:00:00.001
        RightHolder rightHolder = new RightHolder();
        rightHolder.setBirthDate(date);

        String json = objectMapper.writeValueAsString(rightHolder);

        assertTrue(json.contains("\"birthDate\":\"2017-01-01T00:00:00.001Z\""));
    }

    @Test
    void userDateJsonFormat() throws Exception {
        LocalDateTime date = LocalDateTime.of(2017, 1, 1, 0, 0, 0, 1_000_000); // 2017-01-01T00:00:00.001
        User user = new User();
        user.setBirthday(date);
        user.setUpdateDate(date);

        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("userFilter", SimpleBeanPropertyFilter.filterOutAllExcept("eppn", "cards", "crous", "europeanStudentCard", "difPhoto", "name", "firstname", "birthday", "email", "dueDate"))
                .addFilter("cardFilter", SimpleBeanPropertyFilter.filterOutAllExcept("id", "csn", "etat", "dateEtat", "desfireIds", "escnUid"));
        String json = objectMapper.writer(filters).writeValueAsString(user);

        assertTrue(json.contains("\"birthday\":\"2017-01-01\""));
    }


    @Test
    void checkLocalDate2JsonFormat() throws JsonProcessingException {
        // scenario : csv crous -> esup-sgc localdatetime -> jsonformat with offset  for crous api
        DateTimeFormatter csvDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime localDateTime = LocalDate.parse("01/01/2017", csvDateFormat).atStartOfDay();
        CrousSmartCard smartCard = new CrousSmartCard();
        smartCard.setZdcCreationDate(localDateTime);
        String json = objectMapper.writeValueAsString(smartCard);
        assertTrue(json.contains("\"zdcCreationDate\":\"2017-01-01T00:00:00.000Z\""));
    }

    @Test
    void deserializeEscCard() throws JsonProcessingException {
        String json = "{\"expiresAt\": \"2024-06-26\"}";
        EscCard escCard = objectMapper.readValue(json, EscCard.class);
        assertNotNull(escCard.getExpiresAt());
        assertEquals(LocalDate.of(2024, 6, 26).atStartOfDay(), escCard.getExpiresAt());

        String json2 = "{\"expiresAt\": \"2025-06-19T02:00:00.000+02\"}";
        EscCard escCard2 = objectMapper.readValue(json2, EscCard.class);
        assertNotNull(escCard2.getExpiresAt());
        assertEquals(LocalDate.of(2025, 6, 19).atStartOfDay(), escCard2.getExpiresAt());
    }

}
