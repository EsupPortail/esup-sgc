package org.esupportail.sgc.domain;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class AppliVersion {
	
	String esupSgcVersion;
	
	public static AppliVersion getAppliVersion() {
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions("esupSgcVersion", "desc");
		if(appliVersions.isEmpty()) {
			return null;
		}
		return appliVersions.get(0);
	}

}
