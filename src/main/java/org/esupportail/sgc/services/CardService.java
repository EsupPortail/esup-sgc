package org.esupportail.sgc.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.codec.binary.Base64;
import org.esupportail.sgc.dao.*;
import org.esupportail.sgc.domain.*;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.ImageUtils;
import org.esupportail.sgc.tools.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class CardService {	
    
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource 
	private UserInfoService userInfoService;
	
	@Resource
	protected EmailService emailService;	
	
	@Resource
    @Lazy // Avoid circular dependency issues
	protected CardEtatService cardEtatService;	
	
	@Resource
	protected AppliConfigService appliConfigService;	
	
	@Autowired(required = false)
	CardIdsService cardIdsService;
	
	@Resource
	LogService logService;
	
	@Resource
	CardService cardService;

    @Resource
    AppliConfigDaoService appliConfigDaoService;

    @Resource
    BigFileDaoService bigFileDaoService;

    @Resource
    private CardDaoService cardDaoService;

    @Resource
    LogMailDaoService logMailDaoService;

    @Resource
    PayboxTransactionLogDaoService payboxTransactionLogDaoService;

    @Resource
    UserDaoService userDaoService;

    public Card findLastCardByEppnEquals(String eppn) {
		Card lastCard = null;
		List<Card> cards =  cardDaoService.findCardsByEppnEquals(eppn,"requestDate","DESC").getResultList();
		if(!cards.isEmpty()) {
			lastCard = cards.get(0);
		}
		return lastCard;
	}

	public Map<String,String> getPhotoParams(){
		
		return Params.getPhotoParams();
		
	}

	public Object findCard(Long id) {
		Card card = cardDaoService.findCard(id);
		cardEtatService.updateEtatsAvailable4Card(card);
		return card;
	}
	
	
	public boolean deleteMsg(String id){
		
		boolean success = true;
		
		if(appliConfigDaoService.findAppliConfigsByKeyLike(id).getMaxResults()>0){
			// TODO - à virer ??
            appliConfigDaoService.remove(appliConfigDaoService.findAppliConfigsByKeyLike(id).getResultList().get(0));
		}else{
			success = false;
		}
		
		return success;
	}
	
	public boolean displayFormCnil (String type){
		
		boolean displayCnil = false;
		
		if(type!=null && appliConfigService.userTypes2displayFormCnil().contains(type)){
			displayCnil = true;
		}
		
		return displayCnil;
	}
	
	public boolean displayFormCrous (User user){
		return appliConfigService.userTypes2displayFormCrous().contains(user.getUserType());
	}
	
	public boolean isCrousEnabled (User user){
		return appliConfigService.userTypes4isCrousEnabled().contains(user.getUserType());
	}
	
	public boolean displayFormEuropeanCardEnabled(User user){
		
		boolean displayEC = false;
		
		// When escr is accepted ones, we can't unaccept it
		if((user==null || !user.getEuropeanStudentCard()) && user.getUserType()!=null && appliConfigService.userTypes2displayFormEuropeanCard().contains(user.getUserType())){
			displayEC = true;
		}
		
		return displayEC;
	}
	
	public boolean isEuropeanCardEnabled(User user){
		
		boolean enableEC = false;
		
		if(user != null && user.getUserType()!=null && appliConfigService.userTypes4isEuropeanCardEnabled().contains(user.getUserType())){
			enableEC = true;
		}
		
		return enableEC;
	}
	
	public boolean displayFormAdresse (String type){
		
		boolean displayAdresse = false;
		
		if(type!=null && appliConfigService.userTypes2displayFormAdresse().contains(type)){
			displayAdresse = true;
		}
		
		return displayAdresse;
	}
	
	public boolean displayFormRules(String type){
		
		boolean displayRules = false;
		
		if(type!=null && appliConfigService.userTypes2displayFormRules().contains(type)){
			displayRules = true;
		}
		
		return displayRules;
	}
	
	public String getPaymentWithoutCard(String eppn){
		String reference = "";
		User user = userDaoService.findUser(eppn);
		List <PayboxTransactionLog> payboxList =  payboxTransactionLogDaoService.findPayboxTransactionLogsByEppnEquals(eppn).getResultList();

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

	public void sendMailCard(User user, CardActionMessage cardActionMessage, String mailFrom, String mailTo, String sendCC, String subject, String mailMessage){
		if(!sendCC.isEmpty()){
			emailService.sendMessageCc(mailFrom, mailTo, sendCC, subject, mailMessage);
		} else {
			emailService.sendMessage(mailFrom, mailTo, subject, mailMessage);
		}
		LogMail logMail = new LogMail();
		logMail.setCardActionMessage(cardActionMessage);
		logMail.setEppn(user.getEppn());
		logMail.setMailTo(mailTo);
		logMail.setMessage(mailMessage);
		logMail.setSubject(subject);
		logMail.setLogDate(LocalDateTime.now());
		logMailDaoService.persist(logMail);
	}
	
	public String getQrCodeSvg(String value) throws WriterException{
		
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 0);	
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 100, 100, hints);

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" shape-rendering=\"crispEdges\">",
                100, 100));
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>");
        svg.append("<g fill=\"black\">");

		int w = matrix.getWidth();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < w; j++) {
				if (matrix.get(i, j)) {
                    svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"1\" height=\"1\"/>", i, j));
				}
			}
		}

        svg.append("</g></svg>");
		return svg.toString();
	}
	
	@Transactional
	public boolean requestNewCard(Card card, String userAgent, String eppn, HttpServletRequest request, boolean requestUserIsManager){

		boolean emptyPhoto = false;
		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		String navigateur = userAgentUtils.getBrowser().getName();
		String systeme = userAgentUtils.getOperatingSystem().getName();

		card.setEppn(eppn);
		card.setRequestDate(LocalDateTime.now());
		card.setRequestBrowser(navigateur);
		card.setRequestOs(systeme);

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
			log.info("Upload and set file in DB with filesize = " + fileSize);
			if(fileSize > appliConfigService.getFileSizeMax()) {
				log.info(String.format("filesize [%s] too big, we try to resize ...", fileSize));
				bytes = ImageUtils.resizeImage(bytes);
				fileSize = Long.valueOf(Integer.valueOf(bytes.length));
				contentType = ImageUtils.getContentType();
				log.info(String.format("resize to %s OK", fileSize));
			}
			card.getPhotoFile().setContentType(contentType);
			card.getPhotoFile().setFileSize(fileSize);
            bigFileDaoService.setBinaryFile(card.getPhotoFile().getBigFile(), bytes);
            LocalDateTime currentTime = LocalDateTime.now();
			card.getPhotoFile().setSendTime(currentTime);
			if(card.getId() !=null){
				card.setNbRejets(cardDaoService.findCard(card.getId()).getNbRejets());
			} else {
				card.setNbRejets(Long.valueOf(0));
			}

			User user = userDaoService.findUser(eppn);
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
			cardIdsService.generateQrcode4Card(card);
			if(card.getId() ==null) {
                cardDaoService.persist(card);
			}else{
                cardDaoService.merge(card);
			}
			if(user.getId() ==null) {
				userDaoService.persist(user);
			}else{
                userDaoService.merge(user);
			}
			
			String messageLog = "Succès de la demande de carte pour l'utilisateur " +  eppn;
			log.info(messageLog);

			cardEtatService.setCardEtat(card, Etat.NEW, messageLog, null, false, false);
			if(requestUserIsManager){
				logService.log(card.getId(), ACTION.REQUEST_MANAGER, RETCODE.SUCCESS, "", user.getEppn(), null);
			}
		}

		return emptyPhoto;
	}

	@Transactional
	public Card requestRenewalCard(Card card){
		String lockKey = "ESUP-SGC-CARD-requestRenewal-" + card.getId();
		synchronized(lockKey.intern()) {
			cardEtatService.updateEtatsAvailable4Card(card);
			if(card.getEtatsAvailable().contains(Etat.RENEWED)) {
                LocalDateTime currentTime = LocalDateTime.now();
				Card copyCard = new Card();
				copyCard.setEppn(card.getEppn());
				copyCard.setRequestDate(LocalDateTime.now());
				copyCard.setRequestBrowser(card.getRequestBrowser());
				copyCard.setRequestOs(card.getRequestOs());
				cardIdsService.generateQrcode4Card(copyCard);
				PhotoFile photoFile = new PhotoFile();
				photoFile.setFilename(card.getPhotoFile().getFilename());
				photoFile.setContentType(card.getPhotoFile().getContentType());
				photoFile.setFileSize(card.getPhotoFile().getFileSize());
				photoFile.getBigFile().setBinaryFile(card.getPhotoFile().getBigFile().getBinaryFile());
				photoFile.setSendTime(currentTime);
				copyCard.setPhotoFile(photoFile);
				copyCard.setNbRejets(card.getNbRejets());
				User user = userDaoService.findUser(card.getEppn());
				copyCard.setUserAccount(user);
				copyCard.setDueDate(user.getDueDate());
				// on initialise l'état de la carte avec l'état de la carte source pour permettre l'usage de messages de type ENABLED->RENEWED ou encore DISABLED->RENEWED
				copyCard.setEtat(card.getEtat());
				String messageLog = "Demande de renouvellement de carte pour :  " + card.getEppn() + " effectuée.";
				cardEtatService.setCardEtat(copyCard, Etat.RENEWED, messageLog, null, false, false);
				cardDaoService.persist(copyCard);
				log.info(messageLog);
				return copyCard;
			} else {
				log.warn(String.format("card %s (%s) can't be renewed", card.getCsn(), card.getEppn()));
			}
			return null;
		}
	}
}
