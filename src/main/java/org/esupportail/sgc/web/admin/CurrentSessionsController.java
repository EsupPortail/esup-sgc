package org.esupportail.sgc.web.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/currentsessions")
@Controller
public class CurrentSessionsController {
	
	@Resource
	AppliConfigService appliConfigService;

	@Resource
	UserInfoService userInfoService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "sessions";
	}
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  

	@Autowired
	@Qualifier("sessionRegistry")
	private SessionRegistry sessionRegistry;
	
	@RequestMapping
	public String getCurrentSessions(Model uiModel) throws IOException {

		
		
		List<String> sessions = new Vector<String>();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		Map<String, List<User>> users = new HashMap<String, List<User>>();
		List<User> allUsers = new ArrayList<User>();
		
		for(Object p: principals) {
			sessions.add(((UserDetails) p).getUsername());
		}
		
		Collections.sort(sessions);
		
		for(String eppn : sessions){
			User user = User.findUser(eppn);
			if(!users.containsKey(user.getUserType())) {
				users.put(user.getUserType(), new ArrayList<User>());
			}
			users.get(user.getUserType()).add(user);
			allUsers.add(user);
		}

		uiModel.addAttribute("users", users);
		uiModel.addAttribute("active", "sessions");
		uiModel.addAttribute("allUsers", allUsers);
		uiModel.addAttribute("userTypes", userInfoService.getListExistingType());

		return "admin/currentsessions";
	}

}
