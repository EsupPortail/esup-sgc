package org.esupportail.sgc.services.crous;

import jakarta.annotation.Resource;


import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class ApiCrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ApiCrousService apiCrousService;
	
	@Test
	public void testAuthentication() {
		apiCrousService.authenticate();
	}
	
}

