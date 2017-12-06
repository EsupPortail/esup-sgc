package org.esupportail.sgc.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.esupportail.sgc.domain.AppliConfig;
import org.springframework.stereotype.Service;


@Service
public class ExtUserRuleService {
	
	public boolean isExtEsupSgcUser(String eppn) {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("EXT_USER_EPPN_REGEXP");
		if(appliConfig != null) {
			String eppnRegexpExtUseMultiLines = appliConfig.getValue();
			for(String eppnRegexpExtUse : eppnRegexpExtUseMultiLines.split("\r\n")) {
				Pattern p = Pattern.compile(eppnRegexpExtUse) ;      
				Matcher m = p.matcher(eppn) ;    
				if(m.matches()) {
					return true;
				}
			}
		} 
		return false;
	}
}
