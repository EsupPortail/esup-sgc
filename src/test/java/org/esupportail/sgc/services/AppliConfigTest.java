package org.esupportail.sgc.services;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.AppliConfigDaoService;
import org.esupportail.sgc.services.AppliConfigService.AppliConfigKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class AppliConfigTest {

    @Resource
    AppliConfigDaoService appliConfigDaoService;

	@Test
	public void testAppliConfig() {
		for(AppliConfigKey appliConfigKey : AppliConfigService.AppliConfigKey.values()) {
            appliConfigDaoService.findAppliConfigByKey(appliConfigKey.name());
		}
	}
	
}
