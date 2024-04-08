package org.esupportail.sgc.services.ldap;

import org.springframework.beans.factory.BeanNameAware;

import java.util.List;

public interface GroupService extends BeanNameAware {

	List<String> getGroupsForEppn(String eppn);

	List<String> getMembers(String groupName);

	String getBeanName();

	boolean canManageGroup(String groupName);
}
