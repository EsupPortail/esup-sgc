package org.esupportail.sgc.services.ac;

import org.esupportail.sgc.services.ldap.GroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
