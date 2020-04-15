package org.esupportail.sgc.services.userinfos;

import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.esupportail.sgc.EsupSgcTestUtilsService;
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
public class UserInfoServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	UserInfoService userInfoService;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;
	
    @Test
    public void testAdditionalsInfo4UserOfEsupSgc() {
    	testAdditionalsInfo(esupSgcTestUtilsService.getEppnFromDb());
    }
    
    @Test
    public void testAdditionalsInfo4UserOfLdap() {
    	testAdditionalsInfo(esupSgcTestUtilsService.getEppnFromLdap());
    }
    
    @Test
    public void testAdditionalsInfo4UserOfTestConfig() {
    	testAdditionalsInfo(esupSgcTestUtilsService.getEppnFromConfig());
    }
    
	protected void testAdditionalsInfo(String eppn2test) {
		Assume.assumeTrue(eppn2test != null);
	    User dummyUser = new User();
		dummyUser.setEppn(eppn2test);
		userInfoService.setAdditionalsInfo(dummyUser, null);
		String testDetails = String.format("userInfos on %s gives user %s",
				eppn2test, dummyUser);
		log.info(testDetails);
    }

    @Test
    public void testUserFieldsEquals() {
    	User user = esupSgcTestUtilsService.getUserFromDb();
    	log.info(String.format("Test User.fieldsEquals on %s", user.getEppn()));
    	Assume.assumeTrue(user!=null);
    	assertNull(user.getFieldNotEquals(user));
    }
	
}

