package org.esupportail.sgc;

import java.util.List;

import org.esupportail.sgc.dao.AppliVersionDaoService;
import org.esupportail.sgc.domain.AppliVersion;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.annotation.Resource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class AppliVersionTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    AppliVersionDaoService appliVersionDaoService;

	@Test
	public void testgetAppliVersionFromPostgresqlDb() {
		List<AppliVersion> appliVersions = appliVersionDaoService.findAllAppliVersions();
		if(!appliVersions.isEmpty()) {
			AppliVersion appliVersion = appliVersions.get(0);
			log.info("esupSgcVersion : " + appliVersion.getEsupSgcVersion());
		}
	}
	
}
