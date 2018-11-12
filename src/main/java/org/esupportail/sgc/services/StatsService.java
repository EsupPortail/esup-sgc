package org.esupportail.sgc.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


@Service
public class StatsService {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	IpService ipService;
	
	@Autowired
    MessageSource messageSource;
	
	@Resource 
	UserInfoService userInfoService;
	
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
	public  LinkedHashMap<String,Object> getStats(String typeInd, String typeStats) throws ParseException {
			
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>() {
			   
			   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			   LinkedHashMap<String,String>  anneeUnivs = getAnneeUnivs();
	        {
	        	if("cardsByYearEtat".equals(typeStats)){
	        		put("cardsByYearEtat",mapField(Card.countNbCardsByYearEtat(typeInd, mapToCase("etat", mapsFromI18n("etats", Locale.FRENCH, "card.label"), "etat")), 3));
	        	}else if("crous".equals(typeStats)){
	        		put("crous",mapField(User.countNbCrous(typeInd), 2));
	        	}else if("difPhoto".equals(typeStats)){
	        		put("difPhoto",mapField(User.countNbDifPhoto(typeInd), 2));
	        	}else if("cardsByDay".equals(typeStats)){
	        		put("cardsByDay",mapField(Card.countNbCardsByDay(typeInd, "request_date"), 2));
	        	}else if("paybox".equals(typeStats)){
	        		put("paybox",mapField(PayboxTransactionLog.countNbPayboxByYearEtat(), 3));
	        	}else if("motifs".equals(typeStats)){
	        		put("motifs",mapField(Card.countNbCardsByMotifsDisable(typeInd, mapToCase("motif_disable", mapsFromI18n("motifs", Locale.FRENCH, "card.label"), "motif_disable")), 2));
	        	}else if("dates".equals(typeStats)){
	        		put("dates",mapField(Card.countNbCardsByMonthYear(typeInd), 3));
	        	}else if("deliveredCardsByDay".equals(typeStats)){
	        		put("deliveredCardsByDay",mapField(Card.countNbDeliverdCardsByDay(typeInd), 2));
	        	}else if("encodedCardsByday".equals(typeStats)){
	        		put("encodedCardsByday",mapField(Card.countNbEncodedCardsByDay(typeInd), 2));
	        	}else if("nbCards".equals(typeStats)){
	        		put("nbCards",mapField(User.countNbCardsByuser(typeInd), 2));
	        	}else if("editable".equals(typeStats)){
	        		put("editable",mapField(User.countNbEditable(), 2));
	        	}else if("browsers".equals(typeStats)){
	        		put("browsers",mapField(Card.countBrowserStats(typeInd), 2));
	        	}else if("os".equals(typeStats)){
	        		put("os",mapField(Card.countOsStats(typeInd), 2));
	        	}else if("nbRejets".equals(typeStats)){
	        		put("nbRejets",mapField(Card.countNbCardsByRejets(typeInd), 2));
	        	}else if("notDelivered".equals(typeStats)){
	        		put("notDelivered",mapField(Card.countNbEditedCardNotDelivered(mapToCase("user_type", mapsFromI18n("types", Locale.FRENCH, "manager.type"), "motif_disable")), 2));
	        	}else if("deliveryByAdress".equals(typeStats)){
	        		put("deliveryByAdress",mapField(Card.countDeliveryByAddress().getResultList(),2));
	        	}else if("userDeliveries".equals(typeStats)){
	        		put("userDeliveries",mapField(Log.countUserDeliveries(),2));
	        	}else if("tarifsCrousBars".equals(typeStats)){
	        		put("tarifsCrousBars",mapField(User.countTarifCrousByType(),3));
	        	}else if("cardsByMonth".equals(typeStats)){
	        		put("cardsByMonth",mapField(Card.countNbCardRequestByMonth(typeInd), 2));
	        		put("encodedCardsByMonth",mapField(Card.countNbCardEncodedByMonth(typeInd), 2));
	        	}else if("nbRejetsByMonth".equals(typeStats)){
	        		put("nbRejetsByMonth",mapField(Card.countNbRejetsByMonth(typeInd), 2));
	        	}else if("requestFree".equals(typeStats)){
	        		put("requestFree",mapField(User.countNbRequestFree(),3));
	        	}else if("templateCards".equals(typeStats)){
	        		put("templateCards",mapField(TemplateCard.countTemplateCardByNameVersion(),2));
	        	}else if("europeanCardChart".equals(typeStats)){
	        		put("europeanCardChart",mapField(User.countNbEuropenCards(),2));
	        	}else if("nbRoles".equals(typeStats)){
	        		put("nbRoles",mapField(User.countNbRoles(),2));
	        	}else if("pendingCards".equals(typeStats)){
	        		put("pendingCards",mapField(User.countNbPendingCards(typeInd), 2));
	        	}else if("dueDate".equals(typeStats)){
	        		put("dueDate",mapField(Card.countNbRejetsByMonth(typeInd), 2));
	        	}
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
    
    public LinkedHashMap<String, String> getYesterdayCardsByPopulationCrous (String typeDate){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	finalMap = getStatsCardsByPopulationCrous (User.countYesterdayCardsByPopulationCrous(this.getDates().get("isMonday"),typeDate));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getMonthCardsByPopulationCrous (String typeDate){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	finalMap = getStatsCardsByPopulationCrous (User.countMonthCardsByPopulationCrous(this.getDates().get("likeMonth"),typeDate));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getYearEnabledCardsByPopulationCrous (String typeDate, String anneeUniv) throws ParseException{
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	Date dateFin = getDateFinAnneeUniv(anneeUniv);

    	finalMap = getStatsCardsByPopulationCrous (User.countYearEnabledCardsByPopulationCrous(anneeUniv,typeDate, dateFin));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, LinkedHashMap<String, String>> getAllPastYearEnabledCardsByPopulationCrous (String typeDate) throws ParseException{
    	
    	LinkedHashMap<String, LinkedHashMap<String, String>> anneesMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
    	int i = 0; 
    	for(Map.Entry<String, String> entry : this.getAnneeUnivs().entrySet()){
    		if(i!=0){
	    		LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
	        	
	        	Date dateFin = getDateFinAnneeUniv(entry.getValue());
	
	        	finalMap = getStatsCardsByPopulationCrous (User.countYearEnabledCardsByPopulationCrous(entry.getValue(),typeDate, dateFin));
	    		
	    		anneesMap.put(entry.getKey(), finalMap);
    		}
    		i++;
    	}
    	
    	return anneesMap;
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
        
		String splitYear []= getCurrentAnneUniv().split("/");
        
        String splitcrrentYear []= splitYear[splitYear.length-1].split("-");
        String formatYear = splitcrrentYear[2].concat("-").concat(splitcrrentYear[1]).concat("-").concat(splitcrrentYear[0]);
    	
        mapDates.put("isMonday", isMonday);
    	mapDates.put("yesterday", yesterday);
    	mapDates.put("month", month);
    	mapDates.put("likeMonth", likeMonth);
    	mapDates.put("year", splitYear[splitYear.length-1]);
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
	
	public LinkedHashMap<String,String> getAnneeUnivs(){
		
		LinkedHashMap<String,String> annneUniv = new LinkedHashMap<String,String>();
		//Vrai date univ sinon par défaut 1er juillet de l'année courante ou passée
		if(appliConfigService.getCurrentAnneeUniv()!=null){
			String splitYear []= getCurrentAnneUniv().split("/");
			for(int i=splitYear.length-1; i>-1; i--){
				String annee = splitYear[i].split("-")[0].trim();
				annneUniv.put(annee,  splitYear[i].trim());
			}
		}else{
			int year = Calendar.getInstance().get(Calendar.YEAR);
			int month = Calendar.getInstance().get(Calendar.MONTH);
			if(month <7){
				year = year-1;
			}
			annneUniv.put(String.valueOf(year), String.valueOf(year).concat("-07-01"));
		}
		
		return annneUniv;
	}
	
	public Date getDateFinAnneeUniv(String date) throws ParseException{
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String splitDate []= date.split("/");
		String realDate =  splitDate[0];
		String splitRealDate [] = realDate.split("-");
		int endYear = Integer.valueOf(splitRealDate[0]) + 1;
		LinkedHashMap<String,String> anneesUnivs = this.getAnneeUnivs();
		String endDate = anneesUnivs.get(String.valueOf(endYear));
		Date dateFin = null;
		if(endDate!= null){
			dateFin = formatter.parse(endDate);
		}
		
		return dateFin;
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
	
	public HashMap<String,String> mapsFromI18n(String type, Locale locale, String msg){
		
		HashMap<String,String> map = new HashMap<>();
		if("etats".equals(type)){
			 for (Etat e : Etat.values()) {
				 map.put(e.name(), messageSource.getMessage(msg.concat(".").concat(e.name()), null, locale));
			 }
		}else if("motifs".equals(type)){
			 for (MotifDisable m : MotifDisable.values()) {
				 map.put(m.name(), messageSource.getMessage(msg.concat(".").concat(m.name()), null, locale));
			 }	
		}else if("types".equals(type)){
			for(String userType : userInfoService.getListExistingType()) {
				 map.put(userType, messageSource.getMessage(msg.concat(".").concat(userType), null, locale));
			 }	
		}
		
		 return map;
	}
	
	public String mapToCase(String field, HashMap<String,String> map, String alias){
		
		String req = "CASE ";
		
		for(Map.Entry<String, String> entry : map.entrySet()){
			
			req += " WHEN " + field + " LIKE '" + entry.getKey() + "' THEN '" + entry.getValue() + "' ";
			
		}
		
		req += " ELSE " + field + " END AS " + alias;
				
		return req;
		
	}
			
    
}
