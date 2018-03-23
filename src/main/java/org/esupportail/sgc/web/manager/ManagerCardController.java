package org.esupportail.sgc.web.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardActionMessageService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.FormService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.PreferencesService;
import org.esupportail.sgc.services.TemplateCardService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.esupportail.sgc.services.paybox.PayBoxService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/manager")
@Controller	
@RooWebScaffold(path = "manager", formBackingObject = Card.class)
public class ManagerCardController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static String SUCCESS_MSG ="manager.msg.success.";
	public final static String ERROR_MSG ="manager.msg.error.";
	public final static String WARNING_MSG ="manager.msg.warning.";
	public final static String IMG_INTERDIT = "media/photo_interdite.png";
	public final static String header[] = new String[]{"eppn", "etat", "displayName", "supannEtuId", "supannEmpId", "userType", "crous", "difPhoto", "userEditable", "deliveredDate", "nbCards", "nbRejets", "etatEppn", "address", "payCmdNum", "motifDisable", "requestDate", "dateEtat"};

	@Resource 
	UserInfoService userInfoService;

	@Resource
	CardService cardService;

	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	PayBoxService payBoxService;
	
	@Resource
	LogService logService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;

	
	@Resource
	ImportExportService importExportService;
	
	@Resource	
	AppliConfigService appliConfigService;
	
	@Resource
	PreferencesService preferencesService;
	
	@Resource 
	CardActionMessageService cardActionMessageService;
	
	@Resource
	TemplateCardService templateCardService;
	
	@Resource
	FormService formService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "manager";
	}
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpManager();
	}   
	
	@ModelAttribute("actionMessages")
	public Map<Etat, List<CardActionMessage>> getCardActionMessages() {
		return cardActionMessageService.getCardActionMessages();
	}
	
	@ModelAttribute("searchBean")
	public CardSearchBean getDefaultCardSearchBean() {
		CardSearchBean searchBean =  new CardSearchBean();
		searchBean.setEditable("true");
		return searchBean;
	}
	
	@ModelAttribute("livraison")
	public String getLivraisonConfig() {
		return appliConfigService.getModeLivraison();
	}
	
	@ModelAttribute("csvFields")
	public List<String> getCsvFields() {
		return Arrays.asList(header);
	}
	
	@ModelAttribute("validateServicesNames")
	public List<String> getValidateServicesNames() {
		return cardEtatService.getValidateServicesNames();
	}
	
	@ModelAttribute("userPrefs")
	public HashedMap getuserPrefs() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		HashedMap mapPrefs= new HashedMap();
    	mapPrefs.put("editable", preferencesService.getPrefValue(eppn, "EDITABLE"));
    	mapPrefs.put("ownOrFreeCard", preferencesService.getPrefValue(eppn, "OWNORFREECARD"));
		return mapPrefs;
	}

    @RequestMapping(params="eppn", produces = "text/html")
    @Transactional
    public String show(@RequestParam String eppn, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        User user = User.findUser(eppn);
        uiModel.asMap().clear();
        if(!user.getCards().isEmpty()) {
        	return "redirect:/manager/" + user.getCards().get(0).getId();
        } else {
        	return "redirect:/manager";
        }
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    @Transactional
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        Card card =  Card.findCard(id);
        User user = User.findUser(card.getEppn());
        if(!user.getCards().isEmpty()){
        	for(Card cardItem : user.getCards()){
        		cardEtatService.updateEtatsAvailable4Card(cardItem);
        		cardItem.setIsPhotoEditable(cardEtatService.isPhotoEditable(cardItem));
        	}
        }
        uiModel.addAttribute("user", user);
        uiModel.addAttribute("currentCard", card);
        
        return "manager/show";
    }
    
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @RequestMapping(value = "{cardId}/resync-user/{eppn:.+}", produces = "text/html")
    @Transactional
    public String resyncUser(@PathVariable String eppn, @PathVariable String cardId, Model uiModel) {
        User user = User.findUser(eppn);
        resynchronisationUserService.synchronizeUserInfo(user.getEppn());
        uiModel.asMap().clear();
        return "redirect:/manager/" + cardId;
    }

	@RequestMapping(produces = "text/html", params="print")
	public String printCards(Model uiModel) {
		uiModel.addAttribute("cards", Card.findAllCards());
		return "manager/print-card";
	}
	
	@PreAuthorize("hasRole('ROLE_LIVREUR')")
	@RequestMapping(value="/deliver/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String deliver(@PathVariable("cardId") Long cardId,Model uiModel) {
		Card card = Card.findCard(cardId);
		card.setDeliveredDate(new Date());
		card.merge();
		logService.log(card.getId(), ACTION.MANAGER_DELIVERY, RETCODE.SUCCESS, "", card.getEppn(), null);
		uiModel.asMap().clear();
		return "redirect:/manager/" + card.getId();
	}
	
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_LIVREUR')")
	@RequestMapping(value="/multiDelivery", method=RequestMethod.POST)
	@Transactional
	public String multiDelivery(@RequestParam List<Long> listeIds, Model uiModel) {
		
		for(Long id : listeIds){
			try {
				Card card = Card.findCard(id);
				if(card.getDeliveredDate() == null) {
					card.setDeliveredDate(new Date());
					card.merge();
					logService.log(card.getId(), ACTION.MANAGER_DELIVERY, RETCODE.SUCCESS, "", card.getEppn(), null);
				}
			} catch (Exception e) {
				log.info("La carte avec l'id suivant n'a pas pu être marquée comme livrée : " + id, e);
			}
		}
		uiModel.asMap().clear();
		return "redirect:/manager/";
	}

	@PreAuthorize("hasRole('ROLE_MANAGER')")
	@RequestMapping(value="/replayValidationOrInvalidation/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String replayValidationOrInvalidation(@PathVariable("cardId") Long cardId, @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro, Model uiModel) {
		Card card = Card.findCard(cardId);
		cardEtatService.replayValidationOrInvalidation(card.getId(), validateServicesNames, resynchro);
		uiModel.asMap().clear();
		return "redirect:/manager/" + card.getId();
	}
	
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	@RequestMapping(value="/multiReplayValidationOrInvalidation", method=RequestMethod.POST)
	@Transactional
	public String multiReplayValidationOrInvalidation(@RequestParam List<Long> listeIds,  @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro,
			final RedirectAttributes redirectAttributes) {
		if(validateServicesNames != null){
			for(Long id : listeIds){
				try {
					Card card = Card.findCard(id);
					cardEtatService.replayValidationOrInvalidation(card.getId(), validateServicesNames, resynchro);
				} catch (Exception e) {
					log.info("La carte avec l'id suivant n'a pas été validée/invalidée : " + id, e);
				}
			}
		}else{
			redirectAttributes.addFlashAttribute("messageWarning", WARNING_MSG.concat("multivalidation"));
		}
		return "redirect:/manager/";
	}
	
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	@RequestMapping(value="/action/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String actionEtat(@PathVariable("cardId") Long cardId, @RequestParam Etat etatFinal, @RequestParam(required=false) String comment, Model uiModel) {
		Card card = Card.findCard(cardId);
		card.merge();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();

		if(Etat.IN_PRINT.equals(etatFinal) && (Etat.REQUEST_CHECKED.equals(card.getEtat()) || eppn.equals(card.getEtatEppn()))) {
			if(cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false)) {
				uiModel.addAttribute("cards", Arrays.asList(new Card[]{card}));
			}
			return "manager/print-card";
		} else {
			uiModel.asMap().clear();
			if(Etat.RENEWED.equals(etatFinal)) {
				Card newCard = cardService.requestRenewalCard(card);
				return "redirect:/manager/" + newCard.getId();
			} else {
				cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false);
				return "redirect:/manager/" + card.getId();
			}		
		}
	}
	
	public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
		// Not used : for Spring ROO
		return null;
	}
	
	
	
    @RequestMapping(produces = "text/html")
    @Transactional
    public String search(@Valid CardSearchBean searchBean, 
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, 
    		@RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, 
    		@RequestParam(value = "index", required = false) String index, Model uiModel, HttpServletRequest request) {
		
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
    	int sizeNo = -1;
    	int firstResult = -1;  	   	
    	if(size == null || size > 1000 || size == 0) {
    		Object sizeInSession = request.getSession().getAttribute("size_in_session");
    		size = sizeInSession != null ? (Integer)sizeInSession : 10;
    		page = 1;
    	}
    	if(searchBean.getType() == null) {
    		searchBean.setType("");
    		// Hack - ROLE_MANAGER_${userType} -> force userType
    		for(String userType : userInfoService.getListExistingType()) {
    			if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER_" + userType))) {
    				searchBean.setType(userType);
    				break;
    			}
    		}
    	}
    	if(index !=null && "first".equals(index)){
	    	searchBean.setEditable(preferencesService.getPrefValue(eppn, "EDITABLE"));
	    	searchBean.setOwnOrFreeCard(Boolean.valueOf(preferencesService.getPrefValue(eppn, "OWNORFREECARD")));
    	}
    	sizeNo = size == null ? 10 : size.intValue();
    	firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
    	long countCards = Card.countFindCards(searchBean, eppn);
    	List<Card> cards = Card.findCards(searchBean, eppn, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList();
        float nrOfPages = (float) countCards / sizeNo;
        
    	uiModel.addAttribute("types", userInfoService.getListExistingType());
    	uiModel.addAttribute("etats", cardEtatService.getDistinctEtats());
    	List<BigInteger> nbCards = User.getDistinctNbCards();
		uiModel.addAttribute("nbCards", nbCards);
		List<BigInteger> nbRejets = Card.getDistinctNbRejets();
		uiModel.addAttribute("nbRejets", nbRejets);
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	uiModel.addAttribute("searchBean", searchBean);
    	uiModel.addAttribute("cards", cards);
    	uiModel.addAttribute("countCards",  countCards);
    	uiModel.addAttribute("selectedType",  searchBean.getType());
    	uiModel.addAttribute("freeFields",  formService.getFieldList());
    	
    	if(searchBean.getFreeFieldValue()!= null && !searchBean.getFreeFieldValue().isEmpty()){
    		HashMap<String, String[]> test = searchBean.getFreeFieldValue();
    		test.values().removeAll(Collections.singleton(""));
    		uiModel.addAttribute("collapse", test.size() > 0 ? "in" : "");
    		List<String> values = new ArrayList<String>(); 
    		for(String value[] : searchBean.getFreeFieldValue().values()){
    			String joinString = StringUtils.join(value, ",");
    			values.add(joinString);
    		}
    		String finalJoin = StringUtils.join(values, "@@");
    		uiModel.addAttribute("fieldsValue", finalJoin);
    	}else{
    		uiModel.addAttribute("collapse", searchBean.getFreeFieldValue() == null ? "" : "in");
    	}
    	uiModel.addAttribute("size",  size);
    	if(searchBean.getEtat()!=null){
    		uiModel.addAttribute("addresses", getFilteredAdresses(searchBean.getType(),searchBean.getEtat().name()));
    	}else{
    		uiModel.addAttribute("addresses", userInfoService.getListAdresses(searchBean.getType(),null));
    	}
		uiModel.addAttribute("eppn", eppn);
    	addDateTimeFormatPatterns(uiModel);
    	return "manager/list";
    }

	@PreAuthorize("hasRole('ROLE_MANAGER')")
    @RequestMapping("/multiUpdate")
    // No @Transactional here so that exception catching on ManagerController.multiUpdate works well
    public String multiUpdate(@RequestParam(value="comment", defaultValue= "") String comment, 
    		@RequestParam List<Long> listeIds, @RequestParam Etat etatFinal, @RequestParam(value="forcedEtatFinal", required=false) Etat forcedEtatFinal, 
    		@RequestParam(value="updateEtatAdmin", required=false) String updateEtatAdmin, @RequestHeader("User-Agent") String userAgent, 
    		final RedirectAttributes redirectAttributes, Model uiModel, HttpServletRequest request) {

    	List<Long> listeIdsErrors = new ArrayList<Long>();
    	List<Long> listeIdsOk = new ArrayList<Long>();
    	List<Card> cards = new ArrayList<Card>();
    	if(listeIds!=null) {
    		int i = 0;
    		for(Long id : listeIds) {
    			try {
    				i++;
    				//if(i==2) throw new RuntimeException("yop");
    				Card card = Card.findCard(id);
    				card.setCommentaire(comment);
    				
    				if(updateEtatAdmin != null && forcedEtatFinal!=null) {
    					Etat firstEtat = card.getEtat();
    					card.setEtat(forcedEtatFinal);
    					card.merge();
    					log.info("Changement d'etat manuel à " + forcedEtatFinal + " pour la carte de " + card.getEppn());
    					logService.log(card.getId(), ACTION.FORCEDUPDATE, RETCODE.SUCCESS, firstEtat.name() + " -> " + forcedEtatFinal, card.getEppn(), null);
    				} else {
    					if(Etat.RENEWED.equals(etatFinal)) {
    						cardService.requestRenewalCard(card);
    					} else {
	    					if(cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false)) {
	    						log.info("Changement d'etat à " + etatFinal + " pour la carte de " + card.getEppn());
	    						cards.add(card);
	    					}
    					}
    				}
    				listeIdsOk.add(id);
    			} catch (Exception e) {
    				log.error("Erreur lors de la mise à jour de la demande de carte pour l'id : " + id, e);
    				listeIdsErrors.add(id);
    			}
    		}
    	}
    	log.info("Mise à jour des demandes de carte pour les ids suivants OK : " + listeIdsOk);
    	if(listeIdsErrors.isEmpty()) {
    		redirectAttributes.addFlashAttribute("messageSuccess", SUCCESS_MSG.concat("mulitupdate"));
    	} else{
    		redirectAttributes.addFlashAttribute("messageError", ERROR_MSG.concat("multiupdate"));
    		redirectAttributes.addFlashAttribute("messageAddon", "Erreurs sur les ids suivants : " + listeIdsErrors);
    		log.error("Erreur lors de la mise à jour des demandes de carte pour les ids suivant : " + listeIdsErrors);
    	}

    	if(Etat.IN_PRINT.equals(etatFinal)) {
    		uiModel.addAttribute("cards", cards);
    		return "manager/print-card";
    	}

    	/*
    	CardSearchBean searchBean = new CardSearchBean();
    	searchBean.setEditable("true");
    	searchBean.setOwnOrFreeCard(true);
    	searchBean.setEtat(etatFinal);

    	return search(searchBean, null, null, "dateEtat", "DESC", uiModel, request);
    	 */
    	return "redirect:/manager?index=first" ;
    }   
	
	@RequestMapping(value="/getMultiUpdateForm")
	@Transactional
	public String getMultiUpdateForm(@RequestParam List<Long> cardIds, Model uiModel) {
		if(cardIds.isEmpty()) {
			uiModel.addAttribute("cardIds", cardIds);
		} else {
			List<Etat> etatsAvailable = cardEtatService.getEtatsAvailable4Cards(cardIds);
			uiModel.addAttribute("allEtats", Arrays.asList(Etat.values()));
			uiModel.addAttribute("etatsAvailable", etatsAvailable);
			uiModel.addAttribute("cardIds", cardIds);
			uiModel.addAttribute("etatInit", Card.findCard(cardIds.get(0)).getEtat());
			uiModel.addAttribute("deliveredFlag", cardEtatService.areCardsReadyToBeDelivered(cardIds));
			uiModel.addAttribute("validatedFlag", cardEtatService.areCardsReadyToBeValidated(cardIds));
		}
		return "manager/multiUpdateForm";
	}
	
	@PreAuthorize("hasRole('ROLE_MANAGER')")
	@RequestMapping(value="/updatePhoto", method = RequestMethod.POST)
	@Transactional
	public String cardRequest(@RequestParam("cardId") Long id, @RequestParam("imageData") String imageData, Model uiModel, final RedirectAttributes redirectAttributes){
		
		if (imageData.isEmpty()) {
			log.info("Aucune image disponible");
			redirectAttributes.addFlashAttribute("messageInfo", "error_leocarte_emptyfile");
		} else {
			if(id!=null){
				Card card = Card.findCard(id);
				try {
					String encoding = cardService.getPhotoParams().get("encoding");
					int contentStartIndex = imageData.indexOf(encoding) + encoding.length();
					byte[] bytes = Base64.decodeBase64(imageData.substring(contentStartIndex));  
					Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
					String contentType = cardService.getPhotoParams().get("contentType");
					card.getPhotoFile().setContentType(contentType);
					card.getPhotoFile().setFileSize(fileSize);
					ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
					log.info("Upload and set file in DB with filesize = " + fileSize);
					card.getPhotoFile().getBigFile().setBinaryFileStream(inputStream, fileSize);
					Calendar cal = Calendar.getInstance();
					Date currentTime = cal.getTime();
					card.getPhotoFile().setSendTime(currentTime);
					card.merge();
					logService.log(card.getId(), ACTION.UPDATEPHOTO, RETCODE.SUCCESS, "", card.getEppn(), null);
					log.info("Succès de la mise à jour de la photo pour l'utilisateur " +   card.getEppn());
					redirectAttributes.addFlashAttribute("messageSuccess", SUCCESS_MSG.concat("updatePhoto"));
					} catch (Exception e) {
						logService.log(card.getId(), ACTION.UPDATEPHOTO, RETCODE.FAILED, "", card.getEppn(), null);
						redirectAttributes.addFlashAttribute("messageInfo", ERROR_MSG.concat("updatePhoto"));
						log.error("Echec lors de la mise à jour de la photo pour l'utilisateur " +   card.getEppn(), e);
				}
			}
		}
		return "redirect:/manager/" + id;
	}


	public List<String> getFilteredAdresses(String tabType, String etat){
		List<String> adresses = userInfoService.getListAdresses(tabType, etat);	
		return adresses;
	}

	@PreAuthorize("hasRole('ROLE_MANAGER')")
	@RequestMapping(value="/csvSearch", method = RequestMethod.GET)
	public void getCsvFromSearch(@ModelAttribute("searchBean") CardSearchBean searchBean, @RequestParam("fields") List<String> fields, HttpServletResponse response) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();	
		response.setContentType("text/csv");
		String reportName = "cards.csv";
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
		response.setHeader("Content-disposition", "attachment;filename=" + reportName);
		
		importExportService.exportCsv2OutputStream(searchBean, eppn, fields, response.getOutputStream());
	}
	
	@RequestMapping(value="/savePrefs")
	public String  savePrefs(@RequestParam(value="editable", required=false) String editable, @RequestParam(value="ownOrFreeCard", required=false) String ownOrFreeCard,  Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
		editable = (editable!= null)? "true" : "all";
		ownOrFreeCard = (ownOrFreeCard!= null)? "true" : "false";
		uiModel.asMap().clear();
		try {
			preferencesService.setPrefs(eppn, "EDITABLE", editable);
			preferencesService.setPrefs(eppn, "OWNORFREECARD", ownOrFreeCard);
		} catch (Exception e) {
			log.warn("Impossible de sauvegarder les préférences", e);
		}

		return "redirect:/manager?index=first";
	}
	
}

