package org.esupportail.sgc.services;

import java.awt.Color;
import java.awt.Dimension;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


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
	
	@Resource
	LogService logService;
	
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
		
		if(appliConfigService.displayFormCnil().contains(type)){
			displayCnil = true;
		}
		
		return displayCnil;
	}
	
	public boolean displayFormCrous (String eppn, String type){
		
		boolean displayCrous = false;
		
		User user = User.findUser(eppn);
		
		// When crous is accepted ones, we can't unaccept it
		if((user==null || !user.getCrous()) && appliConfigService.displayFormCrous().contains(type)){
			displayCrous = true;
		}
		
		return displayCrous;
	}
	
	public boolean displayFormAdresse (String type){
		
		boolean displayAdresse = false;
		
		if(appliConfigService.displayFormAdresse().contains(type)){
			displayAdresse = true;
		}
		
		return displayAdresse;
	}
	
	public boolean displayFormRules(String type){
		
		boolean displayRules = false;
		
		if(appliConfigService.displayFormRules().contains(type)){
			displayRules = true;
		}
		
		return displayRules;
	}
	
	public boolean displayFormEuropeanCard (String type){
		
		boolean displayFormEuropeanCard = false;
		
		if(appliConfigService.displayFormEuropeanCard().contains(type)){
			displayFormEuropeanCard = true;
		}
		
		return displayFormEuropeanCard;
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
	

}
