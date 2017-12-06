package org.esupportail.sgc.tools;

import java.util.HashMap;
import java.util.Map;


public class Params {
	
	public static Map<String,String> getPhotoParams(){
		
		Map<String, String> photoParams = new HashMap<String, String>();
		
		photoParams.put("encoding", "base64,");
		photoParams.put("extension", ".jpeg");
		photoParams.put("contentType", "image/jpeg");
		
		return photoParams;
	}

}
