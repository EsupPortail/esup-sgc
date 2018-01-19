package org.esupportail.sgc.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	IpService ipService;
	
	public LinkedHashMap<String, String> getPopulationCrous(){
		
		LinkedHashMap<String, String> populationCrous = new LinkedHashMap<String, String> ();
		populationCrous.put("ctr", "Contractuel");
		populationCrous.put("etd", "Etudiant");
		populationCrous.put("fct", "Formation continue");
		populationCrous.put("fpa", "Formation par apprentissage");
		populationCrous.put("hbg", "Hébergé");
		populationCrous.put("psg", "Passager");
		populationCrous.put("prs", "Personnel");
		populationCrous.put("po", "Personnel ouvrier");
		populationCrous.put("stg", "Stagiaire");
		
		return populationCrous;
	}

	
    public LinkedHashMap<String, Object> mapField(List<Object> listes, int level){
    	
    	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    	
    	LinkedHashMap<String, Object> secondMap = new LinkedHashMap<String, Object>();
    	
    	String test = null;
    	int i = 1;
    	
   		for (Object result : listes) {
   			Object[] r = (Object[]) result;	
   			if (level == 2){
   				String r0 = "Aucune donnée";
   				if(r[0]!=null){
   					r0 = r[0].toString();
   				}
	   		    map.put(r0,r[1]);
   			}else{
   				//Hack '_' pour ne pas changer pas l'ordre de la requête dans le navigateur
   				if(test== null || test.equals("_" + r[0].toString())){
   					secondMap.put(r[1].toString(),r[2]);
   				}else{
   					map.put(test, secondMap);
   					secondMap = new LinkedHashMap<String, Object>();
   					secondMap.put(r[1].toString(),r[2]);
   				}
   			//Hack '_' pour ne pas changer pas l'ordre de la requête dans le navigateur
   				test = "_" +  r[0].toString();
   				if(i ==  listes.size()){
   					map.put(test, secondMap);
   				}
   				i++;
   			}
   		}
        return map;
    }
    
    @SuppressWarnings("serial")
	public  LinkedHashMap<String,Object> getStats(String typeInd) throws ParseException {
			
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>() {
			   
			   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			   Date date = formatter.parse(getCurrentAnneUniv());
			
	        {
	        	put("cardsByYearEtat",mapField(Card.countNbCardsByYearEtat(typeInd), 3));
	        	put("crous",mapField(User.countNbCrous(typeInd), 2));
	        	put("difPhoto",mapField(User.countNbDifPhoto(typeInd), 2));
	        	put("cardsByDay",mapField(Card.countNbCardsByDay(typeInd, "request_date"), 2));
	        	put("paybox",mapField(PayboxTransactionLog.countNbPayboxByYearEtat(), 3));
	        	put("motifs",mapField(Card.countNbCardsByMotifsDisable(typeInd), 2));
	        	put("dates",mapField(Card.countNbCardsByMonthYear(typeInd), 3));
	        	put("deliveredCardsByDay",mapField(Card.countNbDeliverdCardsByDay(typeInd), 2));
	        	put("encodedCardsByday",mapField(Card.countNbEncodedCardsByDay(typeInd), 2));
	        	put("nbCards",mapField(User.countNbCardsByuser(typeInd), 2));
	        	put("editable",mapField(User.countNbEditable(), 2));
	        	put("verso5",mapField(User.countNbVerso5(), 2));
	        	put("browsers",mapField(Card.countBrowserStats(typeInd), 2));
	        	put("os",mapField(Card.countOsStats(typeInd), 2));
	        	put("nbRejets",mapField(Card.countNbCardsByRejets(typeInd, date), 2));
	        	put("notDelivered",mapField(Card.countNbEditedCardNotDelivered(date), 2));
	        	put("cardsMajByDay",mapField(Log.countNbLogByDay("MAJVERSO", ipService.setCasesRequest("remote_address"), ipService.getBannedIp()), 3));
	        	put("cardsMajByIp",mapField(Log.countNbLogByAction("MAJVERSO", ipService.setCasesRequest("remote_address"), ipService.getBannedIp()), 2));
	        	put("cardsMajByDay2",mapField(Log.countNbLogByDay2("MAJVERSO", ipService.setCasesRequest("remote_address"), ipService.getBannedIp()), 3));
	        	put("deliveryByAdress",mapField(Card.countDeliveryByAddress(date).getResultList(),2));
	        	put("userDeliveries",mapField(Log.countUserDeliveries(),2));
	        	put("tarifsCrous",mapField(User.countTarifCrousByType(),3));
	        	put("cardsByMonth",mapField(Card.countNbCardRequestByMonth(typeInd, date), 2));
	        	put("encodedCardsByMonth",mapField(Card.countNbCardEncodedByMonth(typeInd, date), 2));
	        	put("nbRejetsByMonth",mapField(Card.countNbRejetsByMonth(typeInd), 2));
	        	put("requestFree",mapField(User.countNbRequestFree(),3));
	        }
	    };
		return results;
    }
    
    public LinkedHashMap<String, String> getStatsCardsByPopulationCrous (List<Object[]> objectList){
    	
    	LinkedHashMap<String, String> mapCards = new LinkedHashMap<String, String>();
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	if(!objectList.isEmpty()){
    		
    		for(Object[] r : objectList){
    			mapCards.put(r[0].toString(), r[1].toString());
    		}
    		
    		LinkedHashMap<String, String> mapCrous =  this.getPopulationCrous();
    		
        	
    		for(Map.Entry<String, String> entry : mapCrous.entrySet()){
    			 if(mapCards.containsKey(entry.getKey())){
    				 finalMap.put(entry.getKey(), mapCards.get(entry.getKey()));
    			 }else{
    				 finalMap.put(entry.getKey(), "0");
    			 }
    		}
    		
    	}
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getYesterdayCardsByPopulationCrous (String typeDate, boolean maj){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	if(maj){
    		finalMap = getStatsCardsByPopulationCrous (User.countYesterdayMajCardsByPopulationCrous(this.getDates().get("isMonday")));
    	}else{
    	  	finalMap = getStatsCardsByPopulationCrous (User.countYesterdayCardsByPopulationCrous(this.getDates().get("isMonday"),typeDate));
    	}
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getMonthCardsByPopulationCrous (String typeDate, boolean maj){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	if(maj){
    		finalMap = getStatsCardsByPopulationCrous (User.countMonthMajCardsByPopulationCrous(this.getDates().get("likeMonth")));
    	}else{
    		finalMap = getStatsCardsByPopulationCrous (User.countMonthCardsByPopulationCrous(this.getDates().get("likeMonth"),typeDate));
    	}
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getYearEnabledCardsByPopulationCrous (String typeDate, boolean maj){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	if(maj){
    		finalMap = getStatsCardsByPopulationCrous (User.countYearMajEnabledCardsByPopulationCrous( StringToDate("yyyy-mm-dd", this.getDates().get("year"))));
    	}else{
    		finalMap = getStatsCardsByPopulationCrous (User.countYearEnabledCardsByPopulationCrous(this.getDates().get("year"),typeDate));
    	}
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getDates(){
    	
    	LinkedHashMap<String, String> mapDates = new LinkedHashMap<String, String>();
    	
    	DateFormat dateFormat = new SimpleDateFormat("EEEEEEE dd MMMM yyyy", Locale.FRENCH);
    	DateFormat dateFormat1 = new SimpleDateFormat("/MM/yyyy", Locale.FRENCH);
    	DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-", Locale.FRENCH);
        Calendar cal = Calendar.getInstance();
        
        String isMonday = "false";
        int day = cal.get(Calendar.DAY_OF_WEEK);
        if(day==2){
        	isMonday="true";
        	 cal.add(Calendar.DATE, -3);
        }else{
        	 cal.add(Calendar.DATE, -1);
        }
        String yesterday = dateFormat.format(cal.getTime());
        String month = "01".concat(dateFormat1.format(cal.getTime()));
        String likeMonth = dateFormat2.format(cal.getTime()).concat("%");
        String splitYear []= getCurrentAnneUniv().split("-");
        String formatYear = splitYear[2].concat("-").concat(splitYear[1]).concat("-").concat(splitYear[0]);
    	
        mapDates.put("isMonday", isMonday);
    	mapDates.put("yesterday", yesterday);
    	mapDates.put("month", month);
    	mapDates.put("likeMonth", likeMonth);
    	mapDates.put("year", getCurrentAnneUniv());
    	mapDates.put("formatYear", formatYear);
    	
    	return mapDates;
    }
    
	public String getCurrentAnneUniv(){
		//Vrai date univ sinon par défaut 1er juillet de l'année courante ou passée
		if(appliConfigService.getCurrentAnneeUniv()!=null){
			return appliConfigService.getCurrentAnneeUniv();
		}else{
			int year = Calendar.getInstance().get(Calendar.YEAR);
			int month = Calendar.getInstance().get(Calendar.MONTH);
			if(month <7){
				year = year-1;
			}
			return String.valueOf(year).concat("-07-01");
		}
	}
	
	public Date StringToDate(String pattern, String dateInString){
	    SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = null;

        try {
        	date = formatter.parse(dateInString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return date;
	}
    
}
