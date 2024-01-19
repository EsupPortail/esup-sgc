package org.esupportail.sgc.web.wsrest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import eu.bitwalker.useragentutils.UserAgent;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * This Web Service provides commands for interaction with sgc from informations systems
 * With this, you can 
 *  - request remotely a card, 
 *   - ask synchronization of user so that sgc is synchronized quickly with your information system
 *   - ...  
 *
 */
@RequestMapping("/wsrest/api")
@Controller
public class WsRestEsupSgcApiController extends AbstractRestController {
	
	public final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ManagerCardController managerCardController;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	UserService userService;
	
	@Resource
	CardService cardService;
	
	@Autowired(required = false)
	CardIdsService cardIdsService;
	
	@Resource 
	UserInfoService userInfoService;
	
	@Resource
	LogService logService;
	
	@Resource
	LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;
	
	@Resource
	ExternalCardService externalCardService;
	
	@Resource
	CrousService crousService;
	
	
	/**
	 * Example to use it :
	 * curl   -F "eppn=toto@univ-ville.fr" -F "difPhotoTransient=true" -F "crousTransient=true" -F "europeanTransient=true" -F "PhotoFile.file=@/tmp/photo-toto.jpg" https://esup-sgc.univ-ville.fr/wsrest/api
	 */
	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> cardRequest(@Valid Card card, BindingResult bindingResult, Model uiModel, @RequestHeader("User-Agent") String userAgent, HttpServletRequest request) throws IOException {	
		
		HttpHeaders responseHeaders = new HttpHeaders();

		if (bindingResult.hasErrors()) {
			log.warn(bindingResult.getAllErrors().toString());
			return new ResponseEntity<String>(bindingResult.getAllErrors().toString(), responseHeaders, HttpStatus.UNPROCESSABLE_ENTITY);
		}	

		String eppn = card.getEppn();
		
		synchronized (eppn.intern()) {
			
			User user = User.findUser(eppn);
			if(user == null) {
				user = new User();
				user.setEppn(eppn);
				user.persist();
			}
			
			resynchronisationUserService.synchronizeUserInfoNoTx(eppn);
			ldapGroup2UserRoleService.syncUser(eppn);
			
			// check rights 
			if(userService.isFirstRequest(user) || userService.isFreeRenewal(user) ||  userService.isPaidRenewal(user) || cardEtatService.hasRejectedCard(eppn)) {
			
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
			
					if (card.getPhotoFile().getFile() == null || card.getPhotoFile().getFile().isEmpty()) {
						if(user.getDefaultPhoto() != null && user.getDefaultPhoto().getBigFile().getMd5()!=null) {
							card.getPhotoFile().setFilename(user.getDefaultPhoto().getFilename());
							card.getPhotoFile().setContentType(user.getDefaultPhoto().getContentType());
							card.getPhotoFile().setFileSize(user.getDefaultPhoto().getFileSize());
							card.getPhotoFile().getBigFile().setBinaryFile(user.getDefaultPhoto().getBigFile().getBinaryFile());
						} else {						
							log.info("Aucune photo pour la demande de " + eppn);
							return new ResponseEntity<String>("Aucune photo pour lda demande de " + eppn, responseHeaders, HttpStatus.UNPROCESSABLE_ENTITY);
						}
					} else {
						String filename = card.getPhotoFile().getFile().getOriginalFilename();
						Long fileSize = card.getPhotoFile().getFile().getSize();
						String contentType = card.getPhotoFile().getFile().getContentType();
						card.getPhotoFile().setFilename(filename);
						card.getPhotoFile().setContentType(contentType);
						card.getPhotoFile().setFileSize(fileSize);
						log.info("Upload and set file in DB with filesize = " + fileSize);
						card.getPhotoFile().getBigFile().setBinaryFile(card.getPhotoFile().getFile().getBytes());
					}
					Calendar cal = Calendar.getInstance();
					Date currentTime = cal.getTime();
					card.getPhotoFile().setSendTime(currentTime);
					card.setUserAccount(user);
					
					if(card.getId() !=null){
						card.setNbRejets(Card.findCard(card.getId()).getNbRejets());
						card.merge();
					} else {
						card.setNbRejets(Long.valueOf(0));
						card.persist();
					}
					
					card.setDueDate(user.getDueDate());
					
					if(card.getEuropeanTransient()!=null && card.getEuropeanTransient()) {
						user.setEuropeanStudentCard(true);
					}
					
					if(card.getCrousTransient()!=null && card.getCrousTransient()) {
						user.setCrous(true);
						userInfoService.setAdditionalsInfo(user, request);
					}
					user.setDifPhoto(card.getDifPhotoTransient());
					String reference = cardService.getPaymentWithoutCard(eppn);
					if(!reference.isEmpty()){
						card.setPayCmdNum(reference);
					}
					user.merge();
					card.merge();
					logService.log(card.getId(), ACTION.DEMANDE, RETCODE.SUCCESS, "", eppn, request.getRemoteAddr());
					log.info("Succès de la demande de carte pour l'utilisateur " +  eppn);
					
					// TODO : use cardEtatService.setCardEtat !
					cardEtatService.sendMailInfo(null, Etat.NEW, user, null);
					
					return new ResponseEntity<String>(card.getId().toString(), responseHeaders, HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<String>(eppn + " tried to request card but he has no rights to do it.", responseHeaders, HttpStatus.FORBIDDEN);
			}
		}
		return new ResponseEntity<String>(eppn + " LOCKED.", responseHeaders, HttpStatus.LOCKED);
	}
	
	/**
	 * Example to use it :
	 * curl https://esup-sgc.univ-ville.fr/wsrest/api/sync?eppn=toto@univ-ville.fr
	 */
	@Transactional
	@RequestMapping(value="/sync", method = RequestMethod.GET)
	public ResponseEntity<String> sync(@RequestParam String eppn) {	
		
		User user = User.findUser(eppn);
		if(user == null) {
			user = new User();
			user.setEppn(eppn);
			user.persist();
		}
		
		resynchronisationUserService.synchronizeUserInfo(eppn);
		ldapGroup2UserRoleService.syncUser(eppn);
		
		return new ResponseEntity<String>(eppn + " has been synchronized.", HttpStatus.OK);
	}
	
	/**
	 * Example to use it :
	 * curl 'https://esup-sgc.univ-ville.fr/wsrest/api/replayValidationOrInvalidation?eppn=toto@univ-ville.fr&validateServicesNames=ldapValidateService&validateServicesNames=accessControlService&resynchro=true'
	 */
	@Transactional
	@RequestMapping(value="/replayValidationOrInvalidation", method = RequestMethod.GET)
	public ResponseEntity<String> replayValidationOrInvalidation(@RequestParam String eppn, @RequestParam List<String> validateServicesNames, @RequestParam(required=false, defaultValue="false") Boolean resynchro) {	
		long nbCardsOk  = 0;
		long nbCardsKo  = 0;
		User user = User.findUser(eppn);
		for(Card card : user.getCards()) {
			try {
				cardEtatService.replayValidationOrInvalidation(card.getId(), validateServicesNames, resynchro);
				nbCardsOk++;
			} catch (Exception e) {
				log.warn(String.format("La carte %s n'a pas été validée/invalidée", card) , e);
				nbCardsKo++;
			}
		}
		String msg = String.format("%d cartes bien revalidées/invalidées (et %d cartes on posé problème, cf logs) sur les services %s pour %s", nbCardsOk, nbCardsKo, validateServicesNames, eppn);
		log.info(msg);
		return new ResponseEntity<String>(msg, HttpStatus.OK);	}
	
	
	/**
	 * Example to use it :
	 * curl -X POST https://esup-sgc.univ-ville.fr/wsrest/api/externalCardEnable?eppn=toto@univ-ville.fr&crous=true&difPhoto=true
	 */
	@RequestMapping(value="/externalCardEnable", method = RequestMethod.POST)
	public ResponseEntity<String> enableExternalCard(@RequestParam String eppn, @RequestParam(required=false, defaultValue = "false") Boolean crous, @RequestParam(required=false, defaultValue = "false") Boolean difPhoto) {
		try {
			Card externalCard = externalCardService.importExternalCard(eppn, null);
			externalCard.setCrous(crous);
			externalCard.setDifPhoto(difPhoto);
			externalCard.merge();
			cardEtatService.setCardEtatAsync(externalCard.getId(), Etat.ENABLED, "Importation d'une Léocarte extérieure", "Importation d'une Léocarte extérieure", false, false);
		} catch (Exception e) {
			String errorMessage = "problème lors de l'importation de la carte extérieure de " + eppn + " : " + e.getMessage();
			log.error(errorMessage, e);
			return new ResponseEntity<String>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("Importation de la carte extérieure de " + eppn + " OK", HttpStatus.OK);
	}
	
	/**
	 * Example to use it :
	 * curl 'https://esup-sgc.univ-ville.fr/wsrest/api/get?eppn=toto@univ-ville.fr&eppn=titi@univ-ville.fr'
	 * @throws JsonProcessingException 
	 */
	@Transactional
	@RequestMapping(value="/get", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String>  get(@RequestParam(value="eppn") List<String> eppns) throws JsonProcessingException {
		List<User> users = new ArrayList<User>();
		for(String eppn : eppns) {
			User user = User.findUser(eppn);
			if(user != null) {
				users.add(user);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("userFilter", SimpleBeanPropertyFilter.filterOutAllExcept("eppn", "cards", "crous", "europeanStudentCard", "difPhoto", "name", "firstname", "birthday", "email", "dueDate"))
				.addFilter("cardFilter", SimpleBeanPropertyFilter.filterOutAllExcept("id", "csn", "etat", "dateEtat", "desfireIds", "escnUid"));
		String jsonUsers = mapper.writer(filters).writeValueAsString(users);
		return new ResponseEntity<String>(jsonUsers, HttpStatus.OK);
	}
	
	
	/**
	 * Example to use it :
	 * curl 'https://esup-sgc.univ-ville.fr/wsrest/api/getCrousRightHolder?eppnOrEmail=toto@univ-ville.fr'
	 */
	@Transactional
	@ResponseBody
	@RequestMapping(value="/getCrousRightHolder", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public RightHolder get(@RequestParam String eppnOrEmail) {
		return crousService.getRightHolder(eppnOrEmail, eppnOrEmail);
	}

	/**
	 * Example to use it :
	 * curl 'https://esup-sgc.univ-ville.fr/wsrest/api/csvSearch?etat=ENABLED&fields=eppn'
	 */
	@RequestMapping(value="/csvSearch", method = RequestMethod.GET)
	public void getCsvFromSearch(@Valid  CardSearchBean searchBean, @RequestParam List<String> fields, HttpServletResponse response) throws IOException {
		managerCardController.getCsvFromSearch(searchBean, fields, null, response);
	}

	/*
	* Example to use it :
	* curl -X POST https://esup-sgc.univ-ville.fr/wsrest/api/renew/1818864
	*/
	@Transactional
	@RequestMapping(value="/renew/{cardId}", method = RequestMethod.POST)
	public ResponseEntity<Long> requestRenewalCard(@PathVariable("cardId") Long cardId) {
		Card card = Card.findCard(cardId);
		Card newCard = cardService.requestRenewalCard(card);
		if(newCard != null) {
			return new ResponseEntity<Long>(newCard.getId(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Long>(-1L, HttpStatus.NOT_ACCEPTABLE);
		}
	}


	/*
	 * Example to use it :
	 * curl -d "etat=REQUEST_CHECKED" https://esup-sgc.univ-ville.fr/wsrest/api/setCardEtat/1818864
	 */
	@Transactional
	@RequestMapping(value="/setCardEtat/{cardId}", method = RequestMethod.POST)
	public ResponseEntity<Boolean> setCardEtat(@PathVariable("cardId") Long cardId, @RequestParam Etat etat, @RequestParam(required = false) String comment, HttpServletRequest request) {
		Card card = Card.findCard(cardId);
		Boolean r = cardEtatService.setCardEtat(card, etat, comment, null, false, false);
		log.info("Changement d'état de la carte %s (%s) à %s via WS depuis l'IP %s -> %s", card.getId(), card.getEppn(), etat, request.getRemoteAddr(), r);
		return new ResponseEntity<Boolean>(r, HttpStatus.OK);
	}
	
}
