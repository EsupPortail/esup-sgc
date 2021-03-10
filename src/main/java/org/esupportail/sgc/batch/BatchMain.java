package org.esupportail.sgc.batch;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BatchMain {

	public static void main(String[] args) throws IOException, SQLException  {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		
		if(args.length < 1 || !"dbupgrade".equals(args[0])) {
			System.err.println("#####\n" +
					"Merci de préciser les arguments.\n" +
					"Voici les possibilités : \n" +
					"\t* mvn exec:java -Dexec.args=\"dbupgrade\"\n" +
					"#####");
			return;
		}

		if("dbupgrade".equals(args[0])) {
			DbToolService dbToolService = springContext.getBean("dbToolService", DbToolService.class);
			dbToolService.disableIndexTriggers();
			dbToolService.upgrade();
			dbToolService.reindex();
			dbToolService.enableIndexTriggers();
		} else {
			System.err.println("Commande non trouvée.");
		}
		
		springContext.close();
	}



}
