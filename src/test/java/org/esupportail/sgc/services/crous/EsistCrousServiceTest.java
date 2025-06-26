package org.esupportail.sgc.services.crous;

import java.util.List;

import jakarta.annotation.Resource;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.User;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class EsistCrousServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	EsistCrousService esistCrousService;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;

    @Resource
    UserDaoService userDaoService;
	
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfEsupSgc() {
    	String eppn = esupSgcTestUtilsService.getEppnFromDb();
    	assumeTrue(eppn != null);
	    User user = userDaoService.findUser(eppn);
	    assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
    
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfLdap() {
    	String eppn = esupSgcTestUtilsService.getEppnFromLdap();
    	assumeTrue(eppn != null);
	    User user = userDaoService.findUser(eppn);
	    assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
    
    @Test
    public void testComputeIdCompagnyRateAndIdRate4UserOfTestConfig() {
    	String eppn = esupSgcTestUtilsService.getEppnFromConfig();
    	assumeTrue(eppn != null);
	    User user = userDaoService.findUser(eppn);
	    assumeTrue(user != null);
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		log.info(String.format("idCompagnyRateAndIdRate for %s : %s", user.getEppn(), idCompagnyRateAndIdRate));
    }
	
}


