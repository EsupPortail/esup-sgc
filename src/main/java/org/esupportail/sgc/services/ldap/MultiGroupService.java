package org.esupportail.sgc.services.ldap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiGroupService implements GroupService {
	
	private List<GroupService> groupServices;

	String beanName;

	@Override
	public String getBeanName() {
		return beanName;
	}

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setGroupServices(List<GroupService> groupServices) {
		this.groupServices = groupServices;
	}

	@Override
	public List<String> getGroupsForEppn(String eppn) {	
		Set<String> groups = new HashSet<String>();	
		for(GroupService groupService: groupServices) {
			groups.addAll(groupService.getGroupsForEppn(eppn));
		}	
		return new ArrayList<String>(groups);
		
	}

	@Override
	public List<String> getMembers(String groupName) {
		Set<String> members = new HashSet<String>();
		for(GroupService groupService: groupServices) {
			members.addAll(groupService.getMembers(groupName));
		}	
		return new ArrayList<String>(members);
	}
	
}

