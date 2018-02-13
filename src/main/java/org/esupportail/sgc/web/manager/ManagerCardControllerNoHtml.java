package org.esupportail.sgc.web.manager;

import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

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
	
	@RequestMapping(value="/photo/{cardId}")
	@Transactional
	public void writePhotoToResponse(@PathVariable Long cardId, HttpServletResponse response) throws IOException, SQLException {
		Card card = Card.findCard(cardId);
		PhotoFile photoFile = card.getPhotoFile();
		Long size = photoFile.getFileSize();
		String contentType = photoFile.getContentType();
		response.setContentType(contentType);
		response.setContentLength(size.intValue());
		IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	}
	
	@RequestMapping(value="/QRCode")
	@ResponseBody
	@Transactional
	public ResponseEntity<String>  getQRCode(@RequestParam Long cardId, HttpServletResponse response) throws WriterException, IOException {

		Card card = Card.findCard(cardId);
		String value = card.getQrcode();
		
		if(value != null) {
			
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
	public String filtrerAdresse(@RequestParam(value="etat") String etat, @RequestParam(value="tabType") String tabType) {
		
		String flexJsonString = "Aucune donnée récupérable";
		try {
			List<String> adresses = userInfoService.getListAdresses(tabType, etat);
			JSONSerializer serializer = new JSONSerializer();
			flexJsonString = serializer.serialize(adresses);
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données", e);
		}
		
    	return flexJsonString;
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
		RightHolder rightHolder = crousService.getRightHolder(eppn);
		uiModel.addAttribute("rightHolder", rightHolder);
		return "manager/rightHolder";
	}
}

