package org.esupportail.sgc.services;

import org.esupportail.sgc.dao.*;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


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

    @Resource
    CardDaoService cardDaoService;

    @Resource
    LogDaoService logDaoService;

    @Resource
    PayboxTransactionLogDaoService payboxTransactionLogDaoService;

    @Resource
    TemplateCardDaoService templateCardDaoService;

    @Resource
    UserDaoService userDaoService;

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

	
public List mapFieldWith2Labels(List<Object[]> queryResults, boolean order) {
    	
    	List data = new ArrayList<>();
    	
    	List<String> labels1 = new ArrayList<String>();
    	for(Object[] r : queryResults) {
    		if(!labels1.contains(r[0].toString())) {
    			labels1.add(r[0].toString());
    		}
    	}   	
    	data.add(labels1);
    	
        List<String> labels2 = new ArrayList<String>();
        for(Object[] r : queryResults) {
        	if(!labels2.contains(r[1].toString())) {
        		labels2.add(r[1].toString());
        	}
    	}    	
    	
        Map<String, List<Long>> valuesMap = new HashMap<String, List<Long>>();
    	for(String label2: labels2) {
    		ArrayList<Long> values = new ArrayList<Long>();
    		// initialize to 0
    		for(String label1: labels1) {
    			values.add(0L);
    		}
    		for(Object[] r : queryResults) {
    	       	if(label2.equals(r[1].toString())) {
    	       		values.set(labels1.indexOf(r[0].toString()), Long.valueOf(r[2].toString()));
    	       	}
    		 }
    		valuesMap.put(label2, values);
    	}
    	if(order) {
	    	// order valuesMap
	    	Map<String, List<Long>> valuesMapSorted = valuesMap
	    	        .entrySet()
	    	        .stream()
	    	        .sorted(Entry.comparingByValue(new Comparator<List<Long>>() {
						@Override
						public int compare(List<Long> o1, List<Long> o2) {
							Long v1 = 0L;
							Long v2 = 0L;
							for(Long s: o1) {
								v1 += s;
							}
							for(Long s: o2) {
								v2 += s;
							}
							return v2.compareTo(v1);
						}
					}))
	    	        .collect(
	    	            Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
	    	                LinkedHashMap::new));
	    	
	    	data.add(valuesMapSorted);
    	} else {
    		data.add(valuesMap);
    	}
    	
        return data;
    }

    
    public List mapFieldWith1Labels(List<Object[]> queryResults) {
    	
    	List data = new ArrayList<>();
    	
    	List<String> labels1 = new ArrayList<String>();
    	for(Object[] r : queryResults) {
    		if(r[0] == null) {
    			labels1.add("");
    		} else if(!labels1.contains(r[0].toString())) {
    			labels1.add(r[0].toString());
    		}
    	}   	
    	data.add(labels1);

    	ArrayList<Long> values = new ArrayList<Long>();

    	for(Object[] r : queryResults) {
    	    values.add(Long.valueOf(r[1].toString()));
    	}
    	data.add(values);
    	
        return data;
    }
    
    @SuppressWarnings("serial")
	public  LinkedHashMap<String,Object> getStats(String typeInd, String typeStats) throws ParseException {
			
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>() {
			   
			   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			   LinkedHashMap<String,String>  anneeUnivs = getAnneeUnivs();
	        {
	        	if("cardsEdited".equals(typeStats)){
	        		put("cardsEdited", mapFieldWith1Labels(cardDaoService.countNbCardsEditedByYear(typeInd)));
	        	}else if("cardsOld".equals(typeStats)){
	        		put("cardsOld", mapFieldWith1Labels(cardDaoService.countNbCardsEnabledEncodedByYear(typeInd)));
	        	}else if("cardsByYearEtat".equals(typeStats)){
	        		put("cardsByYearEtat", mapFieldWith2Labels(cardDaoService.countNbCardsByYearEtat(typeInd, mapToCase("etat", mapsFromI18n("etats", Locale.FRENCH, "card.label"), "etat")), true));
	        	}else if("crous".equals(typeStats)){
	        		put("crous", mapFieldWith1Labels(userDaoService.countNbCrous(typeInd)));
	        	}else if("difPhoto".equals(typeStats)){
	        		put("difPhoto", mapFieldWith1Labels(userDaoService.countNbDifPhoto(typeInd)));
	        	}else if("cardsByDay".equals(typeStats)){
	        		put("cardsByDay", mapFieldWith1Labels(cardDaoService.countNbCardsByDay(typeInd, "request_date")));
	        	}else if("paybox".equals(typeStats)){
	        		put("paybox", mapFieldWith2Labels(payboxTransactionLogDaoService.countNbPayboxByYearEtat(), true));
	        	}else if("motifs".equals(typeStats)){
	        		put("motifs", mapFieldWith1Labels(cardDaoService.countNbCardsByMotifsDisable(typeInd, mapToCase("motif_disable", mapsFromI18n("motifs", Locale.FRENCH, "card.label"), "motif_disable"))));
	        	}else if("dates".equals(typeStats)){
	        		put("dates", mapFieldWith2Labels(cardDaoService.countNbCardsByMonthYear(typeInd), false));
	        	}else if("encodagedates".equals(typeStats)){
	        		put("encodagedates", mapFieldWith2Labels(cardDaoService.countNbCardsEncodedByMonthYear(typeInd), false));
	        	}else if("deliveredCardsByDay".equals(typeStats)){
	        		put("deliveredCardsByDay", mapFieldWith1Labels(cardDaoService.countNbDeliverdCardsByDay(typeInd)));
	        	}else if("encodedCardsByday".equals(typeStats)){
	        		put("encodedCardsByday", mapFieldWith1Labels(cardDaoService.countNbEncodedCardsByDay(typeInd)));
	        	}else if("nbCards".equals(typeStats)){
	        		put("nbCards", mapFieldWith1Labels(userDaoService.countNbCardsByuser(typeInd)));
	        	}else if("editable".equals(typeStats)){
	        		put("editable", mapFieldWith1Labels(userDaoService.countNbEditable()));
	        	}else if("browsers".equals(typeStats)){
	        		put("browsers", mapFieldWith1Labels(cardDaoService.countBrowserStats(typeInd)));
	        	}else if("os".equals(typeStats)){
	        		put("os", mapFieldWith1Labels(cardDaoService.countOsStats(typeInd)));
	        	}else if("realos".equals(typeStats)){
	        		put("realos", mapFieldWith1Labels(cardDaoService.countRealOsStats(typeInd)));
	        	}else if("nbRejets".equals(typeStats)){
	        		put("nbRejets", mapFieldWith1Labels(cardDaoService.countNbCardsByRejets(typeInd)));
	        	}else if("notDelivered".equals(typeStats)){
	        		put("notDelivered", mapFieldWith2Labels(cardDaoService.countNbEditedCardNotDelivered(mapToCase("user_type", mapsFromI18n("types", Locale.FRENCH, "manager.type"), "motif_disable")), true));
	        	}else if("deliveryByAdress".equals(typeStats)){
	        		put("deliveryByAdress", mapFieldWith1Labels(cardDaoService.countDeliveryByAddress()));
	        	}else if("noneditableByAdress".equals(typeStats)){
	        		put("noneditableByAdress", mapFieldWith1Labels(cardDaoService.countNonEditableByAddress()));
	        	}else if("userDeliveries".equals(typeStats)){
	        		put("userDeliveries", mapFieldWith1Labels(logDaoService.countUserDeliveries()));
	        	}else if("tarifsCrousBars".equals(typeStats)){
	        		put("tarifsCrousBars", mapFieldWith2Labels(userDaoService.countTarifCrousByType(), true));
	        	}else if("nextDueDatesOneYearByType".equals(typeStats)){
	        		put("nextDueDatesOneYearByType", mapFieldWith2Labels(userDaoService.countNextDueDatesOneYearByType(), true));
	        	}else if("nextDueDatesOneMonthByType".equals(typeStats)){
	        		put("nextDueDatesOneMonthByType", mapFieldWith2Labels(userDaoService.countNextDueDatesOneMonthByType(), true));
	        	}else if("cardsByMonth".equals(typeStats)){
	        		put("cardsByMonth", mapFieldWith1Labels(cardDaoService.countNbCardRequestByMonth(typeInd)));
	        		put("encodedCardsByMonth", mapFieldWith1Labels(cardDaoService.countNbCardEncodedByMonth(typeInd)));
	        	}else if("nbRejetsByMonth".equals(typeStats)){
	        		put("nbRejetsByMonth", mapFieldWith1Labels(cardDaoService.countNbRejetsByMonth(typeInd)));
	        	}else if("requestFree".equals(typeStats)){
	        		put("requestFree", mapFieldWith2Labels(userDaoService.countNbRequestFree(), true));
	        	}else if("templateCards".equals(typeStats)){
	        		put("templateCards", mapFieldWith1Labels(templateCardDaoService.countTemplateCardByNameVersion()));
	        	}else if("europeanCardChart".equals(typeStats)){
	        		put("europeanCardChart", mapFieldWith1Labels(userDaoService.countNbEuropenCards()));
	        	}else if("nbRoles".equals(typeStats)){
	        		put("nbRoles", mapFieldWith1Labels(userDaoService.countNbRoles()));
	        	}else if("pendingCards".equals(typeStats)){
	        		put("pendingCards", mapFieldWith1Labels(userDaoService.countNbPendingCards(typeInd)));
	        	}else if("dueDate".equals(typeStats)){
	        		put("dueDate", mapFieldWith1Labels(cardDaoService.countDueDatesByDate(typeInd)));
	        	}else if("cardsByEtat".equals(typeStats)){
	        		put("cardsByEtat", mapFieldWith1Labels(cardDaoService.countNbCardsByEtat(typeInd)));
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
    	
    	finalMap = getStatsCardsByPopulationCrous (userDaoService.countYesterdayCardsByPopulationCrous(this.getDates().get("isMonday"),typeDate));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getMonthCardsByPopulationCrous (String typeDate){
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	finalMap = getStatsCardsByPopulationCrous (userDaoService.countMonthCardsByPopulationCrous(this.getDates().get("likeMonth"),typeDate));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, String> getYearEnabledCardsByPopulationCrous (String typeDate, String anneeUniv) throws ParseException{
    	
    	LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
    	
    	Date dateFin = getDateFinAnneeUniv(anneeUniv);

    	finalMap = getStatsCardsByPopulationCrous (userDaoService.countYearEnabledCardsByPopulationCrous(anneeUniv,typeDate, dateFin));
    	
    	return finalMap;
    }
    
    public LinkedHashMap<String, LinkedHashMap<String, String>> getAllPastYearEnabledCardsByPopulationCrous (String typeDate) throws ParseException{
    	
    	LinkedHashMap<String, LinkedHashMap<String, String>> anneesMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
    	int i = 0; 
    	for(Map.Entry<String, String> entry : this.getAnneeUnivs().entrySet()){
    		if(i!=0){
	    		LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
	        	
	        	Date dateFin = getDateFinAnneeUniv(entry.getValue());
	
	        	finalMap = getStatsCardsByPopulationCrous (userDaoService.countYearEnabledCardsByPopulationCrous(entry.getValue(),typeDate, dateFin));
	    		
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
