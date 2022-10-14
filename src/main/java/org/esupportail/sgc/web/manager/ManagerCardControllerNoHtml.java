package org.esupportail.sgc.web.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.EscrCard;
import org.esupportail.sgc.domain.EscrStudent;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.FormService;
import org.esupportail.sgc.services.PhotoResizeService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ldap.LdapPersonService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import flexjson.JSONSerializer;

@RequestMapping("/manager")
@Controller	
public class ManagerCardControllerNoHtml {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource 
	UserInfoService userInfoService;

	@Resource 
	CardService cardService;
	
	@Resource
	CrousService crousService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	FormService formService;
	
	@Resource
	LdapPersonService ldapPersonService;
	
	@Resource
	ApiEscrService apiEscrService;
	
	@Resource
	PhotoResizeService photoResizeService;
	
	@RequestMapping(value="/photo/{cardId}")
	@Transactional
	public ResponseEntity<byte[]> writePhotoToResponse(@PathVariable Long cardId, HttpServletResponse response) throws IOException, SQLException {
		Card card = Card.findCard(cardId);
		PhotoFile photoFile = card.getPhotoFile();
		Long size = photoFile.getFileSize();
		String contentType = photoFile.getContentType();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers.setContentLength(size.intValue());
		return new ResponseEntity(photoFile.getBigFile().getBinaryFileasBytes(), headers, HttpStatus.OK);
	}
	
	@RequestMapping(value="/{userId}/photo")
	@Transactional
	public ResponseEntity<byte[]> writeUserPhotoToResponse(@PathVariable Long userId, HttpServletResponse response) throws IOException, SQLException {
		User user = User.findUser(userId);
		return getUserPhotoAsResponseEntity(user);
	}
	
	@RequestMapping(value="/photo", params="eppn")
	@Transactional
	public ResponseEntity<byte[]> writeUserPhotoToResponse(@RequestParam String eppn, HttpServletResponse response) throws IOException, SQLException {
		User user = User.findUser(eppn);
		if(user == null) {
			user = new User();
			user.setEppn(eppn);
			userInfoService.setAdditionalsInfo(user, null);
		}
		return getUserPhotoAsResponseEntity(user);
	}

	protected ResponseEntity<byte[]> getUserPhotoAsResponseEntity(User user) throws IOException, SQLException {
		PhotoFile photoFile = user.getDefaultPhoto();
		Long size = photoFile.getFileSize();
		if(size!=null) {
			String contentType = photoFile.getContentType();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(contentType));
			headers.setContentLength(size.intValue());
			return new ResponseEntity(photoFile.getBigFile().getBinaryFileasBytes(), headers, HttpStatus.OK);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}

	
	@RequestMapping(value="/vignette/{cardId}")
	@ResponseBody
	@Transactional
	public ResponseEntity<byte[]> writePhotoVignetteToResponse(@PathVariable Long cardId, HttpServletResponse response) throws IOException, SQLException {
		Card card = Card.findCard(cardId);
		PhotoFile photoFile = card.getPhotoFile();
		byte[] vignetteImgBytes = photoResizeService.resizePhoto(photoFile, 150, 188);
		String contentType = photoFile.getContentType();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers.setContentLength(vignetteImgBytes.length);
		return new ResponseEntity(vignetteImgBytes, headers, HttpStatus.OK);
	}
	
	@RequestMapping(value="/QRCode")
	@ResponseBody
	@Transactional
	public ResponseEntity<String>  getQRCode(@RequestParam Long cardId, HttpServletResponse response) throws WriterException, IOException, SQLException {
		Card card = Card.findCard(cardId);
		PhotoFile photoFile = null;
		boolean isCodeBarres = false;
		if( card.getTemplateCard()!=null){
			isCodeBarres = card.getTemplateCard().isCodeBarres();
			photoFile = card.getTemplateCard().getPhotoFileQrCode();
		}else if(card.getUserAccount().getTemplateCard()!=null){
			isCodeBarres = card.getUserAccount().getTemplateCard().isCodeBarres();
			photoFile = card.getUserAccount().getTemplateCard().getPhotoFileQrCode();
		}
		
		String value = card.getQrcode();
		
		if(value != null) {
			if(!isCodeBarres){
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", "image/svg+xml");
				
				Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
				hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
				hints.put(EncodeHintType.MARGIN, 0);	
				hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
				
				BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 100, 100, hints); 
				
				if("SVG".equalsIgnoreCase(appliConfigService.getQrcodeFormat())) {					
					return new ResponseEntity<String>(cardService.getQrCodeSvg(value), headers, HttpStatus.OK);					
				} else {				
					headers.add("Content-Type", "image/png");
					MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream());
					return null;
				}
			} else {
				if(photoFile != null) {
					Long size = photoFile.getFileSize();
					String contentType = photoFile.getContentType();
					response.setContentType(contentType);
					response.setContentLength(size.intValue());
					IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
					log.trace("code-barres is used for this card " + cardId);
					return null;
				}
			}

		}
		
		log.debug("no qrcode for this card " + cardId);
		
		return null;
	}
	
	

    
	@RequestMapping(value="/searchEppn")
	@Transactional
	public  ResponseEntity<String> searchEppn(@RequestParam(value="searchString") String searchString) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		List<Card> eppnList = new ArrayList<Card>();
		if(!searchString.trim().isEmpty()) {
			eppnList = Card.findCardsByEppnLike(searchString, "eppn", "ASC").setMaxResults(100).getResultList();
			// hack : we keep only one card for one eppn
			Map<String, Card> cardsMap = eppnList.stream()
					.collect(
                    Collectors.toMap(c -> c.getEppn(), c -> c, 
                            (oldValue, newValue) -> newValue,       // if same key, take the old key
                            LinkedHashMap::new                      // returns a LinkedHashMap, keep order
                    ));
			eppnList = new ArrayList<Card>(cardsMap.values());
		}
		return new ResponseEntity<String>(toJsonArrayLight(eppnList), headers, HttpStatus.OK);
   }
	
	public static String toJsonArrayLight(List<Card> cards) {
		return new JSONSerializer().include(new String[] {"eppn"}).exclude("*").serialize(cards);
	}
	
	@RequestMapping(value="/filterAdress", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody
	@Transactional
	public Map<String, String> filtrerAdresse(@RequestParam(value="etat") String etat, @RequestParam(value="tabType") String tabType) {
		Map<String, String> adressesMap = new HashMap<String, String>();
		try {
			List<String> adresses = userInfoService.getListAddresses(tabType, etat.isEmpty() ? null : Etat.valueOf(etat));
			adressesMap = formService.getMapWithUrlEncodedString(adresses);
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données", e);
		}
		
    	return MapUtils.sortByValue(adressesMap, true);
	}
	
	@RequestMapping(value="/getCrousRightHolder", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody 
	@Transactional
    public RightHolder getCrousRightHolder(@RequestParam String eppn) {
		return crousService.getRightHolder(User.findUser(eppn));
	}
	
	@RequestMapping(value="/getCrousRightHolderHtmlPart")
	@Transactional
    public String getCrousRightHolderHtmlPart(@RequestParam String eppn, Model uiModel) {
		try {
			RightHolder rightHolder = crousService.getRightHolder(User.findUser(eppn));
			uiModel.addAttribute("rightHolder", rightHolder);
			return "manager/rightHolder";
		} catch(CrousAccountForbiddenException ex) {
			uiModel.addAttribute("message", ex.getMessage());
			return "manager/simple-message";
		}
	}
	
	
	@RequestMapping(value="/getCrousSmartCardUrlHtmlPart")
	@Transactional
    public String getCrousSmartCardUrlHtmlPart(@RequestParam String csn, Model uiModel) {
		CrousSmartCard crousSmartCard = crousService.getCrousSmartCard(csn);
		uiModel.addAttribute("crousSmartCard", crousSmartCard);
		return "manager/crousSmartCard";
	}
	
	
	@RequestMapping(value="/getEscrStudentHtmlPart")
	@Transactional
    public String getEscrStudentHtmlPart(@RequestParam String eppn, Model uiModel) {
		EscrStudent escrStudent = apiEscrService.getEscrStudent(eppn);
		uiModel.addAttribute("escrStudent", escrStudent);
		return "manager/escrStudent";
	}
	
	
	@RequestMapping(value="/getEscrCardHtmlPart")
	@Transactional
    public String getEscrCardHtmlPart(@RequestParam String eppn, @RequestParam String csn, Model uiModel) {
		EscrCard escrCard = apiEscrService.getEscrCard(eppn, csn);
		uiModel.addAttribute("escrCard", escrCard);
		return "manager/escrCard";
	}
	
	
	
	@RequestMapping(value="/templatePhoto/{type}/{templateId}")
	@Transactional
	public void getPhoto(@PathVariable String type, @PathVariable Long templateId, HttpServletResponse response) throws IOException, SQLException {
		
		TemplateCard templateCard = TemplateCard.findTemplateCard(templateId);
		PhotoFile photoFile = null;
		if(templateCard != null) {
			if("logo".equals(type)){
				photoFile = templateCard.getPhotoFileLogo();
			}else if("masque".equals(type)){
				photoFile = templateCard.getPhotoFileMasque();
			}else if("qrCode".equals(type)){
				photoFile = templateCard.getPhotoFileQrCode();
			}
			
			Long size = photoFile.getFileSize();
			String contentType = photoFile.getContentType();
			response.setContentType(contentType);
			response.setContentLength(size.intValue());
			IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
		}
	}
	
	@RequestMapping(value="/freeFieldResults", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody
	public String getResultsFreefield(@RequestParam(value="field") String field) {
		
		Map<String, String> resultsMap = new HashMap<String, String>();
		String flexJsonString = "{}";
		try {
			if(!field.isEmpty()){
				JSONSerializer serializer = new JSONSerializer();
				resultsMap = formService.getFieldsValuesMap(field);
				flexJsonString = serializer.serialize(MapUtils.sortByValue(resultsMap, true));
			}
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données", e);
		}
		
    	return flexJsonString;
	}
	
	@RequestMapping(value="/searchLdap")
	@ResponseBody
	public List<PersonLdap> searchLdap(@RequestParam(value="searchString") String searchString, @RequestParam(required=false) String ldapTemplateName) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		List<PersonLdap> ldapList = new ArrayList<PersonLdap>();
		if(!searchString.trim().isEmpty()) {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
			ldapList = ldapPersonService.searchByCommonName(searchString, ldapTemplateName);
		}
		return ldapList;
   }
}

