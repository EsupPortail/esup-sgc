package org.esupportail.sgc.services.crous;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class ApiCrousServiceTest {

	@Resource
	ApiCrousService apiCrousService;
	
	@Test
	public void testAuthentication() {
		apiCrousService.authenticate();
	}
}
