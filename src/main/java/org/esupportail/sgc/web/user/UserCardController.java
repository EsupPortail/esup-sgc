package org.esupportail.sgc.web.user;

import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.PayBoxForm;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.security.ShibAuthenticatedUserDetailsService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.EsupSgcBmpAsBase64Service;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.TemplateCardService;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.esupportail.sgc.services.paybox.PayBoxService;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequestMapping("/user")
@Controller
public class UserCardController {

	public final Logger log = LoggerFactory.getLogger(getClass());
	
	public final static String SUCCESS_MSG ="user.msg.success.";
	
	public final static String ERROR_MSG ="user.msg.error.";
	
	public final static String WARNING_MSG ="user.msg.warning.";
	
	@Resource
	ImportExportCardService importExportCardService;
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}   
	
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

	@Autowired(required = false)
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
	ExternalCardService externalCardService;
	
	@Resource
	TemplateCardService templateCardService;
	
	@Resource
	CrousService crousService;
	
	@Resource
	ApiEscrService apiEscrService;

	@Resource
	EsupSgcBmpAsBase64Service esupSgcBmpAsBase64Service;
	
	@RequestMapping
	public String index(Locale locale, HttpServletRequest request, Model uiModel, @RequestHeader("User-Agent") String userAgent) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = new User ();
		user.setEppn(eppn);
		uiModel.addAttribute("configUserMsgs", userService.getConfigMsgsUser());
		if(userService.isFirstRequest(user)) {
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
	
	@Transactional
	public String viewExternalCardRequestForm(Model uiModel, HttpServletRequest request, Card externalCard) {
		uiModel.addAttribute("externalCard", externalCard);
		byte[] externalCardPhoto = null;
		try {
			externalCardPhoto = IOUtils.toByteArray(externalCard.getPhotoFile().getBigFile().getBinaryFile().getBinaryStream());
			uiModel.addAttribute("externalCardPhoto", java.util.Base64.getEncoder().encodeToString(externalCardPhoto));
		} catch (IOException | SQLException | NullPointerException e) {
			log.warn("Exception when retrieving photo from external card", e);
			externalCardPhoto = importExportCardService.loadNoImgPhoto();
		}
		
		uiModel.addAttribute("externalCardPhoto", java.util.Base64.getEncoder().encodeToString(externalCardPhoto));
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
			log.error("problème lors de l'importation de la carte extérieure de " + eppn, e);
			redirectAttributes.addFlashAttribute("messageError", ERROR_MSG + "enable");
		}
		return "redirect:/user";
	}

	@Transactional
	@RequestMapping(value="/card-request-form")
	public String viewCardRequestForm(Model uiModel, HttpServletRequest request, @RequestHeader("User-Agent") String userAgent) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		uiModel.addAttribute("user", user);
		
		String defaultPhotoMd5 = null;
		Card lastCard = cardService.findLastCardByEppnEquals(eppn);
		if(lastCard !=null){
			defaultPhotoMd5 = lastCard.getPhotoFile().getBigFile().getMd5();
		} else if(user.getDefaultPhoto() != null && user.getDefaultPhoto().getBigFile().getMd5() != null) {
			defaultPhotoMd5 = user.getDefaultPhoto().getBigFile().getMd5();
		}
		
		UserAgent userAgentUtils = UserAgent.parseUserAgentString(userAgent);
		uiModel.addAttribute("sizeMax", appliConfigService.getFileSizeMax()/1000);
		uiModel.addAttribute("deviceType", userAgentUtils.getOperatingSystem().getDeviceType());
		uiModel.addAttribute("templateCard", templateCardService.getTemplateCard(user));
		uiModel.addAttribute("configUserMsgs", userService.getConfigMsgsUser());
		uiModel.addAttribute("defaultPhotoMd5", defaultPhotoMd5);
		uiModel.addAttribute("isEsupSgcUser", userService.isEsupSgcUser(user));
		uiModel.addAttribute("isISmartPhone",  userService.isISmartphone(userAgent));
		Map<String, Boolean> displayFormParts = userService.displayFormParts(user, false);
		log.debug("displayFormParts for " + eppn + " : " + displayFormParts);
		uiModel.addAttribute("displayFormParts", displayFormParts);
		uiModel.addAttribute("requestUserIsManager", false);
		uiModel.addAttribute("photoSizeMax", appliConfigService.getFileSizeMax());
		return "user/card-request";
	}
	
	
	@RequestMapping(value="/card-payment")
	public String viewPaymentCardRequestForm(Locale locale, Model uiModel, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		if(! userService.isFreeRenewal(user)){
			uiModel.addAttribute("isFreeRenewal",  userService.isFreeRenewal(user));
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
		uiModel.addAttribute("configUserMsgs", userService.getConfigMsgsUser());
		uiModel.addAttribute("id", id);
		uiModel.addAttribute("isRejected", true);
		return viewCardRequestForm(uiModel, request, userAgent);
	}	
	
	@RequestMapping(value="/card-disable")
	public String viewDisableCardForm(@RequestParam("id") Long id, Model uiModel, HttpServletRequest request) {
		uiModel.addAttribute("motifsList", MotifDisable.getMotifsList());
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
		StopWatch stopWatch = new PrettyStopWatch();
		stopWatch.start("auth");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		stopWatch.start("steps");
		uiModel.addAttribute("steps", cardEtatService.getTrackingSteps());
		uiModel.addAttribute("user", user);
		stopWatch.start("payboxList");
		uiModel.addAttribute("payboxList", PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList());
		stopWatch.start("montant");
		uiModel.addAttribute("montant", appliConfigService.getMontantRenouvellement());
		stopWatch.start("displayFormParts");
		uiModel.addAttribute("displayFormParts", userService.displayFormParts(user, false));
		stopWatch.stop();
		uiModel.addAttribute("displayVirtualCard", StringUtils.hasLength(appliConfigService.getBmpCardCommandVirtual()));
		log.trace(stopWatch.prettyPrint());
		return "user/card-info";
	}

	
	@RequestMapping(method = RequestMethod.POST)
	public String cardRequest(@Valid Card card, BindingResult bindingResult, Model uiModel, @RequestParam boolean requestUserIsManager,
			@RequestHeader("User-Agent") String userAgent, HttpServletRequest request, final RedirectAttributes redirectAttributes) throws IOException {	
		if (bindingResult.hasErrors()) {
				log.warn(bindingResult.getAllErrors().toString());
			return "redirect:/";
		}	
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String eppn = auth.getName();
		String redirect = "redirect:/user";
		
		User user = User.findUser(eppn);

		if(requestUserIsManager) {
			// requestUserIsManager=true
			// -> carte demandée par un manager pour un utilisateur donné
			if(card.getEppn() == null) {
				throw new SgcRuntimeException("Tentative de demande de carte mais eppn cible null.", null);
			} else if(!userService.isEsupManager(user)) {
				throw new SgcRuntimeException(String.format("Tentative de demande de carte pour %s mais %s n'a pas les droits de manager.", card.getEppn(), user.getEppn()), null);
			} else {
				eppn = card.getEppn();
				redirect = "redirect:/manager?index=first";
			}
		}

		if(user == null ){
			user = new User();
			user.setEppn(eppn);
		}
		
		synchronized (eppn.intern()) {
			
			// check rights  sur String est global - à éviter - TODO ?
			if(userService.isFirstRequest(user) || userService.isFreeRenewal(user) ||  userService.isPaidRenewal(user) || cardEtatService.hasRejectedCard(eppn) || userService.isEsupManager(user)) {
			
				if(!cardEtatService.hasNewCard(eppn)) {
					
					boolean emptyPhoto = cardService.requestNewCard(card, userAgent, eppn, request, requestUserIsManager);
					
					if(emptyPhoto) {
						redirectAttributes.addFlashAttribute("messageInfo", WARNING_MSG + "leocarte_emptyfile");
					} else {
						redirectAttributes.addFlashAttribute("messageSuccess", "success_leocarte_upload");
					}
				}
			} else {
				log.warn(eppn + " tried to request card but he has no rights to do it." );
			}
		}
		return redirect;
	}
	
	@Transactional
	@RequestMapping(value="/photo")
	public void getPhoto(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		String eppn = SecurityContextHolder.getContext().getAuthentication().getName();
		PhotoFile photoFile = null;
		Card lastCard = cardService.findLastCardByEppnEquals(eppn);
		User user = User.findUser(eppn);
		if(lastCard !=null){
			photoFile = lastCard.getPhotoFile();
		} else if(user.getDefaultPhoto() != null && user.getDefaultPhoto().getBigFile().getMd5() != null) {
			photoFile = user.getDefaultPhoto();
		}
		if(photoFile != null) {
			Long size = photoFile.getFileSize();
			String contentType = photoFile.getContentType();
			response.setContentType(contentType);
			response.setContentLength(size.intValue());
			IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
		}
	}
	
	@Transactional
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

	@Transactional
	@RequestMapping(method = RequestMethod.GET, value = "/payboxOk")
	public String getPaybox(@RequestParam String montant, @RequestParam String reference, @RequestParam(required = false) String auto, @RequestParam String erreur, 
			@RequestParam String idtrans, @RequestParam String signature, HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		String ip = request.getRemoteAddr();
		String queryString = request.getQueryString();
		if (payBoxService.payboxCallback(montant, reference, auto, erreur, idtrans, signature, queryString, ip)) {
 			String eppn = SecurityContextHolder.getContext().getAuthentication().getName();
 			User user = User.findUser(eppn);
 			try {
 				cardService.sendMailCard(user, null, appliConfigService.getNoReplyMsg(),user.getEmail() ,appliConfigService.getListePpale(),
 						appliConfigService.getSubjectAutoCard().concat(" -- ".concat(user.getEppn())), appliConfigService.getPayboxMessage());
 			} catch (Exception e) {
 					log.error("Erreur lors de l'envoi du mail pour la carte de :" + user.getEppn(), e);
 			}
			redirectAttributes.addFlashAttribute("messageSuccess", SUCCESS_MSG + "paybox");
		} 
		return "redirect:/user";
	}
	
	@Transactional
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
	
	@Transactional
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
		try {
			crousService.enableCrous(eppn);
			logService.log(null, ACTION.ENABLECROUS, RETCODE.SUCCESS, "", eppn, null);
			redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "crous");
		} catch (Exception e) {
			log.error("problème lors de l'activation du CROUS pour " + eppn, e);
			redirectAttributes.addFlashAttribute("messageError", ERROR_MSG + "crous");
			logService.log(null, ACTION.ENABLECROUS, RETCODE.FAILED, "", eppn, null);
		} 
		return "redirect:/user";
	}

	@RequestMapping(value="/unclose", method=RequestMethod.POST)
	public String unclose(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		try {
			crousService.uncloseAndResync(eppn);
			redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG.concat("uncloseCrous"));
			logService.log(null, ACTION.CROUS_UNCLOSE, RETCODE.SUCCESS, "", eppn, "");
		} catch (Exception e) {
			logService.log(null, ACTION.CROUS_UNCLOSE, RETCODE.FAILED, e.getMessage(), eppn, null);
			redirectAttributes.addFlashAttribute("messageError", ERROR_MSG.concat("uncloseCrous"));
			log.error("Exception lors de la demande de déclôture du compte CROUS de l'utilisateur " + eppn, e);
		}
		return "redirect:/user";
	}

	@Transactional
	@RequestMapping(value="/disableCrous", method=RequestMethod.POST)
	public String disableCrous(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		// autorisé qu'en cas d'erreur crous
		if(user.getCrousError() != null && !user.getCrousError().isEmpty()) {
			user.setCrous(false);
			user.merge();
		}
		logService.log(null, ACTION.DISABLECROUS, RETCODE.SUCCESS, "Erreur CROUS : " + user.getCrousError(), eppn, null);
		return "redirect:/user";
	}
	
	@Transactional
	@RequestMapping(value="/enableEuropeanCard", method=RequestMethod.POST)
	public String enableEuropeanCard(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		User user = User.findUser(eppn);
		Card enabledCard = user.getEnabledCard();
		if(enabledCard != null) {
			apiEscrService.validate(enabledCard);
		}
		user.setEuropeanStudentCard(true);
		user.merge();
		logService.log(user.getCards().get(0).getId(), ACTION.ENABLEEUROPEANCARD, RETCODE.SUCCESS, "", eppn, null);
		redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "european");
		return "redirect:/user";
	}
	
	@Transactional
	@RequestMapping(value="/disableEuropeanCard", method=RequestMethod.POST)
	public String disableEuropeanCard(final RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		apiEscrService.deleteEscrStudent(eppn);
		User user = User.findUser(eppn);
		user.setEuropeanStudentCard(false);
		user.merge();
		logService.log(user.getCards().get(0).getId(), ACTION.DISABLEEUROPEANCARD, RETCODE.SUCCESS, "", eppn, null);
		redirectAttributes.addFlashAttribute("messageInfo", SUCCESS_MSG + "european.disable");
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

	@RequestMapping(value = "/card-bmp-b64", method = RequestMethod.GET)
	public String getCardBmpB64(@RequestParam Long cardId, Model uiModel) {
		log.debug("getCardBmpB64 with cardId = " + cardId);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card card = Card.findCard(cardId);
		if(eppn.equals(card.getEppn())) {
			String bmpAsBase64 = esupSgcBmpAsBase64Service.getBmpCard(card.getId(), EsupSgcBmpAsBase64Service.BmpType.virtual);
			uiModel.addAttribute("bmpAsBase64", bmpAsBase64);
		}
		return "user/card-b64";
	}
}

