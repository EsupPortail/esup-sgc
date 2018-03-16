package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.List;

import org.esupportail.sgc.domain.User;
import org.springframework.stereotype.Service;

@Service
public class FormService {
	
	private List<String> fieldsList;
	
	public List<String> getFieldsList() {
		return fieldsList;
	}
	public void setFieldsList(List<String> fieldsList) {
		this.fieldsList = fieldsList;
	}
	
	public ArrayList<String> getFieldList(){
		
		ArrayList<String> fields = new ArrayList<>();
		fields = (ArrayList<String>) fieldsList;
		
		return fields;
	}
	
	public List<String> getField1List(String field){
		
		List<String> fields = new ArrayList<>();
		fields = User.getDistinctFreeField(field);
		
		return fields;
	}
}
