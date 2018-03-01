package org.esupportail.sgc.web.user;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.security.ShibAuthenticatedUserDetailsService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.TemplateCardService;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.paybox.PayBoxForm;
import org.esupportail.sgc.services.paybox.PayBoxService;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.bitwalker.useragentutils.UserAgent;

@Transactional
@RequestMapping("/user")
@Controller
public class UserCardController {

	public final Logger log = LoggerFactory.getLogger(getClass());
	
	public final static String SUCCESS_MSG ="user.msg.success.";
	public final static String ERROR_MSG ="user.msg.error.";
	public final static String WARNING_MSG ="user.msg.warning.";
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "user";
	} 
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpUser();
	}
	
	@ModelAttribute("livraison")
	public String getLivraisonConfig() {
		return appliConfigService.getModeLivraison();
	}

	@Resource 
	UserInfoService userInfoService;

	@Resource
	CardService cardService;
	
	@Resource
	CardEtatService cardEtatService;

	@Resource
	PayBoxService payBoxService;
	
	@Resource
	AppliConfigService appliConfigService;	
	
	@Autowired
    MessageSource messageSource;
	
	@Autowired
	List<ExtUserInfoService> extUserInfoServices;
	
	@Resource
	LogService logService;
	
	@Resource
	UserService userService;
	
	@Resource
	ShibAuthenticatedUserDetailsService shibService;
	
	@Resource
	CardIdsService cardIdsService;
	
	@Resource
	ExternalCardService externalCardService;
	
	@Resource
	TemplateCardService templateCardService;
	
	@RequestMapping
	public String index(Locale locale, HttpServletRequest request, Model uiModel, @RequestHeader("User-Agent") String userAgent) throws UnsupportedEncodingException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		uiModel.addAttribute("configUserMsgs", getConfigMsgsUser());
		if(userService.isFirstRequest(eppn)) {
			Card externalCard = externalCardService.getExternalCard(eppn, request);
			if(externalCard != null) {
				return viewExternalCardRequestForm(uiModel, request, externalCard);
			} else {
				return viewCardRequestForm(uiModel, request, userAgent);
			}
		} else {
			return viewCardInfo(locale, uiModel, request);
		}
	}
	
	private String viewExternalCardRequestForm(Model uiModel, HttpServletRequest request, Card externalCard) {
		uiModel.addAttribute("externalCard", externalCard);
		try {
			byte[] externalCardPhoto = IOUtils.toByteArray(externalCard.getPhotoFile().getBigFile().getBinaryFile().getBinaryStream());
			uiModel.addAttribute("externalCardPhoto", java.util.Base64.getEncoder().encodeToString(externalCardPhoto));
		} catch (IOException | SQLException e) {
			log.warn("Exception when retrieving photo from external card", e);
		}
		return "user/external-card-request";
	}
	
	
	@RequestMapping(value="/externalCardEnable", method = RequestMethod.POST)
	public String enableExternalCard(Model uiModel, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		try {
			Card externalCard = externalCardService.importExternalCard(eppn, request);
			cardEtatService.setCardEtat(externalCard, Etat.ENABLED, "Importation d'une Léocarte extérieure", "Importation d'une Léocarte extérieure", false, false);
			redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "enable");
		} catch (Exception e) {
			log.error("problème lors de l'activation de la carte extérieure de " + eppn, e);
			redirectAttributes.addFlashAttribute("messageError", ERROR_MSG + "enable");
		}
		return "redirect:/user";
	}

	@RequestMapping(value="/card-request-form")
	public String viewCardRequestForm(Model uiModel, HttpServletRequest request, @RequestHeader("User-Agent") String userAgent) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		uiModel.addAttribute("user", user);
		Long id = Long.valueOf("-1");
		if(!user.getCards().isEmpty()){
			id = user.getCards().get(0).getId();
		}
		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		
		uiModel.addAttribute("deviceType", userAgentUtils.getOperatingSystem().getDeviceType());
		uiModel.addAttribute("templateCard", templateCardService.getTemplateCard(user.getEppn()));
		uiModel.addAttribute("configUserMsgs", getConfigMsgsUser());
		uiModel.addAttribute("lastId", id);
		uiModel.addAttribute("isEsupSgcUser", userService.isEsupSgcUser(eppn));
		uiModel.addAttribute("isISmartPhone",  userService.isISmartphone(userAgent));
		Map<String, Boolean> displayFormParts = displayFormParts(eppn, user.getUserType());
		log.debug("displayFormParts for " + eppn + " : " + displayFormParts);
		uiModel.addAttribute("displayFormParts", displayFormParts);
		return "user/card-request";
	}
	
	
	@RequestMapping(value="/card-payment")
	public String viewPaymentCardRequestForm(Locale locale, Model uiModel, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		if(! userService.isFreeRenewal(eppn)){
			uiModel.addAttribute("isFreeRenewal",  userService.isFreeRenewal(eppn));
			User user = User.findUser(eppn);
			PayBoxForm payBoxForm = payBoxService.getPayBoxForm(eppn, user.getEmail(), appliConfigService.getMontantRenouvellement());
			uiModel.addAttribute("payBoxForm", payBoxForm);
			uiModel.addAttribute("displayPayboxForm", true);
			return  "user/card-payment";
		} else {
			return viewCardInfo(locale, uiModel, request);
		}
	}
	
	@RequestMapping(value="/rejectedCase")
	public String rejectedCardForm(@RequestParam("id") Long id, Model uiModel, HttpServletRequest request, @RequestHeader("User-Agent") String userAgent) {
		uiModel.addAttribute("configUserMsgs", getConfigMsgsUser());
		uiModel.addAttribute("id", id);
		uiModel.addAttribute("isRejected", true);
		return viewCardRequestForm(uiModel, request, userAgent);
	}	
	
	@RequestMapping(value="/card-disable")
	public String viewDisableCardForm(@RequestParam("id") Long id, Model uiModel, HttpServletRequest request) {
		uiModel.addAttribute("motifsList", getMotifsList());
		uiModel.addAttribute("id", id);
		return "user/card-disable";
	}
	
	@RequestMapping(value="/disable", method = RequestMethod.POST)
	public String disableCard(@RequestParam("id") Long id, @RequestParam(value="motif") MotifDisable motif, Model uiModel, final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card card = Card.findCard(id);
		if(card != null && card.getEppn().equals(eppn)){
			try {
				cardEtatService.disableCardWithMotif(card, motif, false);
				redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "disable");
			} catch (Exception e) {
				log.error("problème lors de l'invalidation de la carte de " + eppn, e);
				redirectAttributes.addFlashAttribute("messageError", ERROR_MSG + "disable");
			}
		} else {
			log.info("Aucune carte valide trouvée pour invalidation");
			redirectAttributes.addFlashAttribute("messageInfo", WARNING_MSG + "disable");
		}

		return "redirect:/user";
	}
	
	@RequestMapping(value="/enable", method = RequestMethod.POST)
	public String enableCard(@RequestParam("id") Long id, Model uiModel, final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card card = Card.findCard(id);
		if(card != null && card.getEppn().equals(eppn)){
			try {
				cardEtatService.setCardEtat(card, Etat.ENABLED, "Réactivation de la carte par l'utilisateur", null, false, false);
				redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "enable");
			} catch (Exception e) {
				log.error("problème lors de la réactivation de la carte de " + eppn, e);
				redirectAttributes.addFlashAttribute("messageError", ERROR_MSG + "enable");
			}
		} else{
			log.info("Aucune carte valide trouvée pour activation");
			redirectAttributes.addFlashAttribute("messageInfo", WARNING_MSG + "enable");
		}

		return "redirect:/user";
	}
	
	public String viewCardInfo(Locale locale, Model uiModel, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card lastCard =  Card.findCardsByEppnEquals(eppn,"requestDate","DESC").getResultList().get(0);
		User user = User.findUser(eppn);
		
		/*DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		String formattedDate = df.format(lastCard.getDateEtat());
		
		
		String messageCode = "user.card-info.defaultMessage";	
		if(Etat.REJECTED.equals(lastCard.getEtat())) {
			messageCode = "user.card-info.rejectedMessage";	
		} else if(Etat.ENABLED.equals(lastCard.getEtat())) {
			messageCode = "user.card-info.sentMessage";	
		} else if(Etat.DISABLED.equals(lastCard.getEtat())) {
			messageCode = "user.card-info.disabledMessage";	
		}else{
			messageCode = "user.card-info.newMessage";	
		}
		String message = messageSource.getMessage(messageCode, new String[] {formattedDate}, locale);
		uiModel.addAttribute("message", message);
		uiModel.addAttribute("comment", lastCard.getCommentaire());
		*/
		uiModel.addAttribute("steps", cardEtatService.getTrackingSteps());
		uiModel.addAttribute("user", user);
		uiModel.addAttribute("payboxList", PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList());
		uiModel.addAttribute("montant", appliConfigService.getMontantRenouvellement());
		uiModel.addAttribute("displayFormParts", displayFormParts(eppn, user.getUserType()));
		return "user/card-info";
	}

	
	@RequestMapping(method = RequestMethod.POST)
	public String cardRequest(@Valid Card card, BindingResult bindingResult, Model uiModel, 
			@RequestHeader("User-Agent") String userAgent, HttpServletRequest request, final RedirectAttributes redirectAttributes) throws IOException {	
		if (bindingResult.hasErrors()) {
				log.warn(bindingResult.getAllErrors().toString());
			return "redirect:/";
		}	

		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String eppn = auth.getName();
		
		// TODO synchronized
		synchronized (eppn.intern()) {
			
			// check rights  sur Sring est global - à éviter
			if(userService.isFirstRequest(eppn) || userService.isFreeRenewal(eppn) ||  userService.isPaidRenewal(eppn) || cardEtatService.hasRejectedCard(eppn)) {
			
				if(!cardEtatService.hasNewCard(eppn)){
					UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
					String navigateur = userAgentUtils.getBrowser().getName();
					String systeme = userAgentUtils.getOperatingSystem().getName();
			
					// TODO : use cardEtatService.setCardEtat !
					card.setEppn(eppn);
					card.setRequestDate(new Date());
					card.setRequestBrowser(navigateur);
					card.setRequestOs(systeme);
					cardIdsService.generateQrcode4Card(card);
			
					if (card.getPhotoFile().getImageData().isEmpty()) {
						log.info("Aucun fichier sélectionné");
						redirectAttributes.addFlashAttribute("messageInfo", WARNING_MSG + "leocarte_emptyfile");
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
							card.merge();
						} else {
							card.setNbRejets(Long.valueOf(0));
							card.persist();
						}
						
						User user = User.findUser(eppn);
						card.setUserAccount(user);
						card.setDueDate(user.getDueDate());
						if(card.getCrousTransient()!=null && card.getCrousTransient()) {
							user.setCrous(true);
							userInfoService.setAdditionalsInfo(user, request);
						}
						if(card.getEuropeanTransient()!=null && card.getEuropeanTransient()) {
							user.setEuropeanStudentCard(true);
							userInfoService.setAdditionalsInfo(user, request);
						}
						if(card.getDifPhotoTransient() != null) {
							user.setDifPhoto(card.getDifPhotoTransient());
						}
						String reference = cardService.getPaymentWithoutCard(eppn);
						if(!reference.isEmpty()){
							card.setPayCmdNum(reference);
						}
						user.merge();
						card.merge();
						logService.log(card.getId(), ACTION.DEMANDE, RETCODE.SUCCESS, "", eppn, null);
						log.info("Succès de la demande de carte pour l'utilisateur " +  eppn);
						
						// TODO : use cardEtatService.setCardEtat !
						cardEtatService.sendMailInfo(null, Etat.NEW, user, null, false);
						
						redirectAttributes.addFlashAttribute("messageSuccess", "success_leocarte_upload");
					}
				}
			} else {
				log.warn(eppn + " tried to request card but he has no rights to do it." );
			}
		}
		return "redirect:/user";
	}
	
	public Map<String,Boolean> displayFormParts(String eppn, String type){
		
		Map<String,Boolean> displayFormParts = new HashMap<String, Boolean>();
		
		displayFormParts.put("displayCnil", cardService.displayFormCnil(type));
		displayFormParts.put("displayCrous", cardService.displayFormCrous(eppn, type));
		displayFormParts.put("displayRules", cardService.displayFormRules(type));
		displayFormParts.put("displayAdresse", cardService.displayFormAdresse(type));		
		displayFormParts.put("isFirstRequest", userService.isFirstRequest(eppn));
		displayFormParts.put("displayForm",  userService.displayForm(eppn));
		displayFormParts.put("displayRenewalForm",  userService.displayRenewalForm(eppn));
		displayFormParts.put("isFreeRenewal",  userService.isFreeRenewal(eppn));
		displayFormParts.put("isPaidRenewal",  userService.isPaidRenewal(eppn));
		displayFormParts.put("canPaidRenewal",  userService.canPaidRenewal(eppn));
		displayFormParts.put("hasDeliveredCard",  userService.hasDeliveredCard(eppn));
		displayFormParts.put("displayEuropeanCard", cardService.displayFormEuropeanCard(type));
		return displayFormParts;
		
	}
	
	public List<String> getMotifsList(){
		List<String>  motifsList = new ArrayList<String>();
		 for (MotifDisable motif : MotifDisable.values()) { 
			 motifsList.add(motif.name());
		 }
		return motifsList;
		
	}
	
	@RequestMapping(value="/photo")
	public void getPhoto(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		String eppn = SecurityContextHolder.getContext().getAuthentication().getName();
		Card lastCard = cardService.findLastCardByEppnEquals(eppn);
		if(lastCard !=null){
			PhotoFile photoFile = lastCard.getPhotoFile();
			Long size = photoFile.getFileSize();
			String contentType = photoFile.getContentType();
			response.setContentType(contentType);
			response.setContentLength(size.intValue());
			IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
		}
	}
	
	@RequestMapping(value="/photo/{cardId}")
	public void getPhoto(@PathVariable Long cardId, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
		Card card = Card.findCard(cardId);
		
		if(card.getEppn()!=null && eppn.equals(card.getEppn())) {
			PhotoFile photoFile = card.getPhotoFile();
			Long size = photoFile.getFileSize();
			String contentType = photoFile.getContentType();
			response.setContentType(contentType);
			response.setContentLength(size.intValue());
			IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/payboxOk")
	public String getPaybox(@RequestParam String montant, @RequestParam String reference, @RequestParam(required = false) String auto, @RequestParam String erreur, 
			@RequestParam String idtrans, @RequestParam String signature, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		String ip = request.getRemoteAddr();
		String queryString = request.getQueryString();
		if (payBoxService.payboxCallback(montant, reference, auto, erreur, idtrans, signature, queryString, ip)) {
 			String eppn = SecurityContextHolder.getContext().getAuthentication().getName();
 			User user = User.findUser(eppn);
 			try {
 				cardService.sendMailCard(appliConfigService.getNoReplyMsg(),user.getEmail() ,appliConfigService.getListePpale(), 
 						appliConfigService.getSubjectAutoCard().concat(" -- ".concat(user.getEppn())), appliConfigService.getPayboxMessage());
 			} catch (Exception e) {
 					log.error("Erreur lors de l'envoi du mail pour la carte de :" + user.getEppn(), e);
 			}
			redirectAttributes.addFlashAttribute("messageSuccess", SUCCESS_MSG + "paybox");
		} 
		return "redirect:/user";
	}
	
	@RequestMapping(value="/difPhoto")
	public String updateDifPhoto(@RequestParam("diffusionphoto") boolean diffusionphoto, @RequestParam("eppn") String eppn) {
		// TODO : remove  @RequestParam("eppn") String eppn
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		eppn = auth.getName();
		User user = User.findUser(eppn);
		Boolean oldDifPhoto = user.getDifPhoto();
		user.setDifPhoto(diffusionphoto);
		user.merge();
		logService.log(user.getCards().get(0).getId(), ACTION.DIFPHOTO, RETCODE.SUCCESS, oldDifPhoto == null ? "null" : oldDifPhoto + " --> " + diffusionphoto, user.getEppn(), null);
		return "redirect:/user";
	}
	
	public HashMap<String,String> getConfigMsgsUser(){
		
		HashMap<String,String> getConfigMsgsUser = new HashMap<String, String>();
		
		getConfigMsgsUser.put("helpMsg", appliConfigService.getUserHelpMsg());
		getConfigMsgsUser.put("freeRenewalMsg", appliConfigService.getUseFreeRenewalMsg());
		getConfigMsgsUser.put("paidRenewalMsg", appliConfigService.getUserPaidRenewalMsg());
		getConfigMsgsUser.put("canPaidRenewalMsg", appliConfigService.getUserCanPaidRenewalMsg());
		getConfigMsgsUser.put("newCardMsg", appliConfigService.getNewCardlMsg());
		getConfigMsgsUser.put("checkedOrEncodedCardMsg", appliConfigService.getCheckedOrEncodedCardMsg());
		getConfigMsgsUser.put("rejectedCardMsg", appliConfigService.getRejectedCardMsg());
		getConfigMsgsUser.put("enabledCardMsg", appliConfigService.getEnabledCardMsg());
		getConfigMsgsUser.put("enabledCardPersMsg", appliConfigService.getEnabledCardPersMsg());
		getConfigMsgsUser.put("userFormRejectedMsg", appliConfigService.getUserFormRejectedMsg());
		getConfigMsgsUser.put("userFormRules", appliConfigService.getUserFormRules());
		getConfigMsgsUser.put("userFreeForcedRenewal", appliConfigService.getUserFreeForcedRenewal());
		getConfigMsgsUser.put("userTipMsg", appliConfigService.getUserTipMsg());
		
		return getConfigMsgsUser;
		
	}
	
	@RequestMapping(value="/forcedFreeRenewal", method = RequestMethod.POST)
	public String setForcedFreeRenewal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		if(shibService.isPreviousAdmin(auth)){
			User user = User.findUser(eppn);
			user.setRequestFree(true);
			user.merge();
			logService.log(user.getCards().get(0).getId(), ACTION.FORCEDFREEREQUEST, RETCODE.SUCCESS, "", user.getEppn(), null);
		}
		return "redirect:/user";
	}
	
	@RequestMapping(value="/deliver/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String deliver(@PathVariable("cardId") Long cardId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card card = Card.findCard(cardId);
		if(eppn.equals(card.getEppn())) {
			card.setDeliveredDate(new Date());
			card.merge();
			logService.log(card.getId(), ACTION.USER_DELIVERY, RETCODE.SUCCESS, "", card.getEppn(), null);
		}
		return "redirect:/user";
	}
	
	@RequestMapping(value="/enableCrous", method=RequestMethod.POST)
	public String enableCrous(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		user.setCrous(true);
		user.merge();
		logService.log(user.getCards().get(0).getId(), ACTION.ENABLECROUS, RETCODE.SUCCESS, "", eppn, null);
		redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "crous");
		return "redirect:/user";
	}
	
	@RequestMapping(value="/enableEuropeanCard", method=RequestMethod.POST)
	public String enableEuropeanCard(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		user.setEuropeanStudentCard(true);
		user.merge();
		logService.log(user.getCards().get(0).getId(), ACTION.ENABLEEUROPEANCARD, RETCODE.SUCCESS, "", eppn, null);
		redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "european");
		return "redirect:/user";
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
}

