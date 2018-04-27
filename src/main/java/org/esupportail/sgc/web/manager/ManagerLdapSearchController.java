package org.esupportail.sgc.web.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardActionMessageService;
import org.esupportail.sgc.services.TemplateCardService;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bitwalker.useragentutils.UserAgent;

@RequestMapping("/manager")
@Controller	
public class ManagerLdapSearchController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource 
	UserInfoService userInfoService;
	
	@Resource	
	AppliConfigService appliConfigService;
	
	@Resource 
	CardActionMessageService cardActionMessageService;
	
	@Resource
	TemplateCardService templateCardService;
	
	@Resource
	UserService userService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "manager";
	}
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpManager();
	}   
	
	@ModelAttribute("actionMessages")
	public Map<Etat, List<CardActionMessage>> getCardActionMessages() {
		return cardActionMessageService.getCardActionMessages();
	}
	
	
	@RequestMapping(value="/ldapSearch")
	public String ldapSearch(Model uiModel, @RequestParam(value="searchString", required=false) String searchString, @RequestHeader("User-Agent") String userAgent) {
		
		List<User> users = new ArrayList<User>();
		if(searchString!=null && !searchString.trim().isEmpty()) {
			users = 	userService.getSgcLdapMix(searchString);	
			Collections.sort(users, (u1, u2) -> u1.getEppn().compareTo(u2.getEppn()));
		}
		uiModel.addAttribute("ldapList", users);
		uiModel.addAttribute("searchItem", searchString);
		
		//loo from manager
		//modificateur
		return "manager/ldapSearch";

	}
	
	@RequestMapping(value="/ldapUserForm", method=RequestMethod.POST)
	public String ldapUserForm(@RequestParam(value="eppn") String eppn, HttpServletRequest request, Model uiModel, @RequestHeader("User-Agent") String userAgent) {
		
		User dummyUser = User.findUser(eppn);
		Long nbCards = Long.valueOf("0");
		if(dummyUser != null){
			nbCards = dummyUser.getNbCards();
		}	
		boolean isFromLdap = true;
		
		if(nbCards > 0){
			isFromLdap = false;
		}
		User user = new User();
		user.setEppn(eppn);
		userInfoService.setAdditionalsInfo(user, null);
		uiModel.addAttribute("user", user);

		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		
		uiModel.addAttribute("deviceType", userAgentUtils.getOperatingSystem().getDeviceType());
		uiModel.addAttribute("templateCard", templateCardService.getTemplateCard(user));
		uiModel.addAttribute("configUserMsgs", userService.getConfigMsgsUser());
		uiModel.addAttribute("isEsupSgcUser", userService.isEsupSgcUser(user));
		uiModel.addAttribute("isISmartPhone",  userService.isISmartphone(userAgent));
		Map<String, Boolean> displayFormParts = userService.displayFormParts(user, isFromLdap);
		log.debug("displayFormParts for " + eppn + " : " + displayFormParts);
		uiModel.addAttribute("displayFormParts", displayFormParts);
		uiModel.addAttribute("fromLdap", isFromLdap);
		uiModel.addAttribute("eppn", eppn);
		
		return "user/card-request";

	}
	
}

