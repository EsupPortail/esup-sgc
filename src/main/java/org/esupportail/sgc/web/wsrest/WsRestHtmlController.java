package org.esupportail.sgc.web.wsrest;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Transactional
@RequestMapping("/wsrest/view")
@Controller
public class WsRestHtmlController {
	
	@Resource
	ManagerCardController managerCardController;
	
	@Resource
	CardEtatService cardEtatService;
	
	@RequestMapping(value="/{eppn}/cardInfo", method = RequestMethod.GET)
	public String viewCardInfo(@PathVariable String eppn, Model uiModel) {
		
		User user = User.findUser(eppn);
		if(user != null){
			Hibernate.initialize(user.getCards());
			uiModel.addAttribute("steps", cardEtatService.getTrackingSteps());
			uiModel.addAttribute("user", user);
			uiModel.addAttribute("payboxList", PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList());
		}
		uiModel.addAttribute("user", user);
		return "user/card-info";
	}

	@RequestMapping(value="/{eppn}/userInfo", method = RequestMethod.GET)
	public String viewUserInfo(@PathVariable String eppn, Model uiModel) {
		
        User user = User.findUser(eppn);
        if(user != null){
	        if(!user.getCards().isEmpty()){
	        	for(Card cardItem : user.getCards()){
	        		cardEtatService.updateEtatsAvailable4Card(cardItem);
	        		cardItem.setIsPhotoEditable(cardEtatService.isPhotoEditable(cardItem));
	        	}
	        	uiModel.addAttribute("currentCard", user.getCards().get(0));
	        	
	        }
	       
        }
        uiModel.addAttribute("user", user);
		return "manager/show";
	}

	/**
	 * Example to use it :
	 * curl https://esup-sgc.univ-ville.fr/wsrest/view/{cardId}/card-b64.html"
	 *
	 * wget -4 'http://localhost:8080/wsrest/view/3083564/card-b64.html?type=black' -O card-black-b64.html
	 * wget -4 'http://localhost:8080/wsrest/view/3083564/card-b64.html?type=color' -O card-color-b64.html
	 * google-chrome --headless --disable-gpu --print-to-pdf=card-black.pdf card-black-b64.html
	 * google-chrome --headless --disable-gpu --print-to-pdf=card-color.pdf card-color-b64.html
	 * convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 card-black.pdf card-black.bmp
	 * convert -resize 1016x648 -gravity center -extent 1016x648 -density 600 card-color.pdf card-color.bmp
	 */
	@RequestMapping(value="/{cardId}/card-b64.html", method = RequestMethod.GET)
	public String cardHtml(@PathVariable Long cardId, @RequestParam(required = false) String type, Model uiModel) throws SQLException, IOException, WriterException {
		Card card = Card.findCard(cardId);

		byte[] photoBytes = card.getPhotoFile().getBigFile().getBinaryFileasBytes();
		String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);

		TemplateCard templateCard = card.getTemplateCard();
		if(templateCard == null) {
			templateCard = card.getUserAccount().getTemplateCard();
		}

		byte[] logoBytes = templateCard.getPhotoFileLogo().getBigFile().getBinaryFileasBytes();
		String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

		byte[] masqueBytes = templateCard.getPhotoFileMasque().getBigFile().getBinaryFileasBytes();
		String masqueBase64 = Base64.getEncoder().encodeToString(masqueBytes);

		String value = card.getQrcode();
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 0);
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 100, 100, hints);
		ByteArrayOutputStream qrcodeOut = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(matrix, "PNG", qrcodeOut);
		byte[] qrcodeBytes = qrcodeOut.toByteArray();
		String qrcodeBase64 = Base64.getEncoder().encodeToString(qrcodeBytes);

		uiModel.addAttribute("photoBase64", photoBase64);
		uiModel.addAttribute("logoBase64", logoBase64);
		uiModel.addAttribute("masqueBase64", masqueBase64);
		uiModel.addAttribute("qrcodeBase64", qrcodeBase64);
		uiModel.addAttribute("card", card);
		uiModel.addAttribute("type", type);
		return "manager/print-card-b64";
	}

}


