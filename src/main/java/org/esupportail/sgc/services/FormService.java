package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.sgc.domain.User;

public class FormService {
	
	private List<String> fieldsList = new ArrayList<>();
	
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
	
	public List<String> getField1List(String field) {
		List<String> fields = new ArrayList<>();
		// prevent sql injection here
		if(fieldsList.contains(field)) {
			fields = User.getDistinctFreeField(field);
		}
		return fields;
	}
}
