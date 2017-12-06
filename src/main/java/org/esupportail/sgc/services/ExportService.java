package org.esupportail.sgc.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

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
		List<List<Object[]>> statsList = new ArrayList<List<Object[]>>(2);
		LinkedHashMap<Integer, String> typeCsv =  null;
		
	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
	   Date date = formatter.parse(appliConfigService.getCurrentAnneeUniv());
	  // Date date = formatter.parse("2017-06-07");
		
		if("editable".equals(stats)){
			objs = User.selectEditableCsv().getResultList();
		}else if("notDelivered".equals(stats)){
			objs = Card.countDeliveryByAddress(date).getResultList();
		}else if("tableStats".equals(stats)){
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
			objs = User.countYearEnabledCardsByPopulationCrous(statsService.getDates().get("year"), "request_date");
			typeCsv.put(2, message + datesStats.get("formatYear"));
			statsList.add(objs); objs = new ArrayList<>();
			message = messageSource.getMessage("stats.table.maj.yesterday", null, locale);
			objs = User.countYesterdayMajCardsByPopulationCrous(statsService.getDates().get("year"));
			typeCsv.put(3, message + datesStats.get("yesterday"));
			statsList.add(objs); objs = new ArrayList<>();
			message = messageSource.getMessage("stats.table.maj.month", null, locale);
			objs = User.countMonthMajCardsByPopulationCrous(statsService.getDates().get("likeMonth"));
			typeCsv.put(4, message + datesStats.get("month"));
			statsList.add(objs); objs = new ArrayList<>();
			message = messageSource.getMessage("stats.table.maj.year", null, locale);
			objs = User.countYearMajEnabledCardsByPopulationCrous(statsService.StringToDate("yyyy-mm-dd", statsService.getDates().get("year")));
			typeCsv.put(5, message +  datesStats.get("formatYear"));
			statsList.add(objs);
		}
		
		List<ExportBean> exportList = new ArrayList<>();

		ExportBean exportBean1 = null;
		ExportBean exportBean = null;
		if(!"tableStats".equals(stats)){
			for(Object[] item : objs) {
				if("editable".equals(stats)){
					exportBean = new ExportBean();
					exportBean.setEditable(item[0].toString());
					exportBean.setNom(item[1].toString());
					exportBean.setPrenom(item[2].toString());
					exportBean.setEmail(item[3].toString());
				}else if("notDelivered".equals(stats)){
					exportBean = new ExportBean();
					exportBean.setAdresse(item[0].toString());
					exportBean.setNombre(item[1].toString());
				}
				exportList.add(exportBean);
			}
		}else{
			int i = 0;
			for(List<Object[]> object : statsList){
				int total = 0;						
				if(object!=null){
					exportBean1 = new ExportBean();
					exportBean = new ExportBean();
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
