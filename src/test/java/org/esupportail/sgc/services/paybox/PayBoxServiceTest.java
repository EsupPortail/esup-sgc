package org.esupportail.sgc.services.paybox;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class PayBoxServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    @Autowired(required = false)
    PayBoxService payBoxService;

    @Test
    public void testGetPayBoxActionUrl() {
    	Assume.assumeTrue(payBoxService!=null);
    	String payBoxActionUrl = payBoxService.getPayBoxActionUrl();
    	log.info(String.format("Get payBoxActionUrl : %s", payBoxActionUrl));
    }
}
