package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
import org.esupportail.sgc.web.wsrest.WsRestEsupNfcController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequestMapping("/admin/locations")
@Controller
public class LocationsController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	final WsRestEsupNfcController wsRestEsupNfcController;

    public LocationsController(WsRestEsupNfcController wsRestEsupNfcController) {
        this.wsRestEsupNfcController = wsRestEsupNfcController;
    }


    @ModelAttribute("active")
	public String getActiveMenu() {
		return "locations";
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String listLocations(Model uiModel, @RequestParam(required=false) String eppn) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String authEppn = auth.getName();
		if(eppn == null) {
			eppn = authEppn;
		}
		Map<String, List<String>> locationsMap = new LinkedHashMap<>();
		locationsMap.put("locations", wsRestEsupNfcController.getLocations(eppn));
		locationsMap.put("locationsDestroy", wsRestEsupNfcController.getLocationsDestroy(eppn));
		locationsMap.put("locationsLivreur", wsRestEsupNfcController.getLocationsLivreur(eppn));
		locationsMap.put("locationsSearch", wsRestEsupNfcController.getLocationsSearch(eppn));
		locationsMap.put("locationsUpdater", wsRestEsupNfcController.getLocationsUpdater(eppn));
		locationsMap.put("locationsVerso", wsRestEsupNfcController.getLocationsVerso(eppn));
		locationsMap.put("locationsSecondaryId", wsRestEsupNfcController.getLocationsSecondaryId(eppn));
		locationsMap.put("locationsDeuinfo", wsRestEsupNfcController.getLocationsDeuinfo(eppn));

		uiModel.addAttribute("eppn", eppn);
		uiModel.addAttribute("locationsMap", locationsMap);

		return "admin/locations";
	}

}
