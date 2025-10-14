package org.esupportail.sgc.services.userinfos;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.domain.User;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class LdapUserInfoServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired(required = false)
	List<LdapUserInfoService> ldapUserInfoServices;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;

    @Test
    public void testGetUserInfos() {
		assumeTrue(ldapUserInfoServices != null, "no ldapUserInfoService defined");
		String eppn2test = esupSgcTestUtilsService.getEppnFromLdap();
		assumeTrue(eppn2test != null);
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

