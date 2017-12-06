package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class IpService {
	
	@Resource
	AppliConfigService appliConfigService;

	 Map<String, String> maps = new HashMap<String, String>();


	  public Map<String, String> getMaps() {
	    return maps;
	  }

	  public void setMaps(Map<String, String> maps) {
	    this.maps = maps;
	  }
	  
	  public List<String> getBannedIp(){
		  
		  List<String> bannedIp = new ArrayList<String>();
		  
		  if(appliConfigService.getBannedIpStats() != null){
			  String [] split = StringUtils.split(appliConfigService.getBannedIpStats(),",");
			  
			  bannedIp = Arrays.asList(split);
		  }
		  
		  
		  return bannedIp;
	  }
	  
	  public String setCasesRequest(String testField){
		  
		  String requestCases = "CASE ";
		  
		  for (Map.Entry<String, String> entry : this.maps.entrySet()) {
			  requestCases += " WHEN " + testField + " LIKE '" +  entry.getKey() + "' THEN '" + entry.getValue() + "' ";
			}
		  requestCases += " ELSE " + testField + " END AS " + testField + " ";
		  return requestCases;
		  
	  }
}

