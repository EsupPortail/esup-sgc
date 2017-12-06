package org.esupportail.sgc.services.ie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

@Service
public class ImportExportService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static String processorsDateType[] = new String[]{"requestDate", "dateEtat", "deliveredDate"};

	@Resource
	ImportExportCardService importExportCardService;

	@Resource
	MessageSource messageSource;

	public void consumeCsv(InputStream stream, Boolean inverseCsn) {
		try {
			List<String> csvList = IOUtils.readLines(stream);

			// on retire l'entête du CSV : 
			String csvHeader = csvList.remove(0);
			log.info("CSV HEADER : " + csvHeader);
			int i =0;
			for(String csv: csvList) {
				try {
					if(importExportCardService.importCsvLine(csv, inverseCsn)) {
						i++;
					}
				} catch(Exception e) {
					log.error("Error with this csv line : " + csv, e);
				}
			}

			log.info(i + " users imported !");

		} catch (IOException e) {
			throw new SgcRuntimeException("Error during parsing csv", e);
		}
	}


	public static CellProcessor[] getProcessors(List<String> fields) {

		int fieldsSize = fields.size();
		final CellProcessor[] processors = new CellProcessor[fieldsSize];
		int i = 0;
		for(String field : fields){
			if(Arrays.asList(processorsDateType).contains(field)){
				processors[i] =  new Optional(new FmtDate("dd-MM-yyyy HH:mm"));
			}else{
				processors[i] = new ConvertNullTo("");
			}
			i++;
		}

		return processors;
	}

	public List<String> getHeadersFromProperties(List<String> fields){

		List<String> fieldsProperties = new ArrayList<String>();

		for(String field : fields){
			fieldsProperties.add(messageSource.getMessage("card.csv.".concat(field), null, Locale.ROOT));
		}

		return fieldsProperties;
	}


	public void exportCsv2OutputStream(CardSearchBean searchBean, String eppn, List<String> fields, OutputStream outputStream) {

		ICsvBeanWriter beanWriter = null;
		
		Writer writer = null;
		
		try{
			String header[] = (String[]) fields.toArray(new String[0]);

			writer = new OutputStreamWriter(outputStream, "UTF8");

			beanWriter =  new CsvBeanWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

			List<String> fieldsProperties = getHeadersFromProperties(fields);

			String headerFr[] = (String[]) fieldsProperties.toArray(new String[0]);		 

			beanWriter.writeHeader(headerFr);

			final CellProcessor[] processors = this.getProcessors(fieldsProperties);

			List<Card> cards = Card.findCards(searchBean, eppn, null, null).getResultList();
			for(Card card : cards) {
				beanWriter.write(card, header,processors);
			}		
		} catch(Exception e){
			log.warn("Interruption de l'export", e);
		} finally {
			if(beanWriter!=null) {
				try {
					beanWriter.close();
				} catch (IOException e) {
					log.warn("IOException ...", e);
				}
			}
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					log.warn("IOException ...", e);
				}
			}
		}
	}
}
