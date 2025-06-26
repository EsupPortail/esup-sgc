package org.esupportail.sgc.web.manager;

import java.text.ParseException;
import java.util.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.PrefsDaoService;
import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.PreferencesService;
import org.esupportail.sgc.services.StatsService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bitwalker.useragentutils.UserAgent;
import flexjson.JSONSerializer;


@RequestMapping("/manager/stats")
@Controller
public class StatsController {
	
	private static final Logger log = LoggerFactory.getLogger(StatsController.class);
	
	private static final String KEY = "STATS";
	private static final String KEYRM = "STATSRM";
	
	@Resource
	StatsService statsService;	
	
	@Resource
	UserInfoService userInfoService;
	
	@Resource
	AppliConfigService appliConfigService;	
	
	@Resource
	PreferencesService preferencesService;

    @Resource
    PrefsDaoService prefsDaoService;

    @Resource
    ManagerCardController managerCardController;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "stats";
	}
	
	@ModelAttribute("types")
	public List<String> getTypes() {
		return userInfoService.getListExistingType();
	}
	
	@ModelAttribute("livraison")
	public String getLivraisonConfig() {
		return appliConfigService.getModeLivraison();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@RequestMapping
	public String index(HttpServletRequest httpServletRequest, Model uiModel, @RequestParam(value = "type", required = false) String type, @RequestHeader("User-Agent") String userAgent) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
    	if(uiModel.containsAttribute("type")){
        	Map<String, ?> flashInputMap = RequestContextUtils.getInputFlashMap(httpServletRequest);
        	if(flashInputMap.containsKey("type")){
        		type = (String) flashInputMap.get("type");
        	}	
    	}
    	if(type==null){
    		type="";
    	}
		uiModel.addAttribute("selectedType", type);
		ObjectMapper mapper = new ObjectMapper();
		String jsonStats = "";
		String jsonStatsRm = "";
		List<String> prefsStats = new ArrayList<>();
		List<String> prefsStatsRm = new ArrayList<>();
		try {
			if(preferencesService.getPrefs(eppn, KEY)!=null){
				prefsStats = Arrays.asList(preferencesService.getPrefs(eppn, KEY).getValue().split("\\s*,\\s*"));
			}
			jsonStats = mapper.writeValueAsString(prefsStats);
			if(preferencesService.getPrefs(eppn, KEYRM)!=null){
				prefsStatsRm = Arrays.asList(preferencesService.getPrefs(eppn, KEYRM).getValue().split("\\s*,\\s*"));
			}
			jsonStatsRm = mapper.writeValueAsString(prefsStatsRm);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		uiModel.addAttribute("prefs",jsonStats);
		uiModel.addAttribute("userAgent", userAgentUtils.getOperatingSystem().getDeviceType());
		uiModel.addAttribute("prefsRm",jsonStatsRm);
		uiModel.addAttribute("prefsRmList",prefsStatsRm);
		uiModel.addAttribute("annees",statsService.getAnneeUnivs());


        uiModel.addAttribute("userPrefs", managerCardController.getuserPrefs());
        return "templates/manager/stats";
	}
	
	@RequestMapping(value = "/tabs/{type}", produces = "text/html")
    public String redirectTab(@PathVariable("type") String type, final RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest){
	  
		redirectAttributes.addFlashAttribute("type", type);
	  
	    return "redirect:/manager/stats";
    }
	
	@RequestMapping(value="json", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody 
	public String getStats( @RequestParam(value="typeInd")  String typeInd, @RequestParam(value="type") String type) {
		String flexJsonString = "Aucune statistique à récupérer";
		try {
			JSONSerializer serializer = new JSONSerializer();
			flexJsonString = serializer.deepSerialize(statsService.getStats(typeInd, type));
			
		} catch (Exception e) {
			log.warn("Impossible de récupérer les statistiques " + type , e);
		}
		
    	return flexJsonString;
	}
	
	@RequestMapping(value="/table", produces = "text/html")
	public String getTableStats(Model uiModel, HttpServletRequest request) throws ParseException {
		uiModel.addAttribute("dates", statsService.getDates());
		uiModel.addAttribute("populationCrous", statsService.getPopulationCrous());
        Map<String, String> yesterdayCards = statsService.getYesterdayCardsByPopulationCrous("encoded_date");
        Map<String, String> monthCards = statsService.getMonthCardsByPopulationCrous("encoded_date");
        Map<String, String> yearCards = statsService.getYearEnabledCardsByPopulationCrous("request_date", statsService.getDates().get("year"));
        Map<String, LinkedHashMap<String, String>> allYearCards = statsService.getAllPastYearEnabledCardsByPopulationCrous("request_date");
		uiModel.addAttribute("yesterdayCards", yesterdayCards);
        uiModel.addAttribute("yesterdayCardsTotal", yesterdayCards.values().stream().mapToInt(Integer::parseInt).sum());
		uiModel.addAttribute("monthCards", monthCards);
        uiModel.addAttribute("monthCardsTotal", monthCards.values().stream().mapToInt(Integer::parseInt).sum());
		uiModel.addAttribute("yearCards", yearCards);
        uiModel.addAttribute("yearCardsTotal", yearCards.values().stream().mapToInt(Integer::parseInt).sum());
		uiModel.addAttribute("allYearCards", allYearCards);
        // allYearCardsTotal: sums
        List<Long> allYearCardsTotal = new ArrayList<>();
        for(String year : allYearCards.keySet()) {
            allYearCardsTotal.add(allYearCards.get(year).values().stream()
                    .mapToLong(Long::parseLong).sum());
        }
        uiModel.addAttribute("allYearCardsTotal", allYearCardsTotal);
        return "templates/manager/stats-table";
	}
	
	@RequestMapping(value="/prefs", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody
	public void savePrefs(@RequestParam List<String> values, @RequestParam String key) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
		try {
			preferencesService.setPrefs(eppn, key, StringUtils.join(values, ","));
			if(KEYRM.equals(key)){
				List <String>  prefsStats = new ArrayList<String>(Arrays.asList(preferencesService.getPrefs(eppn, KEY).getValue().split("\\s*,\\s*")));
				prefsStats.remove(values.get(0));
			}
			
		} catch (Exception e) {
			log.warn("Impossible de sauvegarder les préférences", e);
		}
	}
	
	@RequestMapping(value="/savePrefs")
	public String  savePrefs(@RequestParam(value="prefsRm", required=false) List<String> prefsRm,  Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		uiModel.asMap().clear();
		try {
			if(prefsRm!=null){
				List<String> prefsStatsRm = new ArrayList<String>(Arrays.asList(preferencesService.getPrefs(eppn, KEYRM).getValue().split("\\s*,\\s*")));
				List<String> prefsStats = new ArrayList<String>(Arrays.asList(preferencesService.getPrefs(eppn, KEY).getValue().split("\\s*,\\s*")));
				for(String pref :prefsRm){
					prefsStatsRm.remove(pref);
					prefsStats.add(pref);
				}
				preferencesService.setPrefs(eppn, KEYRM, StringUtils.join(prefsStatsRm.toArray(), ","));
				preferencesService.setPrefs(eppn, KEY, StringUtils.join(prefsStats.toArray(), ","));
				if( prefsDaoService.findPrefsesByEppnEqualsAndKeyEquals(eppn, KEYRM).getSingleResult().getValue().isEmpty()){
					Prefs pref = prefsDaoService.findPrefsesByEppnEqualsAndKeyEquals(eppn, KEYRM).getSingleResult();
                    prefsDaoService.remove(pref);
				}
			}
		} catch (Exception e) {
			log.warn("Impossible de sauvegarder les préférences", e);
		}

		return "redirect:/manager/stats";
	}
}
