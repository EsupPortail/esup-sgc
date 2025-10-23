package org.esupportail.sgc.services.ie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jakarta.annotation.Resource;

import jakarta.persistence.TypedQuery;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.crous.CrousSmartCardEntryService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

@Service
public class ImportExportService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static String processorsDateType[] = new String[]{"requestDate", "dateEtat", "deliveredDate"};
	
	private Boolean inWorking = false;

    private Boolean inWorkingZip = false;

	@Resource
	ImportExportCardService importExportCardService;

	@Resource
	MessageSource messageSource;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    CrousSmartCardEntryService crousSmartCardEntryService;

	public Boolean isInWorking() {
		return inWorking;
	}

    public  Boolean isInWorkingZip() {
        return inWorkingZip;
    }


	@Async
	public synchronized void consumeCsv(InputStream stream, Boolean inverseCsn) {
		inWorking = true;
		try {
			List<String> csvList = IOUtils.readLines(stream);

			// on retire l'entête du CSV : 
			String csvHeader = csvList.remove(0);
			log.info("CSV HEADER : " + csvHeader);
			int i =0;
			for(String csv: csvList) {
				try {
					if(importExportCardService.importCsvLine(csv, inverseCsn, null)) {
						i++;
					}
				} catch(Exception e) {
					log.error("Error with this csv line : " + csv, e);
				}
			}

			log.info(i + " users imported !");

		} catch (Exception e) {
			throw new SgcRuntimeException("Error during parsing csv", e);
		} finally {
			inWorking = false;
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
		
		String message = "";

		for(String field : fields){
			message = messageSource.getMessage("card.csv.".concat(field), null, Locale.ROOT);
			if("card.csv.".concat(field).equals(message)){
				message = field.replace("userAccount.", "").replace("card.", "");
			}
			fieldsProperties.add(message);
		}

		return fieldsProperties;
	}


	public void exportCsv2OutputStream(CardSearchBean searchBean, String eppn, List<String> fields, OutputStream outputStream) {

		CsvDozerBeanWriter beanWriter = null;
		
		Writer writer = null;
		String[] FIELD_MAPPING = new String[fields.size()];
		int i = 0;
		for(String field : fields) {
			FIELD_MAPPING[i] = field.replaceFirst("card.", "");
			i++;
		}
		
		try{
			writer = new OutputStreamWriter(outputStream, "UTF8");

			beanWriter =  new CsvDozerBeanWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

			List<String> fieldsProperties = getHeadersFromProperties(fields);

			String headerFr[] = (String[]) fieldsProperties.toArray(new String[0]);

			beanWriter.writeHeader(headerFr);
			
			final CellProcessor[] processors = this.getProcessors(fieldsProperties);

            beanWriter.configureBeanMapping(Card.class, FIELD_MAPPING);

			List<Card> cards = cardDaoService.findCards(searchBean, eppn, null, null).getResultList();
			for(Card card : cards) {
				beanWriter.write(card,processors);
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

    public void addCsvCardsToZip(TypedQuery<Card> cardsTypedQuery, ZipOutputStream zos) throws IOException {
        ZipEntry fileEntry = new ZipEntry("cards.csv");
        zos.putNextEntry(fileEntry);
        for(Card card : cardsTypedQuery.getResultList()) {
            String csvLine = importExportCardService.exportCsvLine(card);
            zos.write(csvLine.getBytes());
            zos.write(System.lineSeparator().getBytes());
        }
        zos.closeEntry();
    }

    public void addCrousCsvCardsToZip(TypedQuery<Card> cardsTypedQuery, ZipOutputStream zos) throws IOException {
        ZipEntry fileEntry = new ZipEntry("crous_cards.csv");
        zos.putNextEntry(fileEntry);
        for(Card card : cardsTypedQuery.getResultList()) {
            if(card.getUserAccount() != null && card.getUserAccount().getCrous() != null) {
                String csvLine = crousSmartCardEntryService.exportCrousCsvLine(card);
                if(csvLine != null) {
                    zos.write(csvLine.getBytes());
                    zos.write(System.lineSeparator().getBytes());
                }
            }
        }
        zos.closeEntry();
    }

    public void addPhotosCardsToZip(TypedQuery<Card> cardsTypedQuery, ZipOutputStream zos) throws IOException, SQLException {
        ZipEntry folderEntry = new ZipEntry("photos/");
        zos.putNextEntry(folderEntry);
        zos.closeEntry();
        for(Card card : cardsTypedQuery.getResultList()) {
            if(card.getPhotoFile() != null) {
                ZipEntry photoEntry = new ZipEntry("photos/" + card.getId() + ".jpg");
                zos.putNextEntry(photoEntry);
                importExportCardService.putPhotoInStream(card, zos);
                zos.closeEntry();
            }
        }
    }

    public void exportToZip(CardSearchBean searchBean, ZipOutputStream zos) throws IOException, SQLException {
       TypedQuery<Card> cardsTypedQuery = cardDaoService.findCards(searchBean, null, null, null);
       addCsvCardsToZip(cardsTypedQuery, zos);
       addCrousCsvCardsToZip(cardsTypedQuery, zos);
       addPhotosCardsToZip(cardsTypedQuery, zos);
    }

    @Async
    public synchronized void consumeZip(InputStream stream) {
        inWorkingZip =  true;
        try {
            ZipInputStream zis = new ZipInputStream(stream);
            ZipEntry entry;
            Map<String, String> cardsEntries = new HashMap<>();
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                log.debug("Processing zip entry : " + entryName);
                if (entryName.equals("cards.csv")) {
                    String cardsCsv = IOUtils.toString(zis);
                    for (String line : cardsCsv.split("\n")) {
                        String[] parts = line.split(";");
                        if (parts.length > 0) {
                            String key = parts[7];
                            cardsEntries.put(key, line);
                        }
                    }
                } else if (entryName.equals("crous_cards.csv")) {
                    String crousCardsCsv = IOUtils.toString(zis);
                    crousSmartCardEntryService.consumeCrousCsv(crousCardsCsv);
                } else {
                    if(cardsEntries.isEmpty()) {
                        log.error("cards.csv non trouvé - " + entryName + " le précède ? - cards.csv doit être avant les photos dans le zip !");
                    }
                    if(entryName.startsWith("photos/") && entryName.endsWith(".jpg")) {
                        String cardId = entryName.substring(7, entryName.length() - 4);
                        String csvLine = cardsEntries.get(cardId);
                        if (csvLine != null) {
                            try {
                                importExportCardService.importCsvLine(csvLine, false, zis.readAllBytes());
                            } catch (Exception e) {
                                log.error("Error with this csv line : " + csvLine, e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SgcRuntimeException("Error during parsing zip", e);
        } finally {
            inWorkingZip = false;
        }
    }
}
