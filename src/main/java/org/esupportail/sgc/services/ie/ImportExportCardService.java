package org.esupportail.sgc.services.ie;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.User.CnousReferenceStatut;
import org.esupportail.sgc.services.CardEtatService;
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
	
	static final String DEFAULT_PHOTO = "No_image_s.jpg";
	
	static final public String DEFAULT_PHOTO_MIME_TYPE = "image/jpeg";
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	UserInfoService userInfoService;
	
	private static byte[] noImgPhoto = null;

	
	public boolean importCsvLine(String csv, Boolean inverseCsn) throws IOException {

		String[] fields = csv.split(";");
		
		Boolean valid = "Valide".equals(fields[0]);
		if(!valid) {
			return false;
		}
		
		String eppn = null;
		
		if(fields.length>19) {
			eppn = fields[19];
		}
		if(eppn != null && User.findUser(eppn) != null) {
			log.warn(eppn + " exists already ?");
			return false;
		}
		
		Date printedDate = null;
		Date lastModificationDate = null;

		if(!fields[2].isEmpty()) {
			printedDate = parseDate(fields[2]);
		}
		if(!fields[3].isEmpty()) {
			lastModificationDate = parseDate(fields[3]);
		}
		
		String csn = fields[14].toUpperCase();
		if(inverseCsn) {
			csn = HexStringUtils.swapPairs(csn); 
		}
		
		Boolean crous = "Autorisée".equals(fields[16]);
		String desfireId = fields[18];

		if(eppn != null) {
			User user = User.findUser(eppn);
			if(user != null) {
				log.warn(eppn + " exists already ?");
				return false;
			} else {
				user = new User();
			}
			user.setEppn(eppn);
			user.setCrous(crous);
			user.setDifPhoto(true);
			Card card = new Card();
			card.setEppn(eppn);
			card.setCsn(csn);
			card.setDesfireId(desfireId);
			card.setDeliveredDate(printedDate);
			card.setEnnabledDate(printedDate);
			card.setRequestDate(printedDate);
			card.setEncodedDate(printedDate);
			card.setLastEncodedDate(printedDate);
			card.setDateEtat(lastModificationDate);
			userInfoService.setAdditionalsInfo(user, null);
			// TODO : name  == null === non connu dans le ldap
			if(valid && user.getName() != null) {
				byte[] bytes = loadNoImgPhoto();
				String photoFileName = DEFAULT_PHOTO;
				try{
					if(CnousReferenceStatut.prs.equals(user.getCnousReferenceStatut())) {
						photoFileName = user.getSupannEmpId();
						photoFileName = StringUtils.leftPad(photoFileName, 8, "0");
						bytes = loadPhoto("file:///opt/easy-id-import/UR/Personnel/" + photoFileName + ".jpg");
					} else if(CnousReferenceStatut.etd.equals(user.getCnousReferenceStatut()) || 
							CnousReferenceStatut.fpa.equals(user.getCnousReferenceStatut()) || 
							CnousReferenceStatut.fct.equals(user.getCnousReferenceStatut())) {
						photoFileName = user.getSupannEtuId();
						photoFileName = StringUtils.leftPad(photoFileName, 8, "0");
						bytes = loadPhoto("file:///opt/easy-id-import/UR/Etudiant/" + photoFileName + ".jpg");
					} else if(CnousReferenceStatut.hbg.equals(user.getCnousReferenceStatut())) {
						photoFileName = user.getSupannEmpId();
						photoFileName = StringUtils.leftPad(photoFileName, 8, "0");
						bytes = loadPhoto("file:///opt/easy-id-import/UR/Heberge/" + photoFileName + ".jpg");
					} else {
						photoFileName = user.getEppn().replaceAll("@.*", "");
						photoFileName = StringUtils.leftPad(photoFileName, 8, "0");
						bytes = loadPhoto("file:///opt/easy-id-import/UR/Invite/" + photoFileName + ".jpg");
					}
				}  catch (IOException e) {
					try{ 
						photoFileName = user.getSupannEtuId();
						if(photoFileName==null) {
							photoFileName = user.getSupannEmpId();
						}
						if(photoFileName==null) {
							photoFileName = user.getEppn().replaceAll("@.*", "");
						}
						String photoFileNameBase = photoFileName;
						photoFileName = StringUtils.leftPad(photoFileName, 8, "0");
						File photoFile = new File("/opt/easy-id-import/UR/Etudiant/" + photoFileName + ".jpg");
						for(String photoFileNameTest : Arrays.asList(new String[] {photoFileName, photoFileNameBase})) {
							if(!photoFile.exists()) {
								 photoFile = new File("/opt/easy-id-import/UR/Etudiant/" + photoFileNameTest + ".jpg");
							}
					        if(!photoFile.exists()) {
					        	photoFile = new File("/opt/easy-id-import/UR/Personnel/" + photoFileNameTest + ".jpg");
					        }
					        if(!photoFile.exists()) {
					        	photoFile = new File("/opt/easy-id-import/UR/Heberge/" + photoFileNameTest + ".jpg");
					        }
					        if(!photoFile.exists()) {
					        	photoFile = new File("/opt/easy-id-import/UR/Invite/" + photoFileNameTest + ".jpg");
					        }
						}
				        if(!photoFile.exists()) {
				        	log.warn("Error retrieving photo for " + user.getEppn() +  " - " + photoFileName);
				        }
				        bytes = loadPhoto("file://" + photoFile.getAbsolutePath());					
					} catch (IOException ee) {
						log.warn("Error retrieving photo for " + user.getEppn(), ee);
					}
				}
				Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
				card.getPhotoFile().getBigFile().setBinaryFile(bytes);
				card.getPhotoFile().setFilename(photoFileName);
				card.getPhotoFile().setContentType(DEFAULT_PHOTO_MIME_TYPE);
				card.getPhotoFile().setFileSize(fileSize);
				card.setUserAccount(user);
				user.persist();
				userInfoService.setPrintedInfo(card, null);
				card.setEtat(Etat.ENABLED);
				card.setDateEtat(new Date());
				card.persist();
				log.info("Card added for: " + eppn);
				return true;
			} else {
				//cardEtatService.setCardEtat(card, Etat.DISABLED);
			}
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
	
	private byte[] loadPhoto(String filePath) throws IOException {
		byte[] bytes = loadNoImgPhoto();
		UrlResource photoResource = new UrlResource(filePath);
		bytes = IOUtils.toByteArray(photoResource.getInputStream());
		return bytes;
	}

	
}
