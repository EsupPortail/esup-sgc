package org.esupportail.sgc.services.ldap;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class GroupServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public final static String ROLE_TO_TEST = "ROLE_ADMIN";

	@Resource
	GroupService groupService;
	
	@Resource
	Map<String, String> sgcMappingGroupesRoles;
	
	@Test
	public void testConsistenceGroup() {
		
		for(String groupName : sgcMappingGroupesRoles.keySet()) {
			if(ROLE_TO_TEST.equals(sgcMappingGroupesRoles.get(groupName))) {
				List<String> members = groupService.getMembers(groupName);
				if(members.size()>0) {
					String member = members.get(0);
					int index = 1;
					while(StringUtils.isBlank(member) && members.size()>index) {
						member = members.get(index);
						index++;
					}
					List<String> groups = groupService.getGroupsForEppn(member);
					String testDetails = String.format("members of %s : %s \n"
							+ "groups of %s : %s",
							groupName, members, member, groups);
					if(!groups.contains(groupName)) {
						fail("groupService configurations inconsistent :\n"
								+ testDetails);
					} else {
						log.info("Test OK : " + testDetails);
					}
				}
			}
		}
	}

}
