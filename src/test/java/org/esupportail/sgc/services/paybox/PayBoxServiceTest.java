package org.esupportail.sgc.services.paybox;

import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class PayBoxServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    @Autowired(required = false)
    PayBoxService payBoxService;

    @Test
    public void testGetPayBoxActionUrl() {
    	assumeTrue(payBoxService!=null);
    	String payBoxActionUrl = payBoxService.getPayBoxActionUrl();
    	log.info(String.format("Get payBoxActionUrl : %s", payBoxActionUrl));
    }
}
