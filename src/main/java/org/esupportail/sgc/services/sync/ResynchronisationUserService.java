package org.esupportail.sgc.services.sync;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.crous.AuthApiCrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ResynchronisationUserService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	private UserInfoService userInfoService;
	
	@Resource
	AuthApiCrousService authApiCrousService;
	
	@Resource
	ApiEscrService apiEscrService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	AccessControlService accessControlService;
	
	@Async("synchroExecutor")
	public void synchronizeUserInfoAsync(String eppn) {
		synchronizeUserInfo(eppn);
	}
	
	public boolean synchronizeUserInfo(String eppn) {
		boolean updated = false;
		log.debug("Synchronize of user " + eppn + " called.");
		User user = User.findUser(eppn);
		if(log.isTraceEnabled()) {
			log.trace("user : " + user);
		}
		User dummyUser = new User();
		dummyUser.setEppn(user.getEppn());
		dummyUser.setCrous(user.getCrous());
		dummyUser.setEditable(user.isEditable());
		dummyUser.setEuropeanStudentCard(user.getEuropeanStudentCard());
		dummyUser.setExternalCard(user.getExternalCard());
		dummyUser.setNbCards(user.getNbCards());
		dummyUser.setRequestFree(user.isRequestFree());
		dummyUser.setDifPhoto(user.getDifPhoto());
		userInfoService.setAdditionalsInfo(dummyUser, null);
		if(log.isTraceEnabled()) {
			log.trace("'user' computed from userInfoServices : " + dummyUser);
		}
		boolean accessControlMustUpdate = false;
		if(!dummyUser.fieldsEquals(user) && (user.getDueDate() != null || dummyUser.getDueDate() != null)) {
			if(dummyUser.getDueDate() == null && user.getDueDate().after(new Date())) {
				// TODO : avec shib, quelle dueDate ??
				log.warn(user.getEppn() + " has no more dueDate from sql/shib/ldap userInfoService (no more entry) "
						+ "and its recording duedate is at the moment after today -> we should force the dueDate to be today ... ?!");
				//user.setDueDate(new Date());
			}
			
			// if user is caduc and have only cards caduc we don't synchronize
			if(dummyUser.getDueDateIncluded() != null && dummyUser.getDueDateIncluded().before(new Date()) || dummyUser.getDueDateIncluded() == null && user.getDueDateIncluded() != null && user.getDueDateIncluded().before(new Date())) {
				boolean haveOnlyCaducCards = true; 
				for(Card card : user.getCards()) {
					if(!Etat.CADUC.equals(card.getEtat())) {
						haveOnlyCaducCards = false;
					}
				}
				if(haveOnlyCaducCards) {
					log.trace(eppn + " is already caduc - no need to synchronize");
					return false;
				} else {
					log.debug(eppn + " is caduc - but he has cards that are not caduc - need to synchronize");
				}
			}
			
			log.info("Synchronize of user " + eppn + " is needed.");
			userInfoService.setAdditionalsInfo(user, null);
			user.merge();
			// we synchronize users with crous only if user had enable crous and had an email and had cards - same for europeanStudentCard
			if(user.getEmail() != null && !user.getEmail().isEmpty() && Card.countfindCardsByEppnEqualsAndEtatIn(eppn, Arrays.asList(new Etat[] {Etat.ENABLED, Etat.DISABLED, Etat.CADUC}))>0) {
				if(user.getCrous()) {
					authApiCrousService.postOrUpdateRightHolder(user.getEppn());
				}
				if(user.getEuropeanStudentCard()) {
					apiEscrService.postOrUpdateEscrStudent(user.getEppn());
				}
			}
			if(log.isTraceEnabled()) {
				log.trace("user is now : " + user);
			}
			log.debug("Synchronize of user " + eppn + " was needed : done.");
			updated = true;
		} else {
			log.debug("Synchronize of user " + eppn + " was not needed.");
		}
		if(dummyUser.getExternalCard().getCsn() != null && !dummyUser.getExternalCard().getCsn().isEmpty()) {
			Card externalCard = null;
			for(Card card : user.getCards()) {
				if(card.getExternal()) {
					externalCard = card;
					break;
				}
			}
			if(externalCard != null) {
				if(dummyUser.getExternalCard().getCsn() == null || dummyUser.getExternalCard().getCsn().isEmpty()) {
					cardEtatService.setCardEtat(dummyUser.getExternalCard(), Etat.CADUC, null, null, false, true);
				} else {
					if(!dummyUser.getExternalCard().getCsn().equals(dummyUser.getExternalCard().getCsn())) {
						accessControlMustUpdate = true;
					}
					externalCard.setCsn(dummyUser.getExternalCard().getCsn());
					externalCard.setDesfireIds(dummyUser.getExternalCard().getDesfireIds());
					PhotoFile photo = dummyUser.getExternalCard().getPhotoFile();
					externalCard.getPhotoFile().getBigFile().setBinaryFile(photo.getBigFile().getBinaryFile());
					externalCard.getPhotoFile().setFileSize(photo.getFileSize());
					externalCard.getPhotoFile().setContentType(photo.getContentType());
					userInfoService.setPrintedInfo(externalCard, null);
				}
			}
		}
		
		for(Card card : user.getCards()) {
			// resync of cards : dueDate of user = max due_date
			if(card.getDueDate()==null || card.getDueDate().after(user.getDueDate()) || card.isEnabled() && !user.getDueDate().equals(card.getDueDate())) {
				card.setDueDate(user.getDueDate());
				card.merge();
				accessControlMustUpdate = true;
			}
			// if card was out_of_date we check due_date
			if(Etat.CADUC.equals(card.getEtat()) && card.getDueDate().before(user.getDueDate())) {
				card.setDueDate(user.getDueDate());
				card.merge();
			}
			// if card must be reenabled we enable it
			if(Etat.CADUC.equals(card.getEtat()) && card.getDueDateIncluded().after(new Date())) {
				cardEtatService.setCardEtat(card, Etat.DISABLED, null, null, false, true);
			}
			// if card must be out_of_date we invalidate it
			if((Etat.ENABLED.equals(card.getEtat()) || Etat.DISABLED.equals(card.getEtat()) || Etat.ENCODED.equals(card.getEtat())) && user.getDueDateIncluded().before(new Date())) {
				cardEtatService.setCardEtat(card, Etat.CADUC, null, null, false, true);
			}
			
		}
		if(accessControlMustUpdate) {
			accessControlService.sync(eppn);
		}
		return updated;
	}

}
