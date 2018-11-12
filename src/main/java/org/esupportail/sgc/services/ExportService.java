package org.esupportail.sgc.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.ExportBean;
import org.esupportail.sgc.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class ExportService {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	StatsService statsService;
	
	@Autowired
    MessageSource messageSource;

	public List<ExportBean> getBean(String stats, Locale locale) throws ParseException{
		
		List<Object[]> objs = new ArrayList<>();
	   
		if("editable".equals(stats)){
			objs = User.selectEditableCsv().getResultList();
		}else if("notDelivered".equals(stats)){
			objs = Card.countDeliveryByAddress().getResultList();
		}
		
		List<ExportBean> exportList = new ArrayList<>();

		ExportBean exportBean = null;
		for(Object[] item : objs) {
			if("editable".equals(stats)){
				exportBean = new ExportBean();
				exportBean.setEditable(item[0].toString());
				String nom = "";
				if(item[3] != null){
					nom= item[1].toString();
				}
				exportBean.setNom(nom);
				String prenom = "";
				if(item[2] != null){
					prenom= item[2].toString();
				}	
				exportBean.setPrenom(prenom);
				String email = "";
				if(item[3] != null){
					email= item[3].toString();
				}
				exportBean.setEmail(email);
			}else if("notDelivered".equals(stats)){
				exportBean = new ExportBean();
				exportBean.setAdresse(item[0].toString());
				exportBean.setNombre(item[1].toString());
			}
			exportList.add(exportBean);
		}
		return exportList;
	}
	
	public List<ExportBean> getBeanTableStats(Locale locale) throws ParseException{
		
		List<Object[]> objs = new ArrayList<>();
		List<List<Object[]>> statsList = new ArrayList<List<Object[]>>(2);
		LinkedHashMap<Integer, String> typeCsv =  null;
		LinkedHashMap<String, String> datesStats =  statsService.getDates();
		typeCsv = new LinkedHashMap<Integer, String> ();
		String message = messageSource.getMessage("stats.table.edited.yesterday", null, locale);
		objs = User.countYesterdayCardsByPopulationCrous(statsService.getDates().get("isMonday"),"encoded_date");
		typeCsv.put(0, message + datesStats.get("yesterday"));
		statsList.add(objs); objs = new ArrayList<>();
		message = messageSource.getMessage("stats.table.edited.month", null, locale);
		objs = User.countMonthCardsByPopulationCrous(statsService.getDates().get("likeMonth"),"encoded_date");
		typeCsv.put(1, message + datesStats.get("month"));
		statsList.add(objs); objs = new ArrayList<>();
		message = messageSource.getMessage("stats.table.edited.year", null, locale);
		objs = User.countYearEnabledCardsByPopulationCrous(statsService.getDates().get("year"), "request_date", statsService.getDateFinAnneeUniv(statsService.getDates().get("year")));
		typeCsv.put(2, message + datesStats.get("formatYear"));
		statsList.add(objs);
		message = messageSource.getMessage("stats.table.edited.all", null, locale);
		 int k = 0;  int j = 2; 
	    	for(Map.Entry<String, String> entry : statsService.getAnneeUnivs().entrySet()){
	    		if(k!=0){
		        	Date dateFin = statsService.getDateFinAnneeUniv(entry.getValue());
		        	objs = User.countYearEnabledCardsByPopulationCrous(entry.getValue(),"request_date", dateFin);
		        	typeCsv.put(j+1, message + entry.getKey());
	    		}
	    		k++;
	    	}
		statsList.add(objs);
		
		List<ExportBean> exportList = new ArrayList<>();

		ExportBean exportBean1 = null;
		int i = 0;
		for(List<Object[]> object : statsList){
			int total = 0;						
			if(object!=null){
				exportBean1 = new ExportBean();
				exportBean1.setType(typeCsv.get(i));
				for(Object[] item : object) {
					if("ctr".equals(item[0].toString())){
						exportBean1.setContractuel(item[1].toString());
					}else if("etd".equals(item[0].toString())){
						exportBean1.setEtudiant(item[1].toString());
					}else if("fct".equals(item[0].toString())){
						exportBean1.setFormationContinue(item[1].toString());
					}else if("fpa".equals(item[0].toString())){
						exportBean1.setApprentissage(item[1].toString());
					}else if("hbg".equals(item[0].toString())){
						exportBean1.setHeberge(item[1].toString());
					}else if("psg".equals(item[0].toString())){
						exportBean1.setPassager(item[1].toString());
					}else if("prs".equals(item[0].toString())){
						exportBean1.setPersonnel(item[1].toString());
					}else if("po".equals(item[0].toString())){
						exportBean1.setPersonnelOuvrier(item[1].toString());
					}else if("stg".equals(item[0].toString())){
						exportBean1.setStagiaire(item[1].toString());
					}
					total += Integer.valueOf(item[1].toString());
				}
				if(exportBean1!=null){
					exportBean1.setTotal(total);
					exportList.add(exportBean1);			
				}
			}
			i++;
		}
		return exportList;
	}
	
	public String [] getHeader(String stats){
		
		if("editable".equals(stats)){
			return new String[]{"editable", "nom", "prenom", "email"};
		}else if("notDelivered".equals(stats)){
			return new String[]{"adresse", "nombre"};
		}else if("tableStats".equals(stats)){
			return new String[]{"type", "contractuel", "etudiant", "formationContinue", "apprentissage", "heberge", "passager", 
					"personnel", "personnelOuvrier", "stagiaire", "total"};
		}
		else{
			return new String[]{};
		}
	}
}
