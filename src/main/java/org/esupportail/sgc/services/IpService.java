package org.esupportail.sgc.services;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;

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

}

