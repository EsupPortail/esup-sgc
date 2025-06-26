package org.esupportail.sgc.services.crous;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.User;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class CrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CrousService crousService;

	@Resource
	ApiCrousService apiCrousService;

    @Resource
    UserDaoService userDaoService;
	
	@Test
	public void testGetRightHolder() {
		List<User> users = userDaoService.findUsersByCrous(true).setMaxResults(1000).getResultList();
		assumeTrue(!users.isEmpty());
		for(User user: users) {
			if(LocalDateTime.now().isBefore(user.getDueDate())) {
				RightHolder rightHolder = crousService.getRightHolder(user.getEppn(), user.getEppn());
				log.info(String.format("rightHolder for %s : %s", user.getEppn(), rightHolder));
				break;
			}
		}
	}
	

	@Test
	public void testfieldWoDueDateEqualsRightHolder() {
		List<User> users = userDaoService.findUsersByCrous(true).setMaxResults(1000).getResultList();
		assumeTrue(!users.isEmpty());
		for(User user: users) {
			if(LocalDateTime.now().isBefore(user.getDueDate())) {
				RightHolder rightHolder = crousService.getRightHolder(user.getEppn(), user.getEppn());
				assumeTrue(rightHolder != null);
				assertTrue(apiCrousService.fieldsEqualsOrCanNotBeUpdate(rightHolder, rightHolder));
				break;
			}
		}
	}
	
	
}

