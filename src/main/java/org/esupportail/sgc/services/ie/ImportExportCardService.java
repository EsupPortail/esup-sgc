package org.esupportail.sgc.services.ie;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.BigFileDaoService;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.HexStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Transactional
@Service
public class ImportExportCardService {

    private final static Logger log = LoggerFactory.getLogger(ImportExportCardService.class);

    static DateTimeFormatter importDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    static final String DEFAULT_PHOTO = "media/nophoto.png";

    static final String DEFAULT_ESCR_PHOTO = "media/photo_esc.png";

    static final public String DEFAULT_PHOTO_MIME_TYPE = "image/jpg";

    static final public String PHOTO_DIRECTORY_IMPORT = "/opt/photos-import/";

    @Resource
    UserInfoService userInfoService;

    @Resource
    BigFileDaoService bigFileDaoService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

    private static byte[] noImgPhoto = null;

    /*
        * Import une ligne CSV
        * Format attendu :
        * date d'impression/encodage;date de dernière modification;CSN;Autorisation données crous (Autorisée/Interdite);Identifiant Access-Control;eppn;diffusion photo (Oui/Non);id;etat de la carte;generatedIds;qrcode
        * Soit :
        * encodedDate;lastEncodedDate;csn;crous;desfireId;eppn;difPhoto;etat
        * Exemple :
        * 28/01/2015 14:40:35;14/06/2017 23:05:29;803412abcd5704;Autorisée;100020000002120;testju@univ-rouen.fr;Oui;ENABLED
     */
	public boolean importCsvLine(String csv, Boolean inverseCsn, byte[] bytesPhoto) throws IOException {

		String[] fields = csv.split(";");

		String eppn = null;

		if(fields.length>5) {
			eppn = fields[5];
		}
		if(eppn != null && userDaoService.findUser(eppn) != null) {
			log.info(eppn + " exists already ?");
			//return false;
		}

        LocalDateTime printedDate = null;
        LocalDateTime lastModificationDate = null;

		if(!fields[0].isEmpty()) {
			printedDate = parseDate(fields[0]);
		}
		if(!fields[1].isEmpty()) {
			lastModificationDate = parseDate(fields[1]);
		}

		String csn = fields[2].toUpperCase();
		if(inverseCsn) {
			csn = HexStringUtils.swapPairs(csn); 
		}

		Boolean crous = "Autorisée".equals(fields[3]);
		String desfireId = fields[4];

        Boolean difPhoto = false;
        if(fields.length>6) {
            difPhoto = "Oui".equals(fields[6]);
        }
        Etat etat = Etat.ENABLED;
        if(fields.length>8) {
            try {
                etat = Etat.valueOf(fields[8]);
            } catch(Exception e) {
                log.debug("Error parsing this etat " + fields[8], e);
            }
        }
        Map<String, String> generatedIdsMap = new HashMap<>();
        if(fields.length>9) {
            // generatedIds
            String generatedIds = fields[9];
            String[] appIds = generatedIds.split("\\|");
            for(String appId : appIds) {
                String[] pair = appId.split("=");
                if(pair.length==2) {
                    log.debug("GeneratedId for app " + pair[0] + " = " + pair[1]);
                    generatedIdsMap.put(pair[0], pair[1]);
                }
            }
        }
        String qrcode = null;
        if(fields.length>10) {
            qrcode = fields[10];
        }

		if(eppn != null) {
			User user = userDaoService.findUser(eppn);
			if(user != null) {
				log.info(eppn + " exists already ?");
				//return false;
			} else {
				user = new User();
			}
			user.setEppn(eppn);
			user.setCrous(crous);
			user.setDifPhoto(true);
			Card card = new Card();
			card.setEppn(eppn);
			card.setCsn(csn);
			card.getDesfireIds().put(AccessControlService.AC_APP_NAME, desfireId);
            for(String appName : generatedIdsMap.keySet()) {
                card.getDesfireIds().put(appName, generatedIdsMap.get(appName));
            }
			card.setDeliveredDate(printedDate);
			card.setEnnabledDate(printedDate);
			card.setRequestDate(printedDate);
			card.setEncodedDate(printedDate);
			card.setLastEncodedDate(printedDate);
			card.setDateEtat(lastModificationDate);
            card.setQrcode(qrcode);
			userInfoService.setAdditionalsInfo(user, null);
			String photoFileNameFound = "";
			byte[] bytes = null;
            if(bytesPhoto!=null) {
                bytes = bytesPhoto;
            } else {
                bytes = loadNoImgPhoto();
                // on tente de récupérer la photo depuis différents noms de fichiers (eppn, supannEtuId, etc.) -> à garder ?
                for (String photoFileName : new String[]{StringUtils.leftPad(user.getSecondaryId(), 8, "0"), user.getSecondaryId(),
                        StringUtils.leftPad(user.getSupannEtuId(), 8, "0"), user.getSupannEtuId(),
                        StringUtils.leftPad(user.getSupannEmpId(), 8, "0"), user.getSupannEmpId(),
                        user.getEppn().replaceAll("@.*", ""), StringUtils.leftPad(user.getEppn(), 8, "0"), user.getEppn()}) {
                    try {
                        bytes = loadPhoto("file://" + PHOTO_DIRECTORY_IMPORT + photoFileName + ".jpg");
                        photoFileNameFound = "photoFileName";
                        break;
                    } catch (IOException e) {
                        //
                    }
                }
            }
			Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
            bigFileDaoService.setBinaryFile(card.getPhotoFile().getBigFile(), bytes);
			card.getPhotoFile().setFilename(photoFileNameFound);
			card.getPhotoFile().setContentType(DEFAULT_PHOTO_MIME_TYPE);
			card.getPhotoFile().setFileSize(fileSize);
			card.setUserAccount(user);
			userDaoService.persist(user);
			userInfoService.setPrintedInfo(card);
			card.setEtat(etat);
            card.setDifPhoto(difPhoto);
			card.setDateEtat(LocalDateTime.now());
            cardDaoService.persist(card);
			log.info("Card added for: " + eppn);
			return true;
		}
		return false;
	}


	private LocalDateTime parseDate(String dateAsString) {
        LocalDateTime date = null;
		try {
			date = LocalDateTime.parse(dateAsString, importDateFormat);
		} catch (DateTimeParseException e) {
			log.debug("Error parsing this date " + dateAsString, e);
		}
		return date;
	}

	public static byte[] loadNoImgPhoto() {
		if(noImgPhoto == null) {
			String image= DEFAULT_PHOTO;
			ClassPathResource noImg = new ClassPathResource(image);
			try {
				noImgPhoto = IOUtils.toByteArray(noImg.getInputStream());
			} catch (IOException e) {
				log.warn("IOException reading image", e);
			}
		}
		return noImgPhoto;
	}
	
	public static byte[] loadNoImgEscrPhoto() {
		if(noImgPhoto == null) {
			String image= DEFAULT_ESCR_PHOTO;
			ClassPathResource noImg = new ClassPathResource(image);
			try {
				noImgPhoto = IOUtils.toByteArray(noImg.getInputStream());
			} catch (IOException e) {
				log.warn("IOException reading image", e);
			}
		}
		return noImgPhoto;
	}

	private byte[] loadPhoto(String filePath) throws IOException {
		byte[] bytes = loadNoImgPhoto();
		UrlResource photoResource = new UrlResource(filePath);
		bytes = IOUtils.toByteArray(photoResource.getInputStream());
		return bytes;
	}


    /*
        * Retourne l'entrée CSV d'une carte
        * Correspond aux chanps permettant un import
        * Cf la méthode importCsvLine
        * encodedDate;lastEncodedDate;csn;crous;card.getDesfireIds().get(AccessControlService.AC_APP_NAME);eppn;difPhoto;id;etat;generatedIds;qrcode
        *
     */
    public String exportCsvLine(Card card) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter exportDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        if(card.getEncodedDate() != null) {
            sb.append(card.getEncodedDate().format(exportDateFormat));
        }
        sb.append(";");
        if(card.getLastEncodedDate() != null) {
            sb.append(card.getLastEncodedDate().format(exportDateFormat));
        }
        sb.append(";");
        if(card.getCsn() != null) {
            sb.append(card.getCsn());
        }
        sb.append(";");
        if(card.getUserAccount() != null && card.getUserAccount().getCrous() != null) {
            sb.append(card.getUserAccount().getCrous() ? "Autorisée" : "Interdite");
        }
        sb.append(";");
        String desfireId = card.getDesfireIds().get(AccessControlService.AC_APP_NAME);
        if(desfireId != null) {
            sb.append(desfireId);
        }
        sb.append(";");
        if(card.getEppn() != null) {
            sb.append(card.getEppn());
        }
        sb.append(";");
        if(card.getUserAccount() != null && card.getUserAccount().getDifPhoto() != null) {
            sb.append(card.getUserAccount().getDifPhoto() ? "Oui" : "Non");
        }
        sb.append(";");
        sb.append(card.getId());
        sb.append(";");
        if(card.getEtat() != null) {
            sb.append(card.getEtat().toString());
        }
        sb.append(";");
        String generatedIds = "";
        for(String appName : card.getDesfireIds().keySet()) {
            if(!generatedIds.isEmpty()) {
                generatedIds += "|";
            }
            generatedIds += appName + "=" + card.getDesfireIds().get(appName);
        }
        sb.append(generatedIds);
        sb.append(";");
        if(!StringUtils.isEmpty(card.getQrcode())) {
            sb.append(card.getQrcode());
        }
        return sb.toString();
    }

    @Transactional
    public void putPhotoInStream(Card card, ZipOutputStream zos) throws SQLException, IOException {
        zos.write(card.getPhotoFile().getBigFile().getBinaryFileasBytes());
    }


}
