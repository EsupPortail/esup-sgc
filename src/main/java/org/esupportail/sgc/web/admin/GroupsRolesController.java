package org.esupportail.sgc.web.admin;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.services.ldap.GroupService;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/admin/groupsroles")
@Controller
public class GroupsRolesController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	List<GroupService> groupServices;

	@Autowired
	LdapGroup2UserRoleService ldapGroup2UserRoleService;

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "groupsroles";
	}

	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String listGroupsRoles(Model uiModel, @RequestParam(required=false) String eppn, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String authEppn = auth.getName();
		if(eppn == null) {
			eppn = authEppn;
		}

		Map<String, List<String>> groupsMap = new HashMap();
		Map<String, Long> durations = new HashMap<>();
		for(GroupService groupService : groupServices) {
			String beanNameLabel = groupService.getBeanName();
			long time = System.currentTimeMillis();
			groupsMap.put(beanNameLabel, groupService.getGroupsForEppn(eppn));
			durations.put(beanNameLabel, System.currentTimeMillis()-time);
		}

		Map<String, Long> membersDurations = new HashMap<>();
		Map<String, Map<String, Long>> groupsMembers = new HashMap<>();
		Map<String, Long> rolesMembers = new HashMap<>();
		for(GroupService groupService : groupServices) {
			Map<String, String> mappingGroupesRoles = ldapGroup2UserRoleService.getMappingGroupesRoles();
			String beanNameLabel = groupService.getBeanName();
			groupsMembers.put(beanNameLabel, new HashMap<String, Long>());
			long time = System.currentTimeMillis();
			for(String groupName : mappingGroupesRoles.keySet()) {
				if(groupService.canManageGroup(groupName)) {
					long l = groupService.getMembers(groupName).stream().filter(m -> StringUtils.isNotEmpty(m)).count();
					groupsMembers.get(beanNameLabel).put(groupName, l);
					if ("groupService".equals(beanNameLabel)) {
						for (String role : mappingGroupesRoles.get(groupName).split(LdapGroup2UserRoleService.MULTIPLE_ROLES_DELIMITER)) {
							rolesMembers.put(role, Optional.ofNullable(rolesMembers.get(role)).orElse(0L) + l);
						}
					}
				}
			}
			membersDurations.put(groupService.getBeanName(), System.currentTimeMillis()-time);
		}

		long rolesTime = System.currentTimeMillis();
		uiModel.addAttribute("roles", ldapGroup2UserRoleService.getRoles(eppn));
		long rolesDuration = System.currentTimeMillis()-rolesTime;

		List<GrantedAuthority> rolesReachableDb = new ArrayList<>();
		try{
			rolesReachableDb =  ldapGroup2UserRoleService.getReachableRoles(eppn);
		} catch(Exception e) {
			log.debug("Exception for getReachableRoles for eppn {}", eppn);
		}

		uiModel.addAttribute("eppn", eppn);
		uiModel.addAttribute("groupsMap", groupsMap);
		uiModel.addAttribute("durations", durations);
		uiModel.addAttribute("groupsMembers", groupsMembers);
		uiModel.addAttribute("membersDurations", membersDurations);
		uiModel.addAttribute("rolesDuration", rolesDuration);
		uiModel.addAttribute("rolesReachableDb", rolesReachableDb);
		uiModel.addAttribute("rolesMembers", rolesMembers);

		return "admin/groupsroles";
	}

}
