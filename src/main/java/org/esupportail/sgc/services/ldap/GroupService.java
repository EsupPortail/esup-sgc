package org.esupportail.sgc.services.ldap;

import java.util.List;

public interface GroupService {

	List<String> getGroupsForEppn(String eppn);

	List<String> getMembers(String groupName);

}
