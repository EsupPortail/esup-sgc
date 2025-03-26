package org.esupportail.sgc.services.crous;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class CrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CrousService crousService;

	@Resource
	ApiCrousService apiCrousService;
	
	@Test
	public void testGetRightHolder() {
		List<User> users = User.findUsersByCrous(true).setMaxResults(1000).getResultList();
		Assume.assumeTrue(!users.isEmpty());
		for(User user: users) {
			if((new Date()).before(user.getDueDate())) {
				RightHolder rightHolder = crousService.getRightHolder(user.getEppn(), user.getEppn());
				log.info(String.format("rightHolder for %s : %s", user.getEppn(), rightHolder));
				break;
			}
		}
	}
	

	@Test
	public void testfieldWoDueDateEqualsRightHolder() {
		List<User> users = User.findUsersByCrous(true).setMaxResults(1000).getResultList();
		Assume.assumeTrue(!users.isEmpty());
		for(User user: users) {
			if((new Date()).before(user.getDueDate())) {
				RightHolder rightHolder = crousService.getRightHolder(user.getEppn(), user.getEppn());
				Assume.assumeTrue(rightHolder != null);
				assertTrue(apiCrousService.fieldsEqualsOrCanNotBeUpdate(rightHolder, rightHolder));
				break;
			}
		}
	}
	
	
}

