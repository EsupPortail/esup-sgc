package org.esupportail.sgc.services;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.annotation.Resource;

import org.esupportail.sgc.dao.PrefsDaoService;
import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService {

    public final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	LogService logService;

    @Resource
    PrefsDaoService prefsDaoService;
	
	public void setPrefs(String eppn, String key, String value){
		
		try {
			if(prefsDaoService.countFindPrefsesByEppnEqualsAndKeyEquals(eppn, key)>0){
				Prefs pref = prefsDaoService.findPrefsesByEppnEqualsAndKeyEquals(eppn, key).getSingleResult();
				pref.setDateModification(LocalDateTime.now());
				pref.setValue(value);
                prefsDaoService.merge(pref);
			}else{
				Prefs pref = new Prefs();
				pref.setKey(key);
				pref.setValue(value);
				pref.setEppn(eppn);
				pref.setDateModification(LocalDateTime.now());
                prefsDaoService.persist(pref);
				//log... creation et update STATS DELETED
			}
			logService.log(null, ACTION.UPDATEPREFS, RETCODE.SUCCESS, key, eppn, null);
		} catch (Exception e) {
			log.warn("Erreur lors de mise à jour ou création d'une préférence, " + key);
		}
		
	}
	
	public Prefs getPrefs(String eppn, String key){
		if(prefsDaoService.countFindPrefsesByEppnEqualsAndKeyEquals(eppn, key)>0){
			return prefsDaoService.findPrefsesByEppnEqualsAndKeyEquals(eppn, key).getSingleResult();
		}else{
			return null;
		}
	}
	
	public String getPrefValue(String eppn, String key){
		Prefs prefs = this.getPrefs(eppn, key);
		String value = "";
		if(prefs == null){
			if(ManagerCardController.MANAGER_SEARCH_PREF.EDITABLE.name().equals(key)){
				value = "all";
			}else if(ManagerCardController.MANAGER_SEARCH_PREF.OWNORFREECARD.name().equals(key)){
				value = "false";
			}else if(ManagerCardController.MANAGER_SEARCH_PREF.LIST_NO_IMG.name().equals(key)){
                value = "false";
            }
		}else{
			value = prefs.getValue();
		}
		
		return value;
	}
}

