package org.esupportail.sgc.services.ac;

import org.springframework.beans.factory.BeanNameAware;

import java.io.IOException;
import java.util.List;

public interface Export2AccessControlService extends BeanNameAware {

	void sync(List<String> eppns) throws IOException;

	void sync(String eppn) throws IOException;
	
	String getEppnFilter();

	StringBuffer sgc2csv(List<String> eppns);

	public String getBeanName();

}
