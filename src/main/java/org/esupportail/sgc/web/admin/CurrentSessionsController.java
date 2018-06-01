package org.esupportail.sgc.web.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
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
		List<User> users = new ArrayList<User>();
		
		for(Object p: principals) {
			sessions.add(((UserDetails) p).getUsername());
		}
		
		Collections.sort(sessions);
		
		for(String eppn : sessions){
			users.add(User.findUser(eppn));
		}
		uiModel.addAttribute("users", users);
		
		uiModel.addAttribute("active", "sessions");
		
		return "admin/currentsessions";
	}

}
