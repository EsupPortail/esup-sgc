package org.esupportail.sgc.web.wsrest;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.ldap.LdapGroup2UserRoleService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.bitwalker.useragentutils.UserAgent;

@Transactional
@RequestMapping("/wsrest/api")
@Controller
public class WsRestEsupSgcApiController {
	
	public final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ManagerCardController managerCardController;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	UserService userService;
	
	@Resource
	CardService cardService;
	
	@Resource
	CardIdsService cardIdsService;
	
	@Resource 
	UserInfoService userInfoService;
	
	@Resource
	LogService logService;
	
	@Resource
	LdapGroup2UserRoleService ldapGroup2UserRoleService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;
	
	
	/**
	 * Example to use it :
	 * curl   -F "eppn=toto@univ-ville.fr" -F "difPhotoTransient=true" -F "crousTransient=true" -F "PhotoFile.file=@/tmp/photo-toto.jpg" https://esup-sgc.univ-ville.fr/wsrest/api
	 */
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
			
			resynchronisationUserService.synchronizeUserInfo(eppn);
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
			
					if (card.getPhotoFile().getFile().isEmpty()) {
						log.info("Aucune photo pour lda demande de " + eppn);
						return new ResponseEntity<String>("Aucune photo pour lda demande de " + eppn, responseHeaders, HttpStatus.UNPROCESSABLE_ENTITY);
					} else {
						String filename = card.getPhotoFile().getFile().getOriginalFilename();
						Long fileSize = card.getPhotoFile().getFile().getSize();
						String contentType = card.getPhotoFile().getFile().getContentType();
						card.getPhotoFile().setFilename(filename);
						card.getPhotoFile().setContentType(contentType);
						card.getPhotoFile().setFileSize(fileSize);
						log.info("Upload and set file in DB with filesize = " + fileSize);
						card.getPhotoFile().getBigFile().setBinaryFile(card.getPhotoFile().getFile().getBytes());
						Calendar cal = Calendar.getInstance();
						Date currentTime = cal.getTime();
						card.getPhotoFile().setSendTime(currentTime);
						if(card.getId() !=null){
							card.setNbRejets(Card.findCard(card.getId()).getNbRejets());
							card.merge();
						} else {
							card.setNbRejets(Long.valueOf(0));
							card.persist();
						}
						
						card.setUserAccount(user);
						card.setDueDate(user.getDueDate());
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
						cardEtatService.sendMailInfo(null, Etat.NEW, user, null, false);
						
						return new ResponseEntity<String>(eppn + " request OK.", responseHeaders, HttpStatus.OK);
					}
				}
			} else {
				return new ResponseEntity<String>(eppn + " tried to request card but he has no rights to do it.", responseHeaders, HttpStatus.FORBIDDEN);
			}
		}
		return new ResponseEntity<String>(eppn + " LOCKED.", responseHeaders, HttpStatus.LOCKED);
	}
	
}


