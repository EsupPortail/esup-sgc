package org.esupportail.sgc.web.manager;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.*;
import org.esupportail.sgc.domain.*;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.exceptions.SgcNotFoundException;
import org.esupportail.sgc.security.PermissionService;
import org.esupportail.sgc.security.ShibUser;
import org.esupportail.sgc.services.*;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.MapUtils;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.esupportail.sgc.web.admin.ImportExportController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/manager")
@Controller	
@SessionAttributes({"printerEppn"})
public class ManagerCardController {

	public enum MANAGER_SEARCH_PREF {OWNORFREECARD, EDITABLE, USERTYPE, LIST_NO_IMG};

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static String SUCCESS_MSG ="manager.msg.success.";
	
	public final static String ERROR_MSG ="manager.msg.error.";
	
	public final static String WARNING_MSG ="manager.msg.warning.";
	
	public final static String IMG_INTERDIT = "media/photo_interdite.png";
	
	public final static String IMG_NOT_FOUND = "media/nophoto.png";
	
	public final static String[] header = new String[]{"eppn", "email", "etat", "name", "firstname", "birthday", "supannEtuId", "supannEmpId", "supannCodeINE", "userType", "crous", "difPhoto", "userEditable", "deliveredDate", "dueDate", "nbCards", "nbRejets", "etatEppn", "address", "payCmdNum", "motifDisable", "requestDate", "dateEtat"};


	@Resource 
	UserInfoService userInfoService;

	@Resource
	CardService cardService;

	@Resource
	CardEtatService cardEtatService;
	
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
	FormService formService;
	
	@Resource
	PermissionService permissionService;

	@Resource
	CrousService crousService;

	@Resource
	PrinterService printerService;

    @Resource
    BigFileDaoService bigFileDaoService;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    PhotoFileDaoService photoFileDaoService;

    @Resource
    PrinterDaoService printerDaoService;

    @Resource
    TemplateCardDaoService templateCardDaoService;

    @Resource
    UserDaoService userDaoService;

    @Resource
    ImportExportController importExportController;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "manager";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}   
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpManager();
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
	
	@ModelAttribute("csvFiltres")
	public List<String> getCsvFiltres() {
		return formService.getFieldsListAsCamel();
	}
	
	@ModelAttribute("validateServicesNames")
	public List<String> getValidateServicesNames() {
		return cardEtatService.getValidateServicesNames();
	}

	public Map getuserPrefs() {
		PrettyStopWatch stopWatch = new PrettyStopWatch();
		stopWatch.start();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
        Map mapPrefs= new HashMap();
    	mapPrefs.put("editable", preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.EDITABLE.name()));
    	mapPrefs.put("ownOrFreeCard", preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.OWNORFREECARD.name()));
		mapPrefs.put("userType", preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.USERTYPE.name()));
        mapPrefs.put("listNoImg", preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.LIST_NO_IMG.name()));
		stopWatch.stop();
		log.trace("userPrefs tooks " + stopWatch.shortSummary());
		return mapPrefs;
	}

	@ModelAttribute("printers")
	public SortedMap<Printer, Boolean> getPrinters(Authentication auth) {
		PrettyStopWatch stopWatch = new PrettyStopWatch();
		stopWatch.start();
		String eppn = auth.getName();
		List<String> ldapGroups = ((ShibUser)(auth.getPrincipal())).getLdapGroups();
		SortedMap<Printer, Boolean> printers = printerService.getPrinters(eppn, ldapGroups);
		stopWatch.stop();
		log.trace("getPrinters tooks " + stopWatch.shortSummary() + " -> " + printers.size() + " printers");
		log.trace("printers : " + printers.keySet().stream().map(Printer::getLabel).reduce((a, b) -> a + ", " + b).orElse(""));
		return printers;
	}

	@ModelAttribute("currentPrinter")
	public Printer getCurrentPrinter(@SessionAttribute(required = false) String printerEppn) {
		if(!StringUtils.isEmpty(printerEppn)) {
			List<Printer> printers = printerDaoService. findPrintersByEppn(printerEppn).getResultList();
			if(printers.size()>0) {
				return printers.get(0);
			}
		}
		return null;
	}

	@ModelAttribute("printerEppn")
	public String getPrinterEppn(@SessionAttribute(required = false) String printerEppn, Authentication auth) {
		if(StringUtils.isEmpty(printerEppn)) {
			SortedMap<Printer, Boolean> printers = getPrinters(auth);
			if(printers.size()>0) {
				printerEppn = printers.firstKey().getEppn();
			}
		}
		return printerEppn;
	}

    @RequestMapping(params="eppn", produces = "text/html", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public String show(@RequestParam String eppn, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        User user = userDaoService.findUser(eppn);
        uiModel.asMap().clear();
        if(user!=null && !user.getCards().isEmpty()) {
        	return "redirect:/manager/" + user.getCards().get(0).getId();
        } else {
        	return "redirect:/manager";
        }
    }

    @RequestMapping(value = "/{id}", produces = "text/html", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(#id, 'consult')")
    public String show(@PathVariable("id") Long id, @SessionAttribute(required = false) String printerEppn, Model uiModel) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        addDateTimeFormatPatterns(uiModel);
        Card card =  cardDaoService.findCard(id);
        if(card == null) {
        	throw new SgcNotFoundException(String.format("Card %s not found", id), null);
        }
        User user = userDaoService.findUser(card.getEppn());
        if(!user.getCards().isEmpty()){
        	for(Card cardItem : user.getCards()){
        		cardEtatService.updateEtatsAvailable4Card(cardItem, printerEppn);
        		cardActionMessageService.updateCardActionMessages(cardItem);
        		cardItem.setIsPhotoEditable(cardEtatService.isPhotoEditable(cardItem));
        	}
        }
        uiModel.addAttribute("user", user);
        uiModel.addAttribute("currentCard", card);
        uiModel.addAttribute("managePermission",  permissionService.hasManagePermission(roles, user.getUserType()));
        uiModel.addAttribute("motifsList", MotifDisable.getMotifsList());
        uiModel.addAttribute("hasRequestCard", cardEtatService.hasRequestCard(user.getEppn()));
        uiModel.addAttribute("userTemplateCard", templateCardDaoService.getTemplateCard(user));

        return "templates/manager/show";
    }
    
    @PreAuthorize("hasPermission(#cardId, 'manage')")
    @RequestMapping(value = "{cardId}/resync-user/{eppn:.+}", produces = "text/html")
    @Transactional
    public String resyncUser(@PathVariable String eppn, @PathVariable Long cardId, Model uiModel) {
        User user = userDaoService.findUser(eppn);
        resynchronisationUserService.synchronizeUserInfo(user.getEppn());
        uiModel.asMap().clear();
        return "redirect:/manager/" + cardId;
    }
	
	@PreAuthorize("hasPermission(#cardId, 'manage') or hasRole('ROLE_LIVREUR')")
	@RequestMapping(value="/deliver/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String deliver(@PathVariable("cardId") Long cardId,Model uiModel) {
		Card card = cardDaoService.findCard(cardId);
		card.setDeliveredDate(LocalDateTime.now());
		cardDaoService.merge(card);
		logService.log(card.getId(), ACTION.MANAGER_DELIVERY, RETCODE.SUCCESS, "", card.getEppn(), null);
		uiModel.asMap().clear();
		if(Etat.ENCODED.equals(card.getEtat())) {
			log.info("livraison of " + card.getCsn() + " -> activation");
			cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, "Activation suite à la livraison.", null, false, false);
		}
		return "redirect:/manager/" + card.getId();
	}
	
    @PreAuthorize("hasPermission(#listeIds, 'manage') or hasRole('ROLE_LIVREUR')")
	@RequestMapping(value="/multiDelivery", method=RequestMethod.POST)
	@Transactional
	public String multiDelivery(@RequestParam List<Long> listeIds, Model uiModel) {
		
		for(Long id : listeIds){
			try {
				Card card = cardDaoService.findCard(id);
				if(card.getDeliveredDate() == null) {
					card.setDeliveredDate(LocalDateTime.now());
					cardDaoService.merge(card);
					logService.log(card.getId(), ACTION.MANAGER_DELIVERY, RETCODE.SUCCESS, "", card.getEppn(), null);
					if(!Etat.ENABLED.equals(card.getEtat())) {
						log.info("livraison of " + card.getCsn() + " -> activation");
						cardEtatService.setCardEtatAsync(card.getId(), Etat.ENABLED, "Activation suite à la livraison.", null, false, false);
					}
				}
			} catch (Exception e) {
				log.info("La carte avec l'id suivant n'a pas pu être marquée comme livrée : " + id, e);
			}
		}
		uiModel.asMap().clear();
		return "redirect:/manager/";
	}

    @PreAuthorize("hasPermission(#cardId, 'manage')")
	@RequestMapping(value="/replayValidationOrInvalidation/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String replayValidationOrInvalidation(@PathVariable("cardId") Long cardId, @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro, Model uiModel) {
		Card card = cardDaoService.findCard(cardId);
		cardEtatService.replayValidationOrInvalidation(card.getId(), validateServicesNames, resynchro);
		uiModel.asMap().clear();
		return "redirect:/manager/" + card.getId();
	}
	
    @PreAuthorize("hasPermission(#listeIds, 'manage')")
	@RequestMapping(value="/multiReplayValidationOrInvalidation", method=RequestMethod.POST)
	@Transactional
	public String multiReplayValidationOrInvalidation(@RequestParam List<Long> listeIds,  @RequestParam(required=false, defaultValue="") List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro,
			final RedirectAttributes redirectAttributes) {
		if(validateServicesNames != null){
			for(Long id : listeIds){
				try {
					Card card = cardDaoService.findCard(id);
					cardEtatService.replayValidationOrInvalidation(card.getId(), validateServicesNames, resynchro);
				} catch (Exception e) {
					log.info("La carte avec l'id suivant n'a pas été validée/invalidée : " + id, e);
				}
			}
		}else{
			redirectAttributes.addFlashAttribute("messageWarning", WARNING_MSG.concat("multivalidation"));
		}
		return "redirect:/manager";
	}
	
    @PreAuthorize("hasPermission(#cardId, 'manage')")
	@RequestMapping(value="/action/{cardId}", method=RequestMethod.POST)
	@Transactional
	public String actionEtat(@PathVariable("cardId") Long cardId, @RequestParam Etat etatFinal, @RequestParam(required=false) String comment,
							 @RequestParam(value="motif", required=false) MotifDisable motifDisable, @SessionAttribute(required = false) String printerEppn, Model uiModel) {
		Card card = cardDaoService.findCard(cardId);
		cardDaoService.merge(card);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();

		if(Etat.IN_PRINT.equals(etatFinal) && (Etat.REQUEST_CHECKED.equals(card.getEtat()) || eppn.equals(card.getEtatEppn())) && StringUtils.isEmpty(printerEppn)) {
			if(cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false)) {
				uiModel.addAttribute("cards", Arrays.asList(new Card[]{card}));
                Map<Card, TemplateCard> userTemplatesCards = new HashMap<>();
                userTemplatesCards.put(card, templateCardDaoService.getTemplateCard(card.getUser()));
                uiModel.addAttribute("userTemplatesCards", userTemplatesCards);
			}
			return "templates/manager/print-card";
		} else {
			uiModel.asMap().clear();
			if(Etat.RENEWED.equals(etatFinal)) {
				Card newCard = cardService.requestRenewalCard(card);
				if(newCard != null) {
					return "redirect:/manager/" + newCard.getId();
				} else {
					uiModel.addAttribute("messageError", "Cette carte n'a pu être renouvelée (action impossible)");
					return "redirect:/manager/" + card.getId();
				}
			} else {
				cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false, printerEppn);
				if(Etat.DISABLED.equals(etatFinal) && motifDisable != null) {
					card.setMotifDisable(motifDisable);
				}
				return "redirect:/manager/" + card.getId();
			}		
		}
	}
	
    @PreAuthorize("hasPermission(#cardId, 'manage')")
	@RequestMapping(value="/actionAjax", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String>  actionAjaxEtat(@RequestParam Long cardId, @RequestParam Etat etatFinal, @RequestParam(required=false) String comment, @SessionAttribute(required = false) String printerEppn, Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Card card = cardDaoService.findCard(cardId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        String response = "";
		cardDaoService.merge(card);
		uiModel.asMap().clear();
		String etatInitial = card.getEtat().name();
		cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false, printerEppn);
		logService.log(card.getId(), ACTION.RETOUCHE_ACTION_IND, RETCODE.SUCCESS, "", eppn,  etatInitial.concat("->").concat(etatFinal.name()));
		return new ResponseEntity<String>(response, headers, HttpStatus.OK);
	}
	
	public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
		// Not used : for Spring ROO
		return null;
	}
	
	/* 
	 * Permet de diriger par défaut le manager sur l'onglet le plus 'intéressant'
	 */
	@RequestMapping(produces = "text/html", params={"index=first"})
	@Transactional(readOnly = true)
	public String defaultSearch(@RequestParam Map<String, String> params,
								@PageableDefault(size = 10, direction = Sort.Direction.ASC, sort = "key") Pageable pageable,
    							@RequestParam(value = "index", required = false) String index,
								Model uiModel, HttpServletRequest request, HttpServletResponse response, Authentication auth) {
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		CardSearchBean searchBean = new CardSearchBean();
		searchBean.setType(permissionService.getDefaultTypeTab(roles));
		return search(params, pageable, searchBean, index, false, uiModel, request, response, auth);
	}
	
	
    @RequestMapping(produces = "text/html")
    @Transactional(readOnly = true)
    public String search(@RequestParam Map<String, String> params,
						 Pageable pageable,
                         @Valid CardSearchBean searchBean,
                         @RequestParam(value = "index", required = false) String index,
                         @RequestParam(required = false) Boolean zipExport,
                         Model uiModel,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Authentication auth) {

        if(BooleanUtils.isTrue(zipExport)) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
            if(roles.contains("ROLE_ADMIN")) {
                importExportController.export(response, searchBean);
                return null;
            }
        }

		// used to keep search params in the tabs links
		String queryParams = params.entrySet().stream()
				.filter(e -> !e.getKey().equals("page") && !e.getKey().equals("type") && !e.getKey().equals("index"))
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&"));
		uiModel.addAttribute("queryParams", queryParams);

		String sortFieldName = null;
		String sortOrder = null;
				Sort sort = pageable.getSort();
		if(sort.isUnsorted()) {
			// si searchBean.getSearchText() est précisé : tri par pertinence
			if(StringUtils.isEmpty(searchBean.getSearchText())) {
				sortFieldName = "dateEtat";
				sortOrder = "desc";
			}
		} else {
			sortFieldName = sort.stream().iterator().next().getProperty();
			sortOrder = sort.stream().iterator().next().getDirection().name();
		}
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();

		StopWatch stopWatch = new PrettyStopWatch();

		stopWatch.start("auth");
		String eppn = auth.getName();
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		List<String> userTypes = permissionService.getTypesTabs(roles);

		stopWatch.start("userPrefs");
		uiModel.addAttribute("userPrefs", getuserPrefs());

		stopWatch.start("base");
		Long cardsInprintCount = cardDaoService.countfindCardsByEtatEppnEqualsAndEtatEquals(eppn, Etat.IN_PRINT);
		uiModel.addAttribute("cardsInprintCount", cardsInprintCount);
		
    	int sizeNo = -1;
    	int firstResult = -1;
    	if(size == 0) {
    		Object sizeInSession = request.getSession().getAttribute("size_in_session");
    		size = sizeInSession != null ? (Integer)sizeInSession : 10;
    	}
    	if(!userTypes.contains(searchBean.getType())) {
    		searchBean.setType(permissionService.getDefaultTypeTab(roles));
    	}
    	searchBean.setAddress(formService.decodeUrlString(searchBean.getAddress()));
    	if(index !=null && "first".equals(index)){
	    	searchBean.setEditable(preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.EDITABLE.name()));
	    	searchBean.setOwnOrFreeCard(Boolean.valueOf(preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.OWNORFREECARD.name())));
			String userTypePref = preferencesService.getPrefValue(eppn, MANAGER_SEARCH_PREF.USERTYPE.name());
			if(StringUtils.isNotEmpty(userTypePref) && userTypes.contains(userTypePref)) {
				searchBean.setType(userTypePref);
			}
    	}
		if (searchBean.getType()==null || !userTypes.contains(searchBean.getType())) {
			// searchBean.getType obligatoire pour filtrage en fonction des rôles du gestionnaire
			// searchBean.getType() = null -> aucun résultat
			log.warn(String.format("Set userType of search to dummy value NOT_AUTHORIZED - %s doesn't have rights to search with userType = %s", eppn, searchBean.getType()));
			searchBean.setType("NOT_AUTHORIZED");
		}
    	if(searchBean.getLastTemplateCardPrinted()!=null && searchBean.getLastTemplateCardPrinted().getId()==null) {
    		searchBean.setLastTemplateCardPrinted(null);
    	}

		stopWatch.start("freefields");
    	if(searchBean.getFreeFieldValue()!= null && !searchBean.getFreeFieldValue().isEmpty()){
    		SortedMap<Integer, List<String>> noEmptyFreeFieldValue = new TreeMap<Integer, List<String>>(searchBean.getFreeFieldValue());
    		noEmptyFreeFieldValue.values().removeAll(Collections.singleton(""));
    		uiModel.addAttribute("collapse", noEmptyFreeFieldValue.size() > 0 ? "in" : "");
    		List<String> allFreeFieldValueList = new ArrayList<String>();    	
    		for(int j=0; j < Collections.max(searchBean.getFreeFieldValue().keySet())+1; j++) {
    			List<String> freeFieldValueList = searchBean.getFreeFieldValue().get(j);
    			if(freeFieldValueList == null) {
    				freeFieldValueList = new ArrayList<String>();
    			}
    			String freeFieldValueJoinString = StringUtils.join(freeFieldValueList.toArray(), ",");
    			allFreeFieldValueList.add(freeFieldValueJoinString);
    		}
    		String allFreeFieldValueJoinString = StringUtils.join(allFreeFieldValueList, ";");
    		uiModel.addAttribute("fieldsValue", allFreeFieldValueJoinString);
    		SortedMap<Integer, List<String>> freeFieldValueDecoded = new TreeMap<Integer, List<String>>() ; 	
    		HashMap<String, List<String>> fieldsValueEncoded = new HashMap<String, List<String>>() ; 
    		for (Map.Entry<Integer, List<String>> freeFieldEncoded : searchBean.getFreeFieldValue().entrySet()) {
    			List<String> entryValuesDecoded = new ArrayList<String>();
    			for(String value : freeFieldEncoded.getValue()){
    				entryValuesDecoded.add(formService.decodeUrlString(value));
    			}
    			freeFieldValueDecoded.put(freeFieldEncoded.getKey(), entryValuesDecoded);
    			fieldsValueEncoded.put(freeFieldEncoded.getKey().toString(), freeFieldEncoded.getValue());
    		}
    		uiModel.addAttribute("fieldsValueEncoded", fieldsValueEncoded);
    		searchBean.setFreeFieldValue(freeFieldValueDecoded);	
    	} else {
    		uiModel.addAttribute("collapse", searchBean.getFreeFieldValue() == null ? "" : "in");
    	}

		stopWatch.start("cards search count");
    	sizeNo = size;
    	firstResult = page * sizeNo;
    	long countCards = cardDaoService.countFindCards(searchBean, eppn);
		stopWatch.start("cards search");
    	List<Card> cards = cardDaoService.findCards(searchBean, eppn, new SortCriterion(sortFieldName, sortOrder)).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList();
        float nrOfPages = (float) countCards / sizeNo;

		stopWatch.start("filters");
		// TODO remove used of types (utilisé dans les tags roo/jspx)
    	uiModel.addAttribute("types", userTypes);
		uiModel.addAttribute("userTypes", userTypes);
    	uiModel.addAttribute("etats", cardEtatService.getDistinctEtats());
    	uiModel.addAttribute("lastTemplateCardsPrinted", userInfoService.getDistinctLastTemplateCardsPrinted());
    	List<Long> nbCards = userDaoService.getDistinctNbCards();
		uiModel.addAttribute("nbCards", nbCards);
		List<Long> nbRejets = cardDaoService.getDistinctNbRejets();
		uiModel.addAttribute("nbRejets", nbRejets);
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	uiModel.addAttribute("searchBean", searchBean);
    	uiModel.addAttribute("cards", cards);
    	uiModel.addAttribute("countCards",  countCards);
    	uiModel.addAttribute("managePermission",  permissionService.hasManagePermission(roles, searchBean.getType()));
		uiModel.addAttribute("consultPermission",  permissionService.hasConsultPermission(roles, searchBean.getType()));
    	uiModel.addAttribute("selectedType",  searchBean.getType());
    	uiModel.addAttribute("freeFields",  formService.getFieldsList());
    	uiModel.addAttribute("nbFields", new String[formService.getNbFields()]);
    	uiModel.addAttribute("size",  size);
		stopWatch.start("addresses filter");
    	List<String> addresses = userInfoService.getListAddresses(searchBean.getType(), searchBean.getEtat());
    	Map<String, String> addressesMap = formService.getMapWithUrlEncodedString(addresses);
    	uiModel.addAttribute("addresses", MapUtils.sortByValue(addressesMap));
    	searchBean.setAddress(formService.encodeUrlString(searchBean.getAddress()));
		uiModel.addAttribute("eppn", eppn);
    	addDateTimeFormatPatterns(uiModel);
		stopWatch.stop();

		log.trace(stopWatch.prettyPrint());

        PageRequest pageRequest = PageRequest.of(page, sizeNo, sort);
        Page<Card> pageCards = new PageImpl<>(cards, pageRequest, countCards);
        uiModel.addAttribute("cards", pageCards);
        return "templates/manager/list";
    }

    @PreAuthorize("hasPermission(#listeIds, 'manage')")
    @RequestMapping(value="/multiUpdate", method = RequestMethod.POST)
    // No @Transactional here so that exception catching on ManagerController.multiUpdate works well
    public String multiUpdate(@RequestParam(value="comment", defaultValue= "") String comment, 
    		@RequestParam List<Long> listeIds, @RequestParam(value="etatFinal", required=false) Etat etatFinal, @RequestParam(value="forcedEtatFinal", required=false) Etat forcedEtatFinal,
    		@RequestParam(value="updateEtatAdmin", required=false) String updateEtatAdmin, @RequestHeader("User-Agent") String userAgent, @SessionAttribute(required = false) String printerEppn,
							  final RedirectAttributes redirectAttributes, Model uiModel, HttpServletRequest request) {

    	List<Long> listeIdsErrors = new ArrayList<Long>();
    	List<Long> listeIdsOk = new ArrayList<Long>();
    	List<Card> cards = new ArrayList<Card>();
    	if(listeIds!=null) {
    		int i = 0;
    		// we want to preserve order so we reverse listeIds
    		Collections.reverse(listeIds);
    		for(Long id : listeIds) {
    			try {
    				i++;
    				//if(i==2) throw new RuntimeException("yop");
    				Card card = cardDaoService.findCard(id);
    				card.setCommentaire(comment);
    				
    				if(updateEtatAdmin != null && forcedEtatFinal!=null) {
    					Etat firstEtat = card.getEtat();
    					card.setEtat(forcedEtatFinal);
    					cardDaoService.merge(card);
    					card.getUser().setHasCardRequestPending(cardEtatService.hasRequestCard(card.getEppn()));
    					userDaoService.merge(card.getUser());
    					log.info("Changement d'etat manuel à " + forcedEtatFinal + " pour la carte de " + card.getEppn());
    					logService.log(card.getId(), ACTION.FORCEDUPDATE, RETCODE.SUCCESS, firstEtat.name() + " -> " + forcedEtatFinal, card.getEppn(), null);
    				} else {
    					if(Etat.RENEWED.equals(etatFinal)) {
    						cardService.requestRenewalCard(card);
    					} else {
	    					if(cardEtatService.setCardEtat(card, etatFinal, comment, comment, true, false, printerEppn)) {
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

    	if(Etat.IN_PRINT.equals(etatFinal) && StringUtils.isEmpty(printerEppn)) {
    		uiModel.addAttribute("cards", cards);
            Map<Card, TemplateCard> userTemplatesCards = new HashMap<>();
            for(Card card : cards) {
                userTemplatesCards.put(card, templateCardDaoService.getTemplateCard(card.getUser()));
            }
            uiModel.addAttribute("userTemplatesCards", userTemplatesCards);
    		return "templates/manager/print-card";
    	}

    	/*
    	CardSearchBean searchBean = new CardSearchBean();
    	searchBean.setEditable("true");
    	searchBean.setOwnOrFreeCard(true);
    	searchBean.setEtat(etatFinal);

    	return search(searchBean, null, null, "dateEtat", "DESC", uiModel, request);
    	 */
    	return "redirect:/manager?" + request.getQueryString();
    }   
	
    @PreAuthorize("hasPermission(#cardIds, 'manage')")
	@RequestMapping(value="/getMultiUpdateForm")
	@Transactional(readOnly = true)
	public String getMultiUpdateForm(@RequestParam List<Long> cardIds, @SessionAttribute(required = false) String printerEppn, Model uiModel, HttpServletRequest request) {
		if(cardIds.isEmpty()) {
			uiModel.addAttribute("cardIds", cardIds);
		} else {
			List<Etat> etatsAvailable = cardEtatService.getEtatsAvailable4Cards(cardIds, printerEppn);
			Map<Etat, List<CardActionMessage>> actionMessages  = cardActionMessageService.getCardActionMessagesForCards(cardIds);
			uiModel.addAttribute("actionMessages", actionMessages);
			uiModel.addAttribute("allEtats", Arrays.asList(Etat.values()));
			uiModel.addAttribute("etatsAvailable", etatsAvailable);
			uiModel.addAttribute("cardIds", cardIds);
			uiModel.addAttribute("etatInit", cardDaoService.findCard(cardIds.get(0)).getEtat());
			uiModel.addAttribute("deliveredFlag", cardEtatService.areCardsReadyToBeDelivered(cardIds));
			uiModel.addAttribute("validatedFlag", cardEtatService.areCardsReadyToBeValidated(cardIds));
		}
        uiModel.addAttribute("userAgent", request.getHeader("User-Agent"));
		return "templates/manager/multiUpdateForm";
	}
	
    @PreAuthorize("hasPermission(#id, 'manage')")
	@RequestMapping(value="/updatePhoto", method = RequestMethod.POST)
	@Transactional
	public String updatePhoto(@RequestParam("cardId") Long id, @RequestParam("imageData") String imageData, Model uiModel, final RedirectAttributes redirectAttributes){
		
		if (imageData.isEmpty()) {
			log.info("Aucune image disponible");
			redirectAttributes.addFlashAttribute("messageInfo", "error_leocarte_emptyfile");
		} else {
			if(id!=null){
				Card card = cardDaoService.findCard(id);
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
                    bigFileDaoService.setBinaryFileStream(card.getPhotoFile().getBigFile(), inputStream, fileSize);
                    LocalDateTime currentTime = LocalDateTime.now();
					card.getPhotoFile().setSendTime(currentTime);
					cardDaoService.merge(card);
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
	
    @PreAuthorize("hasPermission(#id, 'manage')")
	@RequestMapping(value="/updatePhotoAjax", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String>  updatePhotoAjax(@RequestParam("cardId") Long id, @RequestParam("imageData") String imageData, Model uiModel, final RedirectAttributes redirectAttributes){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		String response = "";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();	
		if (imageData.isEmpty()) {
			log.info("Aucune image disponible");
			redirectAttributes.addFlashAttribute("messageInfo", "error_leocarte_emptyfile");
		} else {
			if(id!=null){
				Card card = cardDaoService.findCard(id);
				try {
					String encoding = cardService.getPhotoParams().get("encoding");
					int contentStartIndex = imageData.indexOf(encoding) + encoding.length();
					byte[] bytes = Base64.decodeBase64(imageData.substring(contentStartIndex));  
					Long fileSize = Long.valueOf(Integer.valueOf(bytes.length));
					String contentType = cardService.getPhotoParams().get("contentType");
					card.getPhotoFile().setContentType(contentType);
					card.getPhotoFile().setFileSize(fileSize);
					log.info("Upload and set file in DB with filesize = " + fileSize);
                    bigFileDaoService.setBinaryFile(card.getPhotoFile().getBigFile(), bytes);
					card.getPhotoFile().setSendTime(LocalDateTime.now());
					cardDaoService.merge(card);
					logService.log(card.getId(), ACTION.UPDATEPHOTO, RETCODE.SUCCESS, "", card.getEppn(), null);
					log.info("Succès de la mise à jour de la photo pour l'utilisateur " +   card.getEppn());
					redirectAttributes.addFlashAttribute("messageSuccess", SUCCESS_MSG.concat("updatePhoto"));
					response = imageData;
					logService.log(card.getId(), ACTION.RETOUCHE_PHOTO, RETCODE.SUCCESS, "", eppn, "");
					} catch (Exception e) {
						logService.log(card.getId(), ACTION.UPDATEPHOTO, RETCODE.FAILED, "", card.getEppn(), null);
						redirectAttributes.addFlashAttribute("messageInfo", ERROR_MSG.concat("updatePhoto"));
						log.error("Echec lors de la mise à jour de la photo pour l'utilisateur " +   card.getEppn(), e);
				}
			}
		}
		
		return new ResponseEntity<String>(response, headers, HttpStatus.OK);
	}

	@RequestMapping(value="/csvSearch", method = RequestMethod.GET)
	public void getCsvSearch(@ModelAttribute("searchBean") CardSearchBean searchBean, @RequestParam(value="fields",required=false) List<String> fields, HttpServletResponse response) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		List<String> userTypes = permissionService.getTypesTabs(roles);
		if(!userTypes.contains(searchBean.getType())) {
			searchBean.setType(permissionService.getDefaultTypeTab(roles));
		}
		getCsvFromSearch(searchBean, fields, eppn, response);
	}
	
	public void getCsvFromSearch(CardSearchBean searchBean, List<String> fields, String eppn, HttpServletResponse response) throws IOException {
		response.setContentType("text/csv");
		String reportName = "cards.csv";
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
		response.setHeader("Content-disposition", "attachment;filename=" + reportName);
    	if(searchBean.getLastTemplateCardPrinted()!=null && searchBean.getLastTemplateCardPrinted().getId()==null) {
    		searchBean.setLastTemplateCardPrinted(null);
    	}
    	searchBean.setAddress(formService.decodeUrlString(searchBean.getAddress()));
    	
    	if(searchBean.getFreeFieldValue()!= null && !searchBean.getFreeFieldValue().isEmpty()){
    		SortedMap<Integer, List<String>> freeFieldValueDecoded = new TreeMap<Integer, List<String>>() ; 		
    		for (Map.Entry<Integer, List<String>> freeFieldEncoded : searchBean.getFreeFieldValue().entrySet()) {
    			List<String> entryValuesDecoded = new ArrayList<String>();
    			for(String value : freeFieldEncoded.getValue()){
    				entryValuesDecoded.add(formService.decodeUrlString(value));
    			}
    			freeFieldValueDecoded.put(freeFieldEncoded.getKey(), entryValuesDecoded);
    		}
    		// freeFieldValue dans searchBean doit correspondre aux valeurs en base : valeurs non url encodées
    		searchBean.setFreeFieldValue(freeFieldValueDecoded);	
    	}

		importExportService.exportCsv2OutputStream(searchBean, eppn, fields, response.getOutputStream());
	}
	
	@RequestMapping(value="/savePrefs")
	public String  savePrefs(@RequestParam(value="editable", required=false) String editable, @RequestParam(value="ownOrFreeCard", required=false) String ownOrFreeCard,
							 @RequestParam(value="userType", required=false) String userType, @RequestParam(value="listNoImg", required=false) String listNoImg, Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
		editable = (editable!= null)? "true" : "all";
		ownOrFreeCard = (ownOrFreeCard!= null)? "true" : "false";
		
		try {
			preferencesService.setPrefs(eppn, MANAGER_SEARCH_PREF.EDITABLE.name(), editable);
			preferencesService.setPrefs(eppn, MANAGER_SEARCH_PREF.OWNORFREECARD.name(), ownOrFreeCard);
			preferencesService.setPrefs(eppn, MANAGER_SEARCH_PREF.USERTYPE.name(), userType);
            preferencesService.setPrefs(eppn, MANAGER_SEARCH_PREF.LIST_NO_IMG.name(), listNoImg);
		} catch (Exception e) {
			log.warn("Impossible de sauvegarder les préférences", e);
		}
		 
		uiModel.asMap().clear();
		  
		return "redirect:/manager?index=first";
	}
	
	@RequestMapping(value="/bordereau", method = RequestMethod.GET)
	public String getBordereau(@ModelAttribute("searchBean") CardSearchBean searchBean, Model uiModel){
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
		
    	if(searchBean.getLastTemplateCardPrinted()!=null && searchBean.getLastTemplateCardPrinted().getId()==null) {
    		searchBean.setLastTemplateCardPrinted(null);
    	}
    	searchBean.setAddress(formService.decodeUrlString(searchBean.getAddress()));
    	
    	if(searchBean.getFreeFieldValue()!= null && !searchBean.getFreeFieldValue().isEmpty()){
    		SortedMap<Integer, List<String>> freeFieldValueDecoded = new TreeMap<Integer, List<String>>() ; 		
    		for (Map.Entry<Integer, List<String>> freeFieldEncoded : searchBean.getFreeFieldValue().entrySet()) {
    			List<String> entryValuesDecoded = new ArrayList<String>();
    			for(String s: freeFieldEncoded.getValue()){
    				entryValuesDecoded.add(formService.decodeUrlString(s));
    			}
    			freeFieldValueDecoded.put(freeFieldEncoded.getKey(), entryValuesDecoded);
    		}
    		searchBean.setFreeFieldValue(freeFieldValueDecoded);	
    	}
    	
    	Long nbCards = cardDaoService.countFindCards(searchBean, eppn);
    	boolean msgbordereau = false;
    	if(nbCards < 500){
    		List<Card> cards = cardDaoService.findCards(searchBean, eppn, new SortCriterion("address", "ASC")).getResultList();
    		uiModel.addAttribute("displayPhoto", appliConfigService.getPhotoBordereau());

            List<CardWithCounter> cardsWithCounters = new ArrayList<>();
            String lastAddress = null;
            int counter = 0;

            for (Card card : cards) {
                boolean isNewAddress = !card.getUserAccount().getAddress().equals(lastAddress);
                if (isNewAddress) {
                    counter = 1;
                    lastAddress = card.getUserAccount().getAddress();
                } else {
                    counter++;
                }
                cardsWithCounters.add(new CardWithCounter(card, counter, isNewAddress));
            }
            uiModel.addAttribute("cardsWithCounters", cardsWithCounters);
    	}else{
    		msgbordereau = true;
    	}
    	uiModel.addAttribute("msgbordereau", msgbordereau);
		
		
		return "templates/manager/bordereau";
	}
	
	@PreAuthorize("hasPermission(#listeIds, 'manage')")
	@RequestMapping(value="/retouche", method = RequestMethod.POST)
	public String getRetouchePage(@RequestParam("listeIds") List<Long> listeIds, Model uiModel) {
		List<Card> cards = cardDaoService.findAllCards(listeIds);
		uiModel.addAttribute("cardIds",listeIds);
		uiModel.addAttribute("cards",cards);
		String joinIds = StringUtils.join(listeIds.toArray(new Long[listeIds.size()]), ",");
		uiModel.addAttribute("joinIds",joinIds);
		
		return "templates/manager/retouche";
	}
	

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Card card, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, card);
            return "templates/manager/create";
        }
        uiModel.asMap().clear();
        cardDaoService.persist(card);
        return "redirect:/manager/" + encodeUrlPathSegment(card.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Card());
        return "templates/manager/create";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Card card, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, card);
            return "templates/manager/update";
        }
        uiModel.asMap().clear();
        cardDaoService.merge(card);
        return "redirect:/manager/" + encodeUrlPathSegment(card.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, cardDaoService.findCard(id));
        return "templates/manager/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Card card = cardDaoService.findCard(id);
        cardDaoService.remove(card);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/manager";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("card_requestdate_date_format", "dd/MM/yyyy");
        uiModel.addAttribute("card_dateetat_date_format", "dd/MM/yyyy");
    }

	void populateEditForm(Model uiModel, Card card) {
        uiModel.addAttribute("card", card);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("photofiles", photoFileDaoService.findAllPhotoFiles());
        uiModel.addAttribute("templatecards", templateCardDaoService.findAllTemplateCards());
        uiModel.addAttribute("users", userDaoService.findAllUsers());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}

