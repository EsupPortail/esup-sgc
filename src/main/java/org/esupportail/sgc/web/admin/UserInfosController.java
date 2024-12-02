package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
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
import java.util.*;

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
