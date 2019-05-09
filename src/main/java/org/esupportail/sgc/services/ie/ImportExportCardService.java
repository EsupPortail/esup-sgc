package org.esupportail.sgc.services.ie;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.HexStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ImportExportCardService {

	private final static Logger log = LoggerFactory.getLogger(ImportExportCardService.class);

	static DateFormat importDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	static final String DEFAULT_PHOTO = "media/nophoto.png";
	
	static final String DEFAULT_ESCR_PHOTO = "media/photo_esc.png";

	static final public String DEFAULT_PHOTO_MIME_TYPE = "image/jpg";

	static final public String PHOTO_DIRECTORY_IMPORT = "/opt/photos-import/";

	@Resource
	CardEtatService cardEtatService;

	@Resource
	UserInfoService userInfoService;

	private static byte[] noImgPhoto = null;


	public boolean importCsvLine(String csv, Boolean inverseCsn) throws IOException {

		String[] fields = csv.split(";");

		String eppn = null;

		if(fields.length>5) {
			eppn = fields[5];
		}
		if(eppn != null && User.findUser(eppn) != null) {
			log.info(eppn + " exists already ?");
			//return false;
		}

		Date printedDate = null;
		Date lastModificationDate = null;

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

		if(eppn != null) {
			User user = User.findUser(eppn);
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
			card.setDeliveredDate(printedDate);
			card.setEnnabledDate(printedDate);
			card.setRequestDate(printedDate);
			card.setEncodedDate(printedDate);
			card.setLastEncodedDate(printedDate);
			card.setDateEtat(lastModificationDate);
			userInfoService.setAdditionalsInfo(user, null);
			String photoFileNameFound = "";
			byte[] bytes = loadNoImgPhoto();
			// on tente de récupérer la photo depuis différents noms de fichiers (eppn, supannEtuId, etc.) -> à garder ?
			for(String photoFileName : new String[] {StringUtils.leftPad(user.getSecondaryId(), 8, "0"), user.getSecondaryId(), 
					StringUtils.leftPad(user.getSupannEtuId(), 8, "0"), user.getSupannEtuId(),
					StringUtils.leftPad(user.getSupannEmpId(), 8, "0"), user.getSupannEmpId(),
					user.getEppn().replaceAll("@.*", ""), StringUtils.leftPad(user.getEppn(), 8, "0"), user.getEppn()}) {
				try{
					bytes = loadPhoto("file://" + PHOTO_DIRECTORY_IMPORT + photoFileName + ".jpg");
					photoFileNameFound = "photoFileName";
					break;
				}  catch (IOException e) {
					//
				}
			}
			Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
			card.getPhotoFile().getBigFile().setBinaryFile(bytes);
			card.getPhotoFile().setFilename(photoFileNameFound);
			card.getPhotoFile().setContentType(DEFAULT_PHOTO_MIME_TYPE);
			card.getPhotoFile().setFileSize(fileSize);
			card.setUserAccount(user);
			user.persist();
			userInfoService.setPrintedInfo(card);
			card.setEtat(Etat.ENABLED);
			card.setDateEtat(new Date());
			card.persist();
			log.info("Card added for: " + eppn);
			return true;
		}
		return false;
	}


	private Date parseDate(String dateAsString) {
		Date date = null;
		try {
			date = importDateFormat.parse(dateAsString);
		} catch (ParseException e) {
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


}
