package org.esupportail.sgc.services.userinfos;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.BigFileDaoService;
import org.esupportail.sgc.dao.PhotoFileDaoService;
import org.esupportail.sgc.dao.TemplateCardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.User.CnousReferenceStatut;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.EsistCrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.esupportail.sgc.tools.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Transactional
public class UserInfoService {

	public enum SynchroCmd {SYNCHRONIZE, JUST_FORCE_CADUC, NONE}

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String DATE_FORMAT = "dd-MM-yyyy";
	
	private final static String DATE_FORMAT_2 = "yyyy-MM-dd";
	
	private final static String DATE_FORMAT_UTCSEC_LDAP = "yyyyMMddHHmmss'Z'";
	
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
	
	private DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern(DATE_FORMAT_2);
	
	String caducIfEmpty = null;
	
	List<ExtUserInfoService> extUserInfoServices;
	
	@Autowired
	EsistCrousService esistCrousService;
	
	@Resource
	CrousService crousService;

	@Resource
	DateUtils dateUtils;
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    BigFileDaoService bigFileDaoService;

    @Resource
    PhotoFileDaoService photoFileDaoService;

    @Resource
    TemplateCardDaoService templateCardDaoService;

    @Resource
    UserDaoService userDaoService;
	
	@Autowired
	public void setExtUserInfoServices(List<ExtUserInfoService> extUserInfoServices) {
		this.extUserInfoServices = extUserInfoServices;
		Collections.sort(this.extUserInfoServices, (p1, p2) -> p1.getOrder().compareTo(p2.getOrder()));
	}

	public void setCaducIfEmpty(String caducIfEmpty) {
		this.caducIfEmpty = caducIfEmpty;
	}

	protected DateTimeFormatter getDateFormatter() {
		return dateFormatter;
	}
	
	protected DateTimeFormatter getDateFormatter2() {
		return dateFormatter2;
	}
	
	
	private DateTimeFormatter dateTimeUTCsecFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_UTCSEC_LDAP);
	
	protected DateTimeFormatter getDateTimeUTCsecFormatter() {
		return dateTimeUTCsecFormatter;
	}

	public SynchroCmd setAdditionalsInfo(User user, HttpServletRequest request) {
		return setAdditionalsInfo(user, request, null);
	}
	
	/**
	 * Get and set userInfos from userInfoservices to user object
	 * Return false if synchronize is set to false by userInfoservices computing
	 */
	public SynchroCmd setAdditionalsInfo(User user, HttpServletRequest request, List<String> fields2reinitialize) {
		Map<String, String> userInfos = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); 
		userInfos.put("synchronize", "true");
		/* Initialize fields to null for fields in list
		 * Used when synchronize userInfo and some fields must be set to null if not present anymore
		 */
		if(fields2reinitialize != null) {
			for(String field: fields2reinitialize) {
				if("cnousReferenceStatut".equals(field)) {
					userInfos.put("referenceStatut", null);
				} else if("rneEtablissemnt".equals(field)) {
					userInfos.put("supannEtablissement", null);
				} else if("dueDate".equals(field)) {
					userInfos.put("schacExpiryDate", null);
				} else {
					userInfos.put(field, null);
				}
			}
		}

		for(ExtUserInfoService extUserInfoService : extUserInfoServices) {
			if(user.getEppn().matches(extUserInfoService.getEppnFilter())) {
				userInfos.putAll(extUserInfoService.getUserInfos(user, request, userInfos));
			}
		}
		log.trace("userInfos for " + user.getEppn() + " would be : " + userInfos);
		for(String key : userInfos.keySet()) {
			if("firstname".equalsIgnoreCase(key)) {
				user.setFirstname(userInfos.get(key));
			} else if("name".equalsIgnoreCase(key)) {
				user.setName(userInfos.get(key));
			} else if("eduPersonPrimaryAffiliation".equalsIgnoreCase(key)) {
				user.setEduPersonPrimaryAffiliation(userInfos.get(key));
			} else if("supannEntiteAffectationPrincipale".equalsIgnoreCase(key)) {
				user.setSupannEntiteAffectationPrincipale(userInfos.get(key));
			} else if("supannEmpId".equalsIgnoreCase(key)) {
				user.setSupannEmpId(userInfos.get(key));
			} else if("supannEtuId".equalsIgnoreCase(key)) {
				user.setSupannEtuId(userInfos.get(key));
			} else if("supannCodeINE".equalsIgnoreCase(key)) {
				user.setSupannCodeINE(userInfos.get(key));
			} else if("birthday".equalsIgnoreCase(key)) {
				LocalDateTime birthday = parseDate(userInfos.get(key));
				user.setBirthday(birthday);
			} else if("email".equalsIgnoreCase(key)) {
				user.setEmail(userInfos.get(key));
			} else if("institute".equalsIgnoreCase(key)) {
				user.setInstitute(userInfos.get(key));
			} else if("supannEtablissement".equalsIgnoreCase(key)) {
				String rneEtablissemnt = "";
				String supannEtablissementAttr = userInfos.get(key);
				if(supannEtablissementAttr!=null) {
					List<String> supannEtablissementList = Arrays.asList(supannEtablissementAttr.split(";"));
					for(String supannEtablissement: supannEtablissementList) {
						if(StringUtils.contains(supannEtablissement, "{UAI}")) {
							rneEtablissemnt = supannEtablissement.replaceAll("\\{UAI\\}", "");
						}
					}
					user.setRneEtablissement(rneEtablissemnt);
				}
			} else if("referenceStatut".equalsIgnoreCase(key)) {
				// default : psg
				CnousReferenceStatut cnousReferenceStatut  = CnousReferenceStatut.psg;
				String siReferenceStatut  = userInfos.get(key);
				try {
					cnousReferenceStatut = CnousReferenceStatut.valueOf(siReferenceStatut);
				} catch(IllegalArgumentException | NullPointerException ex) {
					log.trace("no cnous referenceStatut for " + user.getEppn() + " -> psg");
				}
				user.setCnousReferenceStatut(cnousReferenceStatut);
			} else if("indice".equalsIgnoreCase(key)) {
				if(userInfos.get(key) != null && !(userInfos.get(key)).isEmpty()) {
					Long indice  = Long.valueOf(userInfos.get(key));
					user.setIndice(indice);
				} else {
					user.setIndice(0L);
				}
			} else if("schacExpiryDate".equalsIgnoreCase(key)) {
				LocalDateTime schacExpiryDate = parseDateUTCsec(userInfos.get(key));
				user.setDueDate(schacExpiryDate);
			} else if("secondaryId".equalsIgnoreCase(key)) {
				String secondaryId  = userInfos.get(key);
				user.setSecondaryId(secondaryId);
			} else if("address".equalsIgnoreCase(key)) {
				String address  = userInfos.get(key);
				user.setAddress(address);
			} else if("externalAddress".equalsIgnoreCase(key)) {
				String externalAddress  = userInfos.get(key);
				user.setExternalAddress(externalAddress);
			} else if("recto1".equalsIgnoreCase(key)) {
				String recto1  = userInfos.get(key);
				user.setRecto1(recto1);
			} else if("recto2".equalsIgnoreCase(key)) {
				String recto2  = userInfos.get(key);
				user.setRecto2(recto2);
			} else if("recto3".equalsIgnoreCase(key)) {
				String recto3  = userInfos.get(key);
				user.setRecto3(recto3);
			} else if("recto4".equalsIgnoreCase(key)) {
				String recto4  = userInfos.get(key);
				user.setRecto4(recto4);
			} else if("recto5".equalsIgnoreCase(key)) {
				String recto5  = userInfos.get(key);
				user.setRecto5(recto5);
			} else if("recto6".equalsIgnoreCase(key)) {
				String recto6  = userInfos.get(key);
				user.setRecto6(recto6);
			} else if("recto7".equalsIgnoreCase(key)) {
				String recto7  = userInfos.get(key);
				user.setRecto7(recto7);
			}  else if("verso1".equalsIgnoreCase(key)) {
				String verso1  = userInfos.get(key);
				user.setVerso1(verso1);
			} else if("verso2".equalsIgnoreCase(key)) {
				String verso2  = userInfos.get(key);
				user.setVerso2(verso2);
			} else if("verso3".equalsIgnoreCase(key)) {
				String verso3  = userInfos.get(key);
				user.setVerso3(verso3);
			} else if("verso4".equalsIgnoreCase(key)) {
				String verso4  = userInfos.get(key);
				user.setVerso4(verso4);
			} else if("verso5".equalsIgnoreCase(key)) {
				String verso5  = userInfos.get(key);
				user.setVerso5(verso5);
			} else if("verso6".equalsIgnoreCase(key)) {
				String verso6  = userInfos.get(key);
				user.setVerso6(verso6);
			} else if("verso7".equalsIgnoreCase(key)) {
				String verso7  = userInfos.get(key);
				user.setVerso7(verso7);
			} else if("freeField1".equalsIgnoreCase(key)) {
				String freeField1  = userInfos.get(key);
				user.setFreeField1(freeField1);
			} else if("freeField2".equalsIgnoreCase(key)) {
				String freeField2  = userInfos.get(key);
				user.setFreeField2(freeField2);
			} else if("freeField3".equalsIgnoreCase(key)) {
				String freeField3  = userInfos.get(key);
				user.setFreeField3(freeField3);
			}  else if("freeField4".equalsIgnoreCase(key)) {
				String freeField4  = userInfos.get(key);
				user.setFreeField4(freeField4);
			} else if("freeField5".equalsIgnoreCase(key)) {
				String freeField5  = userInfos.get(key);
				user.setFreeField5(freeField5);
			} else if("freeField6".equalsIgnoreCase(key)) {
				String freeField6  = userInfos.get(key);
				user.setFreeField6(freeField6);
			} else if("freeField7".equalsIgnoreCase(key)) {
				String freeField7  = userInfos.get(key);
				user.setFreeField7(freeField7);
			} else if("userType".equalsIgnoreCase(key)) {
				String userType  = userInfos.get(key);
				user.setUserType(userType);
			} else if("blockUserMsg".equalsIgnoreCase(key)) {
				String blockUserMsg  = userInfos.get(key);
				user.setBlockUserMsg(blockUserMsg);
			} else if("template".equalsIgnoreCase(key)) {
				String templateKey  = userInfos.get(key);
				user.setTemplateKey(templateKey);
			} else if("schacDateOfBirth".equalsIgnoreCase(key)) {
                LocalDateTime birthday = dateUtils.parseSchacDateOfBirth(userInfos.get(key));
				user.setBirthday(birthday);
			} else if("supannRefId4ExternalCard".equalsIgnoreCase(key)) {
				// supannRefId4ExternalCard deprecated : use csn4ExternalCard and access-control4ExternalCard fields
				List<String> supannRefIds = Arrays.asList((userInfos.get(key)).split(";"));
				for(String supannRefId: supannRefIds) {
					if(StringUtils.contains(supannRefId, "{ISO15693}")) {
						user.getExternalCard().setUserAccount(user);
						String csnExternalCard = supannRefId.replaceAll("\\{ISO15693\\}", "");
						user.getExternalCard().setCsn(csnExternalCard);
					}
					// TODO : {LEOCARTE:ACCESS-CONTROL} en dur :(
					if(StringUtils.contains(supannRefId, "{LEOCARTE:ACCESS-CONTROL}")) {
						String desfireIdExternalCard = supannRefId.replaceAll("\\{LEOCARTE:ACCESS-CONTROL\\}", "");
						user.getExternalCard().getDesfireIds().put(AccessControlService.AC_APP_NAME, desfireIdExternalCard);
					}
				}
			} else if("access-control4ExternalCard".equalsIgnoreCase(key)) {
				user.getExternalCard().setUserAccount(user);
				user.getExternalCard().getDesfireIds().put(AccessControlService.AC_APP_NAME, userInfos.get(key));
			} else if("csn4ExternalCard".equalsIgnoreCase(key)) {
				user.getExternalCard().setUserAccount(user);
				user.getExternalCard().setCsn(userInfos.get(key));
			} else if("jpegPhoto4ExternalCard".equalsIgnoreCase(key)) {
				byte[] bytes = ImportExportCardService.loadNoImgPhoto();
				if(userInfos.get(key) != null && !userInfos.get(key).isEmpty()) {
					bytes = java.util.Base64.getDecoder().decode(userInfos.get(key));
				}
                bigFileDaoService.setBinaryFile(user.getExternalCard().getPhotoFile().getBigFile(), bytes);
				user.getExternalCard().getPhotoFile().setFileSize((long)bytes.length);
				user.getExternalCard().getPhotoFile().setContentType(ImportExportCardService.DEFAULT_PHOTO_MIME_TYPE);
			} else if("editable".equalsIgnoreCase(key)) {
				Boolean editable = "true".equalsIgnoreCase(userInfos.get(key));
				user.setEditable(editable);
			} else if("requestFree".equalsIgnoreCase(key)) {
				Boolean requestFree = "true".equalsIgnoreCase(userInfos.get(key));
				user.setRequestFree(requestFree);
			} else if("academicLevel".equalsIgnoreCase(key)) {
				String academicLevel = userInfos.get("academicLevel");
				if(academicLevel != null && !academicLevel.isEmpty()) {
					user.setAcademicLevel(Long.valueOf(academicLevel));
				}
			} else if("pic".equalsIgnoreCase(key)) {
				String pic = userInfos.get("pic");
				user.setPic(pic);
			} else if("jpegPhoto".equalsIgnoreCase(key)) {
				if(userInfos.get(key) != null && !userInfos.get(key).isEmpty()) {
					byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(userInfos.get(key));
					if(user.getDefaultPhoto() == null) {
						user.setDefaultPhoto(new PhotoFile());
                        photoFileDaoService.persist(user.getDefaultPhoto());
					}
                    bigFileDaoService.setBinaryFile(user.getDefaultPhoto().getBigFile(), bytes);
					user.getDefaultPhoto().setFileSize((long)bytes.length);
					user.getDefaultPhoto().setContentType(ImportExportCardService.DEFAULT_PHOTO_MIME_TYPE);
				}
			} 

		}
		
		setDefaultValues4NullAttributes(userInfos, user);
		
		List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
		Long idCompagnyRate = idCompagnyRateAndIdRate.get(0);
		Long idRate = idCompagnyRateAndIdRate.get(1);
		
		if(user.getCrous()!=null && user.getCrous()) {			
			// hack crous tarifs spéciaux étudiants ~boursiers : idCompanyRate en 10 -> idRate final/vrai vient en fait du crous 
			// on s'arrange cependant ici pour ne pas faire un Get sur Api Crous pour chaque étudiant
			// donc récupération en base de l'idRate précédemment récupéré depuis ApiCrousService.updateRightHolder
			if(Long.valueOf(10).equals(idCompagnyRate) && Long.valueOf(10).equals(user.getIdCompagnyRate()) && user.getIdRate()!=null) {
				idRate = user.getIdRate();
			}			
		}	
		
		user.setIdRate(idRate);
		user.setIdCompagnyRate(idCompagnyRate);
		
		if(user.getCrous()!=null && user.getCrous()) {			
			// hack crous ~cnrs : idCompanyRate en 7999 -> idRate final/vrai vient en fait du crous 
			if(Long.valueOf(7999).equals(user.getIdCompagnyRate())) {
				try {
					// GET sur Api Crous pour chaque agent CNRS (on estime que ça reste raisonnable)
					RightHolder rightHolder = crousService.getRightHolder(user);
					if(rightHolder == null) {
						log.debug("rightHolder on crous is null for "  + user.getEppn());
					} else {
						user.setIdRate(rightHolder.getIdRate());
						log.debug("7999 idCompagnyRate case : idRate from crous for "  + user.getEppn() + " is " + user.getIdRate());
					}
				} catch(HttpClientErrorException ex) {
					log.debug("Exception getting crous rightHolder for " + user.getEppn(), ex);
				}
			}			
		}	

		boolean forceCaduc = false;
		if(caducIfEmpty != null && !caducIfEmpty.isEmpty()) {
			String caducIfEmptyValue = userInfos.get(caducIfEmpty);
			if((caducIfEmptyValue == null || caducIfEmptyValue.isEmpty()) && user.getDueDate().isAfter(LocalDateTime.now())) {
				// si plus d'entrée ldap ou similaire -> user.getMustBeCaduc = true -> caduc
				forceDueDateCaduc(user);
				forceCaduc = true;
			}
		}

		boolean synchronize = "true".equalsIgnoreCase(userInfos.get("synchronize"));
		if(synchronize) {
			return SynchroCmd.SYNCHRONIZE;
		} else if(forceCaduc) {
			return SynchroCmd.JUST_FORCE_CADUC;
		}
		return SynchroCmd.NONE;
	}

	public void forceDueDateCaduc(User user) {
		//  caduc -> on surcharge la date de fin
        LocalDateTime dueDateIncluded = LocalDateTime.now();
		user.setDueDate(dueDateIncluded);
	}
	
	public void setPrintedInfo(Card card) {
		User user = card.getUser();
		card.setRecto1Printed(user.getRecto1());
		card.setRecto2Printed(user.getRecto2());
		card.setRecto3Printed(user.getRecto3());
		card.setRecto4Printed(user.getRecto4());
		card.setRecto5Printed(user.getRecto5());
		card.setRecto6Printed(user.getRecto6());
		card.setRecto7Printed(user.getRecto7());
        TemplateCard templateCard = templateCardDaoService.getTemplateCard(user);
		if(templateCard.getBackSupported()) {
			card.setVersoTextPrinted(StringUtils.join(user.getVersoText(), "\n"));
		}
		card.setTemplateCard(templateCard);
        card.getUser().setLastCardTemplatePrinted(templateCard);
	}

	public void setDefaultValues4NullAttributes(Map<String, String> userInfos, User user) {
		if(!userInfos.containsKey("schacExpiryDate") || userInfos.get("schacExpiryDate") == null || (userInfos.get("schacExpiryDate")).isEmpty()) {
            LocalDateTime dateFinDroits = appliConfigService.getDefaultDateFinDroits();
			user.setDueDate(dateFinDroits);
		} 
		if(!userInfos.containsKey("institute") || (userInfos.get("institute")).isEmpty()) {
			user.setInstitute(user.getEppn().replaceAll(".*@", ""));
		}
	}

    public LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        log.debug("parsing of date : " + dateString);

        try {
            DateTimeFormatter formatter = getDateFormatter(); // équivalent de getDateFormatter() mais pour LocalDateTime
            return LocalDate.parse(dateString, formatter).atStartOfDay();
        } catch (DateTimeParseException e) {
            log.error("parsing of date " + dateString + " failed", e);
            return null;
        }
    }


    public LocalDateTime parseDateUTCsec(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            log.trace("parsing of date : " + dateString);
            try {
                return LocalDateTime.parse(dateString, getDateTimeUTCsecFormatter());
            } catch (Exception e) {
                log.warn("parsing of date " + dateString + " failed", e);
            }
        }
        return null;
    }

	public void updateUser(String eppn, HttpServletRequest request) {
		User user = userDaoService.findUser(eppn);
		setAdditionalsInfo(user, request);
		log.info("UserInfo of " + user.getEppn() + " now : " + user);
	}
	
	public List<String> getListExistingType(){
		List<String> listTypes = new ArrayList<String>();
		listTypes = userDaoService.findDistinctUserType();
		return listTypes;	
	}
	
	public List<String> getListAddresses(String userType, Etat etat) {
		List<String> adresses = userDaoService.findDistinctAddresses(userType, etat);
		return adresses;
	}

	public List<TemplateCard> getDistinctLastTemplateCardsPrinted() {
		return templateCardDaoService.findDistinctLastTemplateCardsPrinted();
	}
}

