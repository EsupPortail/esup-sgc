package org.esupportail.sgc.services.userinfos;

import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.services.ldap.LdapPersonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class UserInfoServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	UserInfoService userInfoService;
	
	@Resource
	LdapPersonService ldapPersonService;
	
	@Value("${test.userinfo.eppn2test:}") 
	String eppn2testFromConfig; 
	
    @Test
    public void testAdditionalsInfo4UserOfEsupSgc() {
    	testAdditionalsInfo(getEppnFromDb());
    }
    
    @Test
    public void testAdditionalsInfo4UserOfLdap() {
    	testAdditionalsInfo(getEppnFromLdap());
    }
    
    @Test
    public void testAdditionalsInfo4UserOfTestConfig() {
    	if(!eppn2testFromConfig.isEmpty()) {
    		testAdditionalsInfo(eppn2testFromConfig);
    	}
    }
    
	protected void testAdditionalsInfo(String eppn2test) {
    	if(eppn2test != null) {
	    	User dummyUser = new User();
			dummyUser.setEppn(eppn2test);
			userInfoService.setAdditionalsInfo(dummyUser, null);
			String testDetails = String.format("userInfos on %s gives user %s",
					eppn2test, dummyUser);
			log.info(testDetails);
		}
    }

	protected String getEppnFromDb() {
		String eppn2test = null;
		List<String> eppns = User.findAllEppns();
		if(!eppns.isEmpty()) {
			eppn2test = eppns.get(0);
		}
		return eppn2test;
	}
    
    protected String getEppnFromLdap() {
    	String eppn2test = null;
		List<PersonLdap> personsLdap = ldapPersonService.searchByCommonName("A", null);
		if(!personsLdap.isEmpty()) {
			eppn2test = personsLdap.get(0).getEduPersonPrincipalName();
		}
		return eppn2test;
	}

}

