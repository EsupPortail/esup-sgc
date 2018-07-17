package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.User;

public class FormService {
	
	private List<String> fieldsList = new ArrayList<String>();
	
	private int nbFields = 3;
	
	public int getNbFields() {
		return nbFields;
	}
	public void setNbFields(int nbFields) {
		this.nbFields = nbFields;
	}
	public List<String> getFieldsList() {
		return fieldsList;
	}
	public void setFieldsList(List<String> fieldsList) {
		this.fieldsList = fieldsList;
	}
	
	public ArrayList<String> getFieldList() {
		
		ArrayList<String> fields = new ArrayList<>();
		fields = (ArrayList<String>) fieldsList;
		
		return fields;
	}
	
	public ArrayList<String> getFieldList2() {
		ArrayList<String> fields = new ArrayList<>();
		ArrayList<String> camelFields = new ArrayList<>();
		fields = (ArrayList<String>) fieldsList;
		for(String item : fields){
			camelFields.add(Card.snakeToCamel(item));
		}
		return camelFields;
	}

	public List<String> getField1List(String field) {
		List<String> fields = new ArrayList<String>();
		// prevent sql injection here
		if(fieldsList.contains(field)) {
			fields = User.getDistinctFreeField(field);
			fields.remove("");
		}
		return fields;
	}
}
