/**
 * Licensed to EsupPortail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * EsupPortail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.sgc.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.esupportail.sgc.domain.User;
import org.springframework.stereotype.Service;

@Service("urlEncodingUtils")
public class MemoryMapStringEncodingUtils {

	Map<String, String> idsMap = new HashMap<String, String>();
	
	@PostConstruct
	void preloadStringMap() {
		for(String s : User.findDistinctAddresses(null, null)) {
			encodeString(s);
		}
	}
	
	public String encodeString(String string2encode) {
		if(string2encode==null)
			return "";
		String encPath = String.valueOf(string2encode.hashCode());
		if(!idsMap.containsKey(encPath)) {
			idsMap.put(encPath, string2encode);
		}
		return encPath;
	}
	
	public String decodeString(String string2decode) {
		if(string2decode == null || "".equals(string2decode)) {
			return "";
		}
		String path = idsMap.get(string2decode);
		return path;
	}
	
	public Map<String, String> getMapWithEncodedString(List<String> strings) {
		Map<String, String> mapWithEncodedString = new HashMap<String, String>(); 
		for(String s : strings) {
			mapWithEncodedString.put(encodeString(s), s);
		}
		return mapWithEncodedString;
	}
	
}
