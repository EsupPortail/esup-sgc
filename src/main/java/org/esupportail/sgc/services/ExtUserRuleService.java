package org.esupportail.sgc.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;


@Service
public class ExtUserRuleService {
	
	@Resource
	AppliConfigService appliConfigService;
	
	public boolean isExtEsupSgcUser(String eppn) {
		String eppnRegexpExtUseMultiLines = appliConfigService.getEppnRegexpExt();
		for(String eppnRegexpExtUse : eppnRegexpExtUseMultiLines.split("\r\n")) {
			Pattern p = Pattern.compile(eppnRegexpExtUse) ;      
			Matcher m = p.matcher(eppn) ;    
			if(m.matches()) {
				return true;
			}
		} 
		return false;
	}
}
