package org.esupportail.sgc.services.userinfos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.User.CnousReferenceStatut;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.crous.AuthApiCrousService;
import org.esupportail.sgc.services.crous.EsistCrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.esupportail.sgc.tools.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Transactional
public class UserInfoService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String DATE_FORMAT = "dd-MM-yyyy";
	
	private final static String DATE_FORMAT_2 = "yyyy-MM-dd";
	
	private final static String DATE_FORMAT_UTCSEC_LDAP = "yyyyMMddHHmmss'Z'";
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	
	private SimpleDateFormat dateFormatter2 = new SimpleDateFormat(DATE_FORMAT_2);
	
	String caducIfEmpty = null;
	
	List<ExtUserInfoService> extUserInfoServices;
	
	@Autowired
	EsistCrousService esistCrousService;
	
	@Resource
	AuthApiCrousService authApiCrousService;

	@Resource
	DateUtils dateUtils;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Autowired
	public void setExtUserInfoServices(List<ExtUserInfoService> extUserInfoServices) {
		this.extUserInfoServices = extUserInfoServices;
		Collections.sort(this.extUserInfoServices, (p1, p2) -> p1.getOrder().compareTo(p2.getOrder()));
	}

	public void setCaducIfEmpty(String caducIfEmpty) {
		this.caducIfEmpty = caducIfEmpty;
	}

	protected SimpleDateFormat getDateFormatter() {
		return dateFormatter;
	}
	
	protected SimpleDateFormat getDateFormatter2() {
		return dateFormatter2;
	}
	
	
	private SimpleDateFormat dateUTCsecFormatter = new SimpleDateFormat(DATE_FORMAT_UTCSEC_LDAP);
	
	protected SimpleDateFormat getDateUTCsecFormatter() {
		return dateUTCsecFormatter;
	}

	
	/**
	 * Get and set userInfos from userInfoservices to user object
	 * Return false if synchronize is set to true by userInfoservices computing
	 */
	public boolean setAdditionalsInfo(User user, HttpServletRequest request) {
		Map<String, String> userInfos = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); 
		userInfos.put("synchronize", "true");
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
				Date birthday = parseDate(userInfos.get(key));
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
				}
			} else if("schacExpiryDate".equalsIgnoreCase(key)) {
				Date schacExpiryDate = parseDateUTCsec(userInfos.get(key));
				user.setDueDate(schacExpiryDate);
			} else if("secondaryId".equalsIgnoreCase(key)) {
				String secondaryId  = userInfos.get(key);
				user.setSecondaryId(secondaryId);
			} else if("address".equalsIgnoreCase(key)) {
				String address  = userInfos.get(key);
				user.setAddress(address);
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
				Date birthday = dateUtils.parseSchacDateOfBirth(userInfos.get(key));
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
				user.getExternalCard().getPhotoFile().getBigFile().setBinaryFile(bytes);
				user.getExternalCard().getPhotoFile().setFileSize((long)bytes.length);
				user.getExternalCard().getPhotoFile().setContentType(ImportExportCardService.DEFAULT_PHOTO_MIME_TYPE);
			} else if("editable".equalsIgnoreCase(key)) {
				Boolean editable = "true".equalsIgnoreCase(userInfos.get(key));
				user.setEditable(editable);
			} else if("requestFree".equalsIgnoreCase(key)) {
				Boolean requestFree = "true".equalsIgnoreCase(userInfos.get(key));
				user.setRequestFree(requestFree);
			} 

		}
		
		setDefaultValues4NullAttributes(userInfos, user);
		if(user.getCrous()!=null && user.getCrous()) {
			List<Long> idCompagnyRateAndIdRate = esistCrousService.compute(user);
			Long idCompagnyRate = idCompagnyRateAndIdRate.get(0);
			Long idRate = idCompagnyRateAndIdRate.get(1);
			user.setIdCompagnyRate(idCompagnyRate);
			user.setIdRate(idRate);
			
			// hack crous ~cnrs : idCompanyRate en 7999 -> idRate final/vrai vient en fait du crous 
			if(Long.valueOf(7999).equals(user.getIdCompagnyRate())) {
				try {
					RightHolder rightHolder = authApiCrousService.getRightHolder(user.getEppn());
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
		
		if(caducIfEmpty != null && !caducIfEmpty.isEmpty()) {
			String caducIfEmptyValue = userInfos.get(caducIfEmpty);
			if((caducIfEmptyValue == null || caducIfEmptyValue.isEmpty()) && user.getDueDate().after(new Date())) {
				// si plus d'entrée ldap ou similaire -> user.getMustBeCaduc = true -> caduc -> on surcharge la date de fin
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(new Date());
	            cal.add(Calendar.HOUR, - User.DUE_DATE_INCLUDED_DELAY);
	            Date dueDateIncluded = cal.getTime();
				user.setDueDate(dueDateIncluded);
			}
		}
		
		
		return "true".equalsIgnoreCase(userInfos.get("synchronize"));
		
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
		card.setTemplateCard(user.getTemplateCard());
		if(user.getTemplateCard() != null) {
			card.getUser().setLastCardTemplatePrinted(user.getTemplateCard());
		}
	}

	public void setDefaultValues4NullAttributes(Map<String, String> userInfos, User user) {
		if(!userInfos.containsKey("schacExpiryDate") || (userInfos.get("schacExpiryDate")).isEmpty()) {
			Date dateFinDroits = appliConfigService.getDefaultDateFinDroits();
			user.setDueDate(dateFinDroits);
		} 
		if(!userInfos.containsKey("institute") || (userInfos.get("institute")).isEmpty()) {
			user.setInstitute(user.getEppn().replaceAll(".*@", ""));
		}
	}

	public Date parseDate(String dateString) {
		Date date = null;
		if(dateString!=null && !dateString.isEmpty()) {
			log.debug("parsing of date : " + dateString);
			try {
				date = getDateFormatter().parse(dateString);
			} catch (ParseException e) {
				log.error("parsing of date " + dateString + " failed");
			}
		}
		return date;
	}



	public Date parseDateUTCsec(String dateString) {
		Date date = null;
		if(dateString!=null && !dateString.isEmpty()) {
			log.trace("parsing of date : " + dateString);
			try {
				date = getDateUTCsecFormatter().parse(dateString);
			} catch (Exception e) {
				log.warn("parsing of date " + dateString + " failed", e);
			}
		}
		return date;
	}
	

	public Date parseDate2(String dateString) {
		Date date = null;
		if(dateString!=null && !dateString.isEmpty()) {
			log.trace("parsing of date : " + dateString);
			try {
				date = getDateFormatter2().parse(dateString);
			} catch (Exception e) {
				log.warn("parsing of date " + dateString + " failed", e);
			}
		}
		return date;
	}
	
	public void updateUser(String eppn, HttpServletRequest request) {
		User user = User.findUser(eppn);
		setAdditionalsInfo(user, request);
		log.info("UserInfo of " + user.getEppn() + " now : " + user);
	}
	
	public List<String> getListExistingType(){
		List<String> listTypes = new ArrayList<String>();
		listTypes = User.findDistinctUserType();
		return listTypes;	
	}
	
	public List<String> getListAdresses(String userType, String etat) {
		
		List<String> adresses = User.findDistinctAddresses(userType, etat);
		return adresses;
	}

	public List<TemplateCard> getDistinctLastTemplateCardsPrinted() {
		return User.findDistinctLastTemplateCardsPrinted();
	}
}

