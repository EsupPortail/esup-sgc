package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private Map<String, String> idsMap = new HashMap<String, String>();
	
	private Map<String, String> fieldsList = new HashMap<String, String>();
	
	private int nbFields = 3;
	
	private int fieldsValuesNbMax = 200;
	
	public int getNbFields() {
		return nbFields;
	}
	public void setNbFields(int nbFields) {
		this.nbFields = nbFields;
	}
	public int getFieldsValuesNbMax() {
		return fieldsValuesNbMax;
	}
	public void setFieldsValuesNbMax(int fieldsValuesNbMax) {
		this.fieldsValuesNbMax = fieldsValuesNbMax;
	}
	public Map<String, String> getFieldsList() {
		return fieldsList;
	}
	
	public void setFieldsList(Map<String, String> fieldsList) {
		this.fieldsList = fieldsList;
	}
	
	public String encodeUrlString(String string2encode) {
		if(string2encode==null)
			return "";
		String encPath = String.valueOf(string2encode.hashCode());
		if(!idsMap.containsKey(encPath)) {
			idsMap.put(encPath, string2encode);
		}
		return encPath;
	}
	
	public String decodeUrlString(String string2decode) {
		if(string2decode == null || "".equals(string2decode)) {
			return "";
		}
		String path = idsMap.get(string2decode);
		if(path != null) {
			return path;
		} 
		return string2decode;
	}
	
	public List<String> getFieldsListAsCamel() {
		List<String> camelFields = new ArrayList<>();
		for(String item : fieldsList.keySet()){
			camelFields.add(Card.snakeToCamel(item));
		}
		return camelFields;
	}
	
	public Map<String, String> getFieldsValuesMap(String field) {
		Map<String, String> mapWithEncodedString = new HashMap<String, String>(); 
		// prevent sql injection here
		if(fieldsList.keySet().contains(field)) {
			if(field.equals("card.template_card")) {
				for(TemplateCard tc : TemplateCard.findAllTemplateCards()) {
					mapWithEncodedString.put(encodeUrlString(tc.getId().toString()), tc.toString());
				}
			} else {
				List<String> fields = new ArrayList<String>();
				if(field.startsWith("card.")) {
					long nbFields = Card.getCountDistinctFreeField(field.substring("card.".length()));
					if(nbFields>fieldsValuesNbMax) {
						log.debug(String.format("%s entrées pour le champ %s (> %s)", nbFields, field, fieldsValuesNbMax));
					} else {
						fields = Card.getDistinctFreeField(field.substring("card.".length()));
					}
				} else if(field.startsWith("user_account.")) {
					long nbFields = User.getCountDistinctFreeField(field.substring("user_account.".length()));
					if(nbFields>fieldsValuesNbMax) {
						log.debug(String.format("%s entrées pour le champ %s (> %s)", nbFields, field, fieldsValuesNbMax));
					} else {
						fields = User.getDistinctFreeField(field.substring("user_account.".length()));
					}
				} else if(field.contains(".")) {
					log.debug(String.format("champ %s inconnu ?", field));
				} else if(!"desfire_ids".equals(field)) {
					long nbFields = User.getCountDistinctFreeField(field);
					if(nbFields>fieldsValuesNbMax) {
						log.debug(String.format("%s entrées pour le champ %s (> %s)", nbFields, field, fieldsValuesNbMax));
					} else {
						fields = User.getDistinctFreeField(field);
					}
				}
				fields.remove("");
				for(String s : fields) {
					mapWithEncodedString.put(encodeUrlString(s), s);
				}
			}
		}
		return mapWithEncodedString;
	}

	public Map<String, String> getMapWithUrlEncodedString(List<String> strings) {
		Map<String, String> mapWithEncodedString = new HashMap<String, String>(); 
		for(String s : strings) {
			mapWithEncodedString.put(encodeUrlString(s), s);
		}
		return mapWithEncodedString;
	}

}


