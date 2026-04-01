package org.esupportail.sgc.services.ac;

import java.io.IOException;
import java.util.List;

public interface Export2AccessControlService {

	void sync(List<String> eppns) throws IOException;

	void sync(String eppn) throws IOException;
	
	String getEppnFilter();
		
}
