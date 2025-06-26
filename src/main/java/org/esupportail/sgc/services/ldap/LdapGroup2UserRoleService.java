package org.esupportail.sgc.services.ldap;

import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LdapGroup2UserRoleService {
	
	public static String MULTIPLE_ROLES_DELIMITER = ";";
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public final static String ROLE_USER_NO_EDITABLE = "ROLE_USER_NO_EDITABLE";
	
	public final static String ROLE_USER_RENEWAL_PAYED = "ROLE_USER_RENEWAL_PAYED";
	
	protected GroupService groupService;

	protected Map<String, String> mappingGroupesRoles;
	
	@Resource
	LdapGroup2OneUserRoleService ldapGroup2OneUserRoleService;

    @Resource
    UserDaoService userDaoService;
	
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}

	public Map<String, String> getMappingGroupesRoles() {
		return mappingGroupesRoles;
	}

	public Set<String> getRoles(List<String> groups) {
		Set<String> roles = new HashSet<String>();
		for(String groupName : groups) {
			if(mappingGroupesRoles.containsKey(groupName)) {
				for(String role : mappingGroupesRoles.get(groupName).split(MULTIPLE_ROLES_DELIMITER)) {
					roles.add(role);
				}
			}
		}
		return roles;
	}
	
	public Set<String> getRoles(String eppn) {
		Set<String> roles = new HashSet<String>();
		List<String> groups = getGroupsForEppn(eppn);
		log.trace(String.format("Groups for %s : %s", eppn, groups));
		roles.addAll(getRoles(groups));
		log.trace(String.format("Roles for %s : %s", eppn, roles));
		return roles;
	}

	public List<String> getGroupsForEppn(String eppn) {
		return groupService.getGroupsForEppn(eppn);
	}

	@Transactional
	public List<GrantedAuthority> getReachableRoles(String eppn) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		User user = userDaoService.findUser(eppn);
		for(String role : user.getReachableRoles()) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		return authorities;
	}

	@Transactional
	public void syncUser(String eppn) {
		User user = userDaoService.findUser(eppn);
		Set<String> rolesTarget = getRoles(eppn);
		Set<String> roles2Add = new HashSet<String>(rolesTarget);
		roles2Add.removeAll(user.getRoles());
		Set<String> roles2Remove = new HashSet<String>(user.getRoles());
		roles2Remove.removeAll(rolesTarget);
		for(String role : roles2Add) {
			ldapGroup2OneUserRoleService.addRole(eppn, role);
		}
		for(String role : roles2Remove) {
			ldapGroup2OneUserRoleService.removeRole(eppn, role);
		}
	}
	
	public void syncAllGroupsOnDb() {
		long membersAdded = 0;
		long membersRemoved = 0;
		StopWatch stopWatch = new PrettyStopWatch();
		Set<String> allEppnUsers = new HashSet<String>(userDaoService.findAllEppns());
		Map<String, List<String>> groups4RoleMap = new HashMap<String, List<String>>();
		
		for(String groupName : mappingGroupesRoles.keySet()) {
			for(String role : mappingGroupesRoles.get(groupName).split(MULTIPLE_ROLES_DELIMITER)) {
				if(!groups4RoleMap.containsKey(role)) {
					groups4RoleMap.put(role, new ArrayList<String>());
				}
				groups4RoleMap.get(role).add(groupName);
			}
		}
		
		for(String role : groups4RoleMap.keySet()) {
			
				stopWatch.start("sync " + role);
				
				Set<String> eppnUsersWithRole = new HashSet<String>(userDaoService.findAllEppnsWithRole(role));
				
				Set<String> ldapGroupMembers = new HashSet<String>();
				for(String groupName : groups4RoleMap.get(role)) {
					ldapGroupMembers.addAll(new HashSet<String>(groupService.getMembers(groupName)));
				}
				

				stopWatch.start("copy " + role);
				Set<String> eppnMembersUsers2Add = new HashSet<String>(ldapGroupMembers);

				stopWatch.start("removeAll " + role);
				eppnMembersUsers2Add.removeAll(eppnUsersWithRole);

				stopWatch.start("retainAll " + role);
				eppnMembersUsers2Add.retainAll(allEppnUsers);

				stopWatch.start("add sync " + role);
				for(String eppn : eppnMembersUsers2Add) {
					if(ldapGroup2OneUserRoleService.addRole(eppn, role)) {
						membersAdded++;
					} 
				}

				stopWatch.start("remove sync " + role);
				Set<String> eppnMembersUsers2Remove = new HashSet<String>(allEppnUsers);	
				eppnMembersUsers2Remove.removeAll(ldapGroupMembers);
				eppnMembersUsers2Remove.retainAll(eppnUsersWithRole);
				for(String eppn : eppnMembersUsers2Remove) {
					if(ldapGroup2OneUserRoleService.removeRole(eppn, role)) {
						membersRemoved++;
					}
				}
				stopWatch.stop();
				
		}
		log.debug("Total execution time to sync ldap groups on DB " + stopWatch.getTotalTimeMillis()/1000.0 + "sec");
		log.trace(stopWatch.prettyPrint());
		if(membersRemoved!=0 || membersAdded!=0) {
			log.info("Sync groups : " + membersAdded + " members added and " + membersRemoved + " members removed") ;
		}
	}
	
}
