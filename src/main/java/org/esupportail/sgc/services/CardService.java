package org.esupportail.sgc.services;

import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.codec.binary.Base64;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import eu.bitwalker.useragentutils.UserAgent;


@Service
public class CardService {	
    
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource 
	private UserInfoService userInfoService;
	
	@Resource
	protected EmailService emailService;	
	
	@Resource
	protected CardEtatService cardEtatService;	
	
	@Resource
	protected AppliConfigService appliConfigService;	
	
	@Autowired(required = false)
	CardIdsService cardIdsService;
	
	@Resource
	LogService logService;
	
	@Resource
	CardService cardService;
	
	public Card findLastCardByEppnEquals(String eppn) {
		Card lastCard = null;
		List<Card> cards =  Card.findCardsByEppnEquals(eppn,"requestDate","DESC").getResultList();
		if(!cards.isEmpty()) {
			lastCard = cards.get(0);
		}
		return lastCard;
	}

	public Map<String,String> getPhotoParams(){
		
		return Params.getPhotoParams();
		
	}

	public Object findCard(Long id) {
		Card card = Card.findCard(id);
		cardEtatService.updateEtatsAvailable4Card(card);
		return card;
	}
	
	
	public boolean deleteMsg(String id){
		
		boolean success = true;
		
		if(AppliConfig.findAppliConfigsByKeyLike(id).getMaxResults()>0){
			// TODO - à virer ??
			AppliConfig.findAppliConfigsByKeyLike(id).getResultList().get(0).remove();
		}else{
			success = false;
		}
		
		return success;
	}
	
	public boolean displayFormCnil (String type){
		
		boolean displayCnil = false;
		
		if(type!=null && appliConfigService.displayFormCnil().contains(type)){
			displayCnil = true;
		}
		
		return displayCnil;
	}
	
	public boolean displayFormCrous (User user){
		
		boolean displayCrous = false;
		
		// When crous is accepted ones, we can't unaccept it
		if((user==null || !user.getCrous()) && user.getUserType()!=null && appliConfigService.displayFormCrous().contains(user.getUserType())){
			displayCrous = true;
		}
		
		return displayCrous;
	}
	
	public boolean isCrousEnabled (User user){
		
		boolean enableCrous = false;
		
		if((user==null || !user.getCrous()) && user.getUserType()!=null && appliConfigService.isCrousEnabled().contains(user.getUserType())){
			enableCrous = true;
		}
		
		return enableCrous;
	}
	
	public boolean displayFormEuropeanCardEnabled(User user){
		
		boolean displayEC = false;
		
		if((user==null || !user.getEuropeanStudentCard()) && user.getUserType()!=null && appliConfigService.displayFormEuropeanCard().contains(user.getUserType())){
			displayEC = true;
		}
		
		return displayEC;
	}
	
	public boolean isEuropeanCardEnabled(User user){
		
		boolean enableEC = false;
		
		if((user==null || !user.getEuropeanStudentCard()) && user.getUserType()!=null && appliConfigService.isEuropeanCardEnabled().contains(user.getUserType())){
			enableEC = true;
		}
		
		return enableEC;
	}
	
	public boolean displayFormAdresse (String type){
		
		boolean displayAdresse = false;
		
		if(type!=null && appliConfigService.displayFormAdresse().contains(type)){
			displayAdresse = true;
		}
		
		return displayAdresse;
	}
	
	public boolean displayFormRules(String type){
		
		boolean displayRules = false;
		
		if(type!=null && appliConfigService.displayFormRules().contains(type)){
			displayRules = true;
		}
		
		return displayRules;
	}
	
	public String getPaymentWithoutCard(String eppn){
		String reference = "";
		User user = User.findUser(eppn);
		List <PayboxTransactionLog> payboxList =  PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList();

		if(!payboxList.isEmpty()){
			List<String> references = new ArrayList<String>();
			for(PayboxTransactionLog log : payboxList){
				if ("00000".equals(log.getErreur())) {
					references.add(log.getReference());
				}
			}			
			List <Card> cards = user.getCards();
			if(!cards.isEmpty()){
				for(Card card : cards){
					if(card.getPayCmdNum()!= null && !card.getPayCmdNum().isEmpty()){
						if(references.contains(card.getPayCmdNum())){
							references.remove(card.getPayCmdNum());
						}
					}
				}

			}
			if(!references.isEmpty()){
				reference = references.get(0);
			}
		}
		return reference;
		
	}

	public void sendMailCard(String mailFrom, String mailTo, String sendCC, String subject, String mailMessage){
		if(!sendCC.isEmpty()){
				emailService.sendMessageCc(mailFrom, mailTo, sendCC, subject, mailMessage);
		}else{
			emailService.sendMessage(mailFrom, mailTo, subject, mailMessage);
		}
	}
	
	public String getQrCodeSvg(String value) throws SVGGraphics2DIOException, WriterException{
		
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 0);	
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		
		BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 100, 100, hints); 
		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setBackground(Color.RED);
		svgGenerator.setColor(Color.BLACK);
		svgGenerator.setSVGCanvasSize(new Dimension(100,100));
		
		
		int w = matrix.getWidth();
		svgGenerator.setSVGCanvasSize(new Dimension(w,w));
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < w; j++) {
				if (matrix.get(i, j)) {
					svgGenerator.fillRect(i, j, 1, 1);
				}
			}
		}

		boolean useCSS = false; // we want to use CSS style attributes
		Writer svgWriter = new StringWriter();
		svgGenerator.stream(svgWriter, useCSS);
		return svgWriter.toString();
	}
	
	@Transactional
	public boolean requestNewCard(Card card, String userAgent, String eppn, HttpServletRequest request, boolean fromLdap){

		boolean emptyPhoto = false;
		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		String navigateur = userAgentUtils.getBrowser().getName();
		String systeme = userAgentUtils.getOperatingSystem().getName();

		card.setEppn(eppn);
		card.setRequestDate(new Date());
		card.setRequestBrowser(navigateur);
		card.setRequestOs(systeme);
		cardIdsService.generateQrcode4Card(card);

		if (card.getPhotoFile().getImageData().isEmpty()) {
			emptyPhoto = true;
			log.info("Aucun fichier sélectionné");
		} else {
			String encoding = cardService.getPhotoParams().get("encoding");
			int contentStartIndex = card.getPhotoFile().getImageData().indexOf(encoding) + encoding.length();
			byte[] bytes = Base64.decodeBase64(card.getPhotoFile().getImageData().substring(contentStartIndex));  
			String filename = eppn.concat(cardService.getPhotoParams().get("extension"));
			Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
			String contentType = cardService.getPhotoParams().get("contentType");
			log.info("Try to upload file '" + filename + "' with size=" + fileSize + " and contentType=" + contentType);
			card.getPhotoFile().setFilename(filename);
			card.getPhotoFile().setContentType(contentType);
			card.getPhotoFile().setFileSize(fileSize);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			log.info("Upload and set file in DB with filesize = " + fileSize);
			card.getPhotoFile().getBigFile().setBinaryFileStream(inputStream, fileSize);
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
			card.getPhotoFile().setSendTime(currentTime);
			if(card.getId() !=null){
				card.setNbRejets(card.findCard(card.getId()).getNbRejets());
			} else {
				card.setNbRejets(Long.valueOf(0));
			}

			User user = User.findUser(eppn);
			if(user == null){
				user = new User();
				user.setEppn(card.getEppn());
			}
			card.setUserAccount(user);
			card.setDueDate(user.getDueDate());
			if(card.getCrousTransient()!=null && card.getCrousTransient()) {
				user.setCrous(true);
			}
			if(card.getEuropeanTransient()!=null && card.getEuropeanTransient()) {
				user.setEuropeanStudentCard(true);
			}
			if(card.getDifPhotoTransient() != null) {
				user.setDifPhoto(card.getDifPhotoTransient());
			}
			String reference = cardService.getPaymentWithoutCard(eppn);
			if(!reference.isEmpty()){
				card.setPayCmdNum(reference);
			}
			userInfoService.setAdditionalsInfo(user, null);
			if(card.getId() ==null) {
				card.persist();
			}else{
				card.merge();
			}
			if(user.getId() ==null) {
				user.persist();
			}else{
				user.merge();
			}
			String messageLog = "Succès de la demande de carte pour l'utilisateur " +  eppn;
			log.info(messageLog);

			cardEtatService.setCardEtat(card, Etat.NEW, messageLog, null, false, false);
			if(fromLdap){
				logService.log(card.getId(), ACTION.REQUEST_FROM_LDAP, RETCODE.SUCCESS, "", user.getEppn(), null);
			}
		}

		return emptyPhoto;
	}

	@Transactional
	public Card requestRenewalCard(Card card){

		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		Card copyCard = new Card();
		copyCard.setEppn(card.getEppn());
		copyCard.setRequestDate(new Date());
		copyCard.setRequestBrowser(card.getRequestBrowser());
		copyCard.setRequestOs(card.getRequestOs());
		cardIdsService.generateQrcode4Card(copyCard);
		PhotoFile photoFile = new PhotoFile();
		photoFile.setFilename(card.getPhotoFile().getFilename());
		photoFile.setContentType(card.getPhotoFile().getContentType());
		photoFile.setFileSize(card.getPhotoFile().getFileSize());
		photoFile.setBigFile(card.getPhotoFile().getBigFile());
		photoFile.getBigFile().setBinaryFile(card.getPhotoFile().getBigFile().getBinaryFile());
		photoFile.setSendTime(currentTime);
		copyCard.setPhotoFile(photoFile);
		copyCard.setNbRejets(card.getNbRejets());
		User user = User.findUser(card.getEppn());
		copyCard.setUserAccount(user);
		copyCard.setDueDate(user.getDueDate());
		String messageLog = "Demande de renouvellement de carte pour :  " + card.getEppn() + " effectuée.";
		cardEtatService.setCardEtat(copyCard, Etat.RENEWED, messageLog, null, false, false);
		copyCard.persist();
		log.info(messageLog);
		

		return copyCard;
	}
}
