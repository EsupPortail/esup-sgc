package org.esupportail.sgc.services.crous;

import java.util.List;

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
public class EsistCrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	EsistCrousService esistCrousService;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;
	
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfEsupSgc() {
    	String eppn = esupSgcTestUtilsService.getEppnFromDb();
    	Assume.assumeTrue(eppn != null);
	    User user = User.findUser(eppn);
	    Assume.assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
    
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfLdap() {
    	String eppn = esupSgcTestUtilsService.getEppnFromLdap();
    	Assume.assumeTrue(eppn != null);
	    User user = User.findUser(eppn);
	    Assume.assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
    
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfTestConfig() {
    	String eppn = esupSgcTestUtilsService.getEppnFromConfig();
    	Assume.assumeTrue(eppn != null);
	    User user = User.findUser(eppn);
	    Assume.assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
	
}


