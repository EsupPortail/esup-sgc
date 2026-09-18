package org.esupportail.sgc.services.ie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.csv.CSVPrinter;

@Service
public class ImportExportService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static String processorsDateType[] = new String[]{"requestDate", "dateEtat", "deliveredDate"};
	private static final int CSV_EXPORT_BATCH_SIZE = 1000;
	
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

    @PersistenceContext
    transient EntityManager entityManager;

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


	public static boolean[] getDateFieldsMask(List<String> fields) {

		int fieldsSize = fields.size();
		final boolean[] isDateField = new boolean[fieldsSize];
		int i = 0;
		for(String field : fields){
			isDateField[i] = Arrays.asList(processorsDateType).contains(field);
			i++;
		}

		return isDateField;
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


	@Transactional(readOnly = true)
	public void exportCsv2OutputStream(CardSearchBean searchBean, String eppn, List<String> fields, OutputStream outputStream) {
		CSVPrinter csvPrinter = null;
		Writer writer = null;
		List<String> fieldMapping = new ArrayList<String>(fields.size());
		for(String field : fields) {
			fieldMapping.add(field.replaceFirst("card.", ""));
		}
		
		try{
			long totalCards = cardDaoService.countFindCards(searchBean, eppn);
			log.info("CSV export start: {} cards, {} fields, batchSize={}", totalCards, fields.size(), CSV_EXPORT_BATCH_SIZE);
			writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

			csvPrinter = new CSVPrinter(writer, CsvExportUtils.EXCEL_NORTH_EUROPE);

			List<String> fieldsProperties = getHeadersFromProperties(fields);

			csvPrinter.printRecord(fieldsProperties);
			
			final boolean[] isDateField = this.getDateFieldsMask(fields);

			int firstResult = 0;
			long exportedCards = 0;
			while(true) {
				List<Card> cards = cardDaoService.findCards(searchBean, eppn, null, null)
					.setFirstResult(firstResult)
					.setMaxResults(CSV_EXPORT_BATCH_SIZE)
					.getResultList();
				if(cards.isEmpty()) {
					break;
				}
				for(Card card : cards) {
					Object[] row = CsvExportUtils.extractRow(card, fieldMapping, isDateField, "dd-MM-yyyy HH:mm");
					csvPrinter.printRecord(row);
					exportedCards++;
				}
				csvPrinter.flush();
				log.trace("CSV export progress: {}/{} cards written, heapUsed={} MB", exportedCards, totalCards, usedHeapMb());
				entityManager.clear();
				firstResult += cards.size();
			}
			log.info("CSV export done: {} cards written", exportedCards);
		} catch(Exception e){
			log.warn("Interruption de l'export", e);
		} finally {
			if(csvPrinter!=null) {
				try {
					csvPrinter.close();
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

	private long usedHeapMb() {
		Runtime runtime = Runtime.getRuntime();
		long usedBytes = runtime.totalMemory() - runtime.freeMemory();
		return usedBytes / (1024 * 1024);
	}

    public void addCsvCardsToZip(CardSearchBean searchBean, String eppn, ZipOutputStream zos) throws IOException {
        ZipEntry fileEntry = new ZipEntry("cards.csv");
        zos.putNextEntry(fileEntry);
        String header =  "encodedDate;lastEncodedDate;csn;crous;access-control-id;eppn;difPhoto;id;etat;generatedIds;qrcode";
        zos.write(header.getBytes(StandardCharsets.UTF_8));
        zos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        int firstResult = 0;
        while (true) {
            List<Card> cards = cardDaoService.findCards(searchBean, eppn, null, null)
                .setFirstResult(firstResult)
                .setMaxResults(CSV_EXPORT_BATCH_SIZE)
                .getResultList();
            if (cards.isEmpty()) {
                break;
            }
            for(Card card : cards) {
                String csvLine = importExportCardService.exportCsvLine(card);
                zos.write(csvLine.getBytes(StandardCharsets.UTF_8));
                zos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }
            zos.flush();
            entityManager.clear();
            firstResult += cards.size();
        }
        zos.closeEntry();
    }

    public void addCrousCsvCardsToZip(CardSearchBean searchBean, String eppn, ZipOutputStream zos) throws IOException {
        ZipEntry fileEntry = new ZipEntry("crous_cards.csv");
        zos.putNextEntry(fileEntry);
        String header = "PIX.SS;PIX.NN;AAPL;NUM_PROTOCOLAIRE;NUM_APPLICATIF;NFO;CNOUS;CROUS;EMETTEUR;MAPPING;NUM_CARTE;DATE_CREATION";
        zos.write(header.getBytes(StandardCharsets.UTF_8));
        zos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        int firstResult = 0;
        while (true) {
            List<Card> cards = cardDaoService.findCards(searchBean, eppn, null, null)
                .setFirstResult(firstResult)
                .setMaxResults(CSV_EXPORT_BATCH_SIZE)
                .getResultList();
            if (cards.isEmpty()) {
                break;
            }
            for(Card card : cards) {
                if(card.getUserAccount() != null && card.getUserAccount().getCrous() != null) {
                    String csvLine = crousSmartCardEntryService.exportCrousCsvLine(card);
                    if(csvLine != null) {
                        zos.write(csvLine.getBytes(StandardCharsets.UTF_8));
                        zos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            zos.flush();
            entityManager.clear();
            firstResult += cards.size();
        }
        zos.closeEntry();
    }

    public void addPhotosCardsToZip(CardSearchBean searchBean, String eppn, ZipOutputStream zos) throws IOException, SQLException {
        ZipEntry folderEntry = new ZipEntry("photos/");
        zos.putNextEntry(folderEntry);
        zos.closeEntry();
        int firstResult = 0;
        while (true) {
            List<Card> cards = cardDaoService.findCards(searchBean, eppn, null, null)
                .setFirstResult(firstResult)
                .setMaxResults(CSV_EXPORT_BATCH_SIZE)
                .getResultList();
            if (cards.isEmpty()) {
                break;
            }
            for(Card card : cards) {
                if(card.getPhotoFile() != null) {
                    ZipEntry photoEntry = new ZipEntry("photos/" + card.getId() + ".jpg");
                    zos.putNextEntry(photoEntry);
                    importExportCardService.putPhotoInStream(card, zos);
                    zos.closeEntry();
                }
            }
            zos.flush();
            entityManager.clear();
            firstResult += cards.size();
        }
    }

    @Transactional(readOnly = true)
    public void exportToZip(CardSearchBean searchBean, ZipOutputStream zos) throws IOException, SQLException {
       addCsvCardsToZip(searchBean, null, zos);
       addCrousCsvCardsToZip(searchBean, null, zos);
       addPhotosCardsToZip(searchBean, null, zos);
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
                    for (String line : cardsCsv.split("\\r?\\n")) {
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
