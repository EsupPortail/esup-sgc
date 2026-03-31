package org.esupportail.sgc.services.ac;

import org.esupportail.sgc.services.ldap.GroupService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcFamilyService {

    GroupService  groupService;

    List<String> regexpMapping = new ArrayList<>();

    Map<String, String> simpleGroupFamilyMapping = new HashMap<>();

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setRegexpMapping(List<String> regexpMapping) {
        this.regexpMapping = regexpMapping;
    }

    public void setSimpleGroupFamilyMapping(Map<String, String> simpleGroupFamilyMapping) {
        this.simpleGroupFamilyMapping = simpleGroupFamilyMapping;
    }

    public Collection<String> getFamilies(String eppn) {
        List<String>  families = new ArrayList<>();
        List<String> groups = groupService.getGroupsForEppn(eppn);
        for(String group : groups) {
            if(simpleGroupFamilyMapping.containsKey(group)) {
                families.add(simpleGroupFamilyMapping.get(group));
            }
            for(String regexp : regexpMapping) {
                Pattern pattern = Pattern.compile(regexp);
                Matcher matcher = pattern.matcher(group);
                if(matcher.find()) {
                    families.add(matcher.group(1));
                }
            }
        }
        return families;
    }
}
