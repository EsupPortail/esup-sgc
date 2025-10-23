package org.esupportail.sgc.batch;

import java.io.*;
import java.sql.SQLException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.esupportail.sgc.services.ie.ImportExportService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BatchMain {

	public static void main(String[] args) throws IOException, SQLException  {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		
		if(args.length < 1 || !"dbupgrade".equals(args[0]) && !"exportZip".equals(args[0]) && !"importZip".equals(args[0])
                || (("exportZip".equals(args[0]) || "importZip".equals(args[0])) && args.length < 2)) {
			System.err.println("#####\n" +
					"Merci de préciser les arguments.\n" +
					"Voici les possibilités : \n" +
					"\t* mvn exec:java -Dexec.args=\"dbupgrade\"\n" +
                    "\t* mvn exec:java -Dexec.args=\"exportZip /tmp/esup-sgc.zip\"\n" +
                    "\t* mvn exec:java -Dexec.args=\"importZip /tmp/esup-sgc.zip\"\n" +
					"#####");
			return;
		}

		if("dbupgrade".equals(args[0])) {
			DbToolService dbToolService = springContext.getBean("dbToolService", DbToolService.class);
			dbToolService.disableIndexTriggers();
			dbToolService.upgrade();
			dbToolService.reindex();
			dbToolService.enableIndexTriggers();
		} else if("exportZip".equals(args[0])) {
            ImportExportService importExportService = springContext.getBean("importExportService", ImportExportService.class);
            String outputFilePath = args[1];
            OutputStream out = new FileOutputStream(outputFilePath);
            ZipOutputStream zos = new ZipOutputStream(out);
            importExportService.exportToZip(new CardSearchBean(), zos);
            zos.finish();
            zos.close();
            out.close();
            System.out.println("Export terminé dans le fichier : " + outputFilePath);
        } else if("importZip".equals(args[0])) {
            ImportExportService importExportService = springContext.getBean("importExportService", ImportExportService.class);
            String inputFilePath = args[1];
            InputStream in = new FileInputStream(inputFilePath);
            importExportService.consumeZip(in);
            in.close();
            System.out.println("Import terminé depuis le fichier : " + inputFilePath);
        } else {
			System.err.println("Commande non trouvée.");
		}
		
		springContext.close();
	}



}
