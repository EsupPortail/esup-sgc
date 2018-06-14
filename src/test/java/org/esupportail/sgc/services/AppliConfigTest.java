package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.services.AppliConfigService.AppliConfigKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class AppliConfigTest {
	
	@Test
	public void testAppliConfig() {
		for(AppliConfigKey appliConfigKey : AppliConfigService.AppliConfigKey.values()) {
			AppliConfig.findAppliConfigByKey(appliConfigKey.name());
		}
	}
	
}
