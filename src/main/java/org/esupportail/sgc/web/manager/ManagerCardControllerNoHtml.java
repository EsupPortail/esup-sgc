package org.esupportail.sgc.web.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.CrousSmartCard;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.FormService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.ldap.LdapPersonService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.MemoryMapStringEncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
	MemoryMapStringEncodingUtils urlEncodingUtils;
	
	@RequestMapping(value="/photo/{cardId}")
	@Transactional
	public ResponseEntity writePhotoToResponse(@PathVariable Long cardId, HttpServletResponse response) throws IOException, SQLException {
		Card card = Card.findCard(cardId);
		PhotoFile photoFile = card.getPhotoFile();
		Long size = photoFile.getFileSize();
		String contentType = photoFile.getContentType();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers.setContentLength(size.intValue());
		return new ResponseEntity(photoFile.getBigFile().getBinaryFileasBytes(), headers, HttpStatus.OK);
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
				}
			}else{
				if(photoFile != null){
					Long size = photoFile.getFileSize();
					String contentType = photoFile.getContentType();
					response.setContentType(contentType);
					response.setContentLength(size.intValue());
					IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
					log.debug("code-barres is used for this card " + cardId);
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
			eppnList = Card.findCardsByEppnLike(searchString, "eppn", "ASC").getResultList();
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
			List<String> adresses = userInfoService.getListAdresses(tabType, etat);
			adressesMap = urlEncodingUtils.getMapWithEncodedString(adresses);
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données", e);
		}
		
    	return adressesMap;
	}
	
	@RequestMapping(value="/getCrousRightHolder", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody 
	@Transactional
    public RightHolder getCrousRightHolder(@RequestParam String eppn) {
		return crousService.getRightHolder(eppn);
	}
	
	@RequestMapping(value="/getCrousRightHolderHtmlPart")
	@Transactional
    public String getCrousRightHolderHtmlPart(@RequestParam String eppn, Model uiModel) {
		try {
			RightHolder rightHolder = crousService.getRightHolder(eppn);
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
		
		String flexJsonString = "Aucune donnée récupérable";
		try {
			if(!field.isEmpty()){
				List<String> results = formService.getField1List(field);
				JSONSerializer serializer = new JSONSerializer();
				flexJsonString = serializer.serialize(results);
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
			ldapList = ldapPersonService.searchByCommonName(searchString, ldapTemplateName);
		}
		return ldapList;
   }
}

