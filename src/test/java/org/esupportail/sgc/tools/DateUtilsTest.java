package org.esupportail.sgc.tools;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml", "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml"})
@WebAppConfiguration
public class DateUtilsTest {

    @Resource
    DateUtils dateUtils;

    @Test
    void testSchadDateOfBirthDay2FrenchDate() {
        String schadDate = "20240115";
        String frenchDate = dateUtils.schadDateOfBirthDay2FrenchDate(schadDate);
        assert(frenchDate.equals("15/01/2024"));
    }
}
