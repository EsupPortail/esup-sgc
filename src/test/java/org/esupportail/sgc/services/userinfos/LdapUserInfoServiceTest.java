package org.esupportail.sgc.services.userinfos;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.domain.User;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class LdapUserInfoServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired(required = false)
	List<LdapUserInfoService> ldapUserInfoServices;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;

    @Test
    public void testGetUserInfos() {
		Assume.assumeNotNull(ldapUserInfoServices);
		String eppn2test = esupSgcTestUtilsService.getEppnFromLdap();
		Assume.assumeTrue(eppn2test != null);
		User dummyUser = new User();
		dummyUser.setEppn(eppn2test);
		for(LdapUserInfoService ldapUserInfoService: ldapUserInfoServices) {
			Map<String, String> ldapUserInfos = ldapUserInfoService.getUserInfos(dummyUser, null, null);
			String testDetails = String.format("userInfos on %s gives user %s : %s",
					eppn2test, dummyUser, ldapUserInfos);
			log.info(testDetails);
		}
    }

}

