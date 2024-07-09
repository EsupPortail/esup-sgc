package org.esupportail.sgc.web.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.esupportail.sgc.domain.SgcHttpSession;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.security.SgcHttpSessionsListenerService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionInformation;
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

	@Resource
	SgcHttpSessionsListenerService sgcHttpSessionsListenerService;
	
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

		Map<String, SgcHttpSession> allSessions = sgcHttpSessionsListenerService.getSessions();
		List<String> sessions = new Vector<String>();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		Map<String, List<User>> users = new HashMap<String, List<User>>();
		List<User> allUsers = new ArrayList<User>();
		
		for(Object p: principals) {
			String eppn = ((UserDetails) p).getUsername();
			sessions.add(eppn);
			for(SessionInformation sessionInformation: sessionRegistry.getAllSessions(p, false)) {
				if(allSessions.containsKey(sessionInformation.getSessionId())) {
					allSessions.get(sessionInformation.getSessionId()).setUserEppn(eppn);
				}
			}
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
		uiModel.addAttribute("allSessions", allSessions.values());

		return "admin/currentsessions";
	}

}
