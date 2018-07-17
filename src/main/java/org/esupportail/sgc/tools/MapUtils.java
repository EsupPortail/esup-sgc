package org.esupportail.sgc.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class MapUtils {
	
	
    public static Map<String, String> sortByValue(Map<String, String> unsortMap, boolean forJson) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, String>> list =
                new LinkedList<Map.Entry<String, String>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        String prefix = "";
        if(forJson){
        	prefix = "_";
        }
        for (Map.Entry<String, String> entry : list) {
            sortedMap.put(prefix + entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
