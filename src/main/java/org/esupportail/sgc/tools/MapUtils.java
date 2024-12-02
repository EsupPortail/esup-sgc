package org.esupportail.sgc.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {
	
	
    public static Map<String, String> sortByValue(Map<String, String> unsortMap) {
        List<Map.Entry<String, String>> list = new ArrayList<>(unsortMap.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
