package org.esupportail.sgc.services;

import java.util.Date;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService {
	
	@Resource
	LogService logService;
	
	public final Logger log = LoggerFactory.getLogger(getClass());
	
	public void setPrefs(String eppn, String key, String value){
		
		try {
			if(Prefs.countFindPrefsesByEppnEqualsAndKeyEquals(eppn, key)>0){
				Prefs pref = Prefs.findPrefsesByEppnEqualsAndKeyEquals(eppn, key).getSingleResult();
				pref.setDateModification(new Date());
				pref.setValue(value);
				pref.merge();
			}else{
				Prefs pref = new Prefs();
				pref.setKey(key);
				pref.setValue(value);
				pref.setEppn(eppn);
				pref.setDateModification(new Date());
				pref.persist();
				//log... creation et update STATS DELETED
			}
			logService.log(null, ACTION.UPDATEPREFS, RETCODE.SUCCESS, key, eppn, null);
		} catch (Exception e) {
			log.warn("Erreur lors de mise à jour ou création d'une préférence, " + key);
		}
		
	}
	
	public Prefs getPrefs(String eppn, String key){
		if(Prefs.countFindPrefsesByEppnEqualsAndKeyEquals(eppn, key)>0){
			return Prefs.findPrefsesByEppnEqualsAndKeyEquals(eppn, key).getSingleResult();
		}else{
			return null;
		}
	}
	
	public String getPrefValue(String eppn, String key){
		Prefs prefs = this.getPrefs(eppn, key);
		String value = "";
		if(prefs == null){
			if("EDITABLE".equals(key)){
				value = "all";
			}else if("OWNORFREECARD".equals(key)){
				value = "false";
			}
		}else{
			value = prefs.getValue();
		}
		
		return value;
	}
}

