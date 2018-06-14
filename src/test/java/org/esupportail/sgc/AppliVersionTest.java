package org.esupportail.sgc;

import java.util.List;

import org.esupportail.sgc.domain.AppliVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class AppliVersionTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testgetAppliVersionFromPostgresqlDb() {
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions();
		if(!appliVersions.isEmpty()) {
			AppliVersion appliVersion = appliVersions.get(0);
			log.info("esupSgcVersion : " + appliVersion.getEsupSgcVersion());
		}
	}
	
}
