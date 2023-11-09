package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.EscrStudent;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.crous.CrousPatchIdentifierService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RequestMapping("/admin/userinfos")
@Controller
public class UserInfosController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	List<ExtUserInfoService> extUserInfoServices;

	@Autowired
	public void setExtUserInfoServices(List<ExtUserInfoService> extUserInfoServices) {
		this.extUserInfoServices = extUserInfoServices;
		Collections.sort(this.extUserInfoServices, (p1, p2) -> p1.getOrder().compareTo(p2.getOrder()));
	}

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "userinfos";
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String listUserInfos(Model uiModel, @RequestParam(required=false) String eppn, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String authEppn = auth.getName();
		if(eppn == null) {
			eppn = authEppn;
		}
		boolean useRequest = eppn.equals(authEppn);

		User user = new User();
		try {
			user = User.findUsersByEppnEquals(eppn).getSingleResult();
		} catch(NoResultException e) {
			log.info(String.format("No result for %s in DB", eppn));
			user.setEppn(eppn);
		}
		Map<String, String> userInfosInComputing = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		Map<String, Map<String, String>> userInfosMap = new LinkedHashMap();
		Map<String, String> beanNameFinal4UserInfo = new HashMap<>();
		Map<String, Boolean> beanNamesRegexMatch = new LinkedHashMap<>();
		Map<String, Long> durations = new HashMap<>();

		long totalTime = System.currentTimeMillis();
		for(ExtUserInfoService extUserInfoService : extUserInfoServices) {
			String beanNameLabel = extUserInfoService.getBeanName() + " #" + extUserInfoService.getOrder();
			beanNamesRegexMatch.put(beanNameLabel, eppn.matches(extUserInfoService.getEppnFilter()));
			if(beanNamesRegexMatch.get(beanNameLabel)) {
				long time = System.currentTimeMillis();
				Map<String, String> userInfos = extUserInfoService.getUserInfos(user, useRequest ? request : null, userInfosInComputing);
				durations.put(beanNameLabel, System.currentTimeMillis()-time);
				userInfosMap.put(beanNameLabel, userInfos);
				userInfosInComputing.putAll(userInfos);
				for(String key : userInfos.keySet()) {
					beanNameFinal4UserInfo.put(key, beanNameLabel);
				}
			}
		}

		uiModel.addAttribute("eppn", eppn);
		uiModel.addAttribute("userInfosMap", userInfosMap);
		uiModel.addAttribute("userInfosFinal", userInfosInComputing);
		uiModel.addAttribute("beanNameFinal4UserInfo", beanNameFinal4UserInfo);
		uiModel.addAttribute("beanNamesRegexMatch", beanNamesRegexMatch);
		uiModel.addAttribute("durations", durations);
		uiModel.addAttribute("totalDuration", System.currentTimeMillis()-totalTime);

		return "admin/userinfos";
	}

}
