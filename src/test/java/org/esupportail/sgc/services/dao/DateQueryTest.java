package org.esupportail.sgc.services.dao;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.UserDaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml", "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml"})
@WebAppConfiguration
public class DateQueryTest {

    @Resource
    UserDaoService userDaoService;

    @Test
    void testFindAllUsersWithDueDateBeforeAndDueDateAfterNow() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(300);
        userDaoService.findAllUsersWithDueDateBeforeAndDueDateAfterNow(futureDate);
    }


}
