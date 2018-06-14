package org.esupportail.sgc.services.crous;

import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class EsistCrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	EsistCrousService esistCrousService;
	
	@Test
	public void testComputeIdCompagnyRateAndIdRate() {
		List<User> users = User.findAllUsers();
		if(!users.isEmpty()) {
			User user = users.get(0);
			List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
			log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
		}	
	}
	
}

