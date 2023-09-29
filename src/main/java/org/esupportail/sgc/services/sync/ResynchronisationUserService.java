package org.esupportail.sgc.services.sync;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.services.ac.AccessControlService;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class ResynchronisationUserService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	private UserInfoService userInfoService;
	
	@Resource
	CrousService crousService;
	
	@Resource
	ApiEscrService apiEscrService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Autowired(required = false)
	AccessControlService accessControlService;
	
	@Resource
	ExternalCardService externalCardService;
	
	@Resource
	ImportExportCardService importExportCardService;
	
	@Async("synchroExecutor")
	public void synchronizeUserInfoAsync(String eppn) {
		synchronizeUserInfo(eppn);
	}

	// Propagation.REQUIRES_NEW pour que DatabaseUserDetailsService.loadUserByUser puisse catcher une éventuelle exception
	// et continuer la procédure d'authentification malgré cette  exception (erreur crous ou autre)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean synchronizeUserInfo(String eppn) {
		return synchronizeUserInfoNoTx(eppn);
	}

	public boolean synchronizeUserInfoNoTx(String eppn) {
		boolean updated = false;
		log.trace("Synchronize of user " + eppn + " called.");
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

		UserInfoService.SynchroCmd syncUserInfoServiceFlag = userInfoService.setAdditionalsInfo(dummyUser, null);
		boolean accessControlMustUpdate = false;
		
		if(UserInfoService.SynchroCmd.NONE.equals(syncUserInfoServiceFlag)) {
			log.debug("Flag synchronize false for " + eppn + " -> no synchronize");
		} else if(dummyUser.getDueDate()!=null && user.getDueDate()!=null && dummyUser.getDueDate().before(new Date()) && user.getDueDate().before(new Date())) {
			log.debug("duedates are passed for " + eppn + " -> no synchronize");
		} else if(UserInfoService.SynchroCmd.JUST_FORCE_CADUC.equals(syncUserInfoServiceFlag)) {
			log.debug("caducIfEmpty for " + eppn + " -> force dueDate");
			userInfoService.forceDueDateCaduc(user);
		} else {
			// UserInfoService.SynchroCmd.SYNCHRONIZE.equals(syncUserInfoServiceFlag)
			if(log.isTraceEnabled()) {		
				log.trace("Flag synchronize true for " + eppn);
				log.trace("'user' computed from userInfoServices : " + dummyUser);
			}
	
			// si plus de csn sur une carte externe -> carte à invalider - sauf si elle va être caduque ...
			if (dummyUser.getExternalCard().getCsn() == null || dummyUser.getExternalCard().getCsn().isEmpty()) {
				for (Card card : user.getCards()) {
					if (card.getExternal() && card.isEnabled() && dummyUser.getDueDate().after(new Date())) {
						userInfoService.setAdditionalsInfo(user, null);
						cardEtatService.setCardEtat(card, Etat.DISABLED, null, null, false, true);
						accessControlMustUpdate = true;
					}
				}
			} else {
				Card externalCard = null;
				Boolean haveExternalCard = false;
				for (Card card : user.getCards()) {
					if (card.getExternal()) {
						haveExternalCard = true;
						if (card.getCsn() == null) {
							card.setCsn(dummyUser.getExternalCard().getCsn());
							externalCard = card;
						} else if (card.getCsn().equals(dummyUser.getExternalCard().getCsn())) {
							externalCard = card;
						}
					}
				}
				// externalCard has changed CSN !
				if (haveExternalCard && externalCard == null) {
					externalCard = externalCardService.initExternalCard(user);
					userInfoService.setAdditionalsInfo(user, null);
					cardEtatService.setCardEtat(externalCard, Etat.DISABLED, null, null, false, true);
					accessControlMustUpdate = true;
				}
				if (externalCard != null) {
					if (dummyUser.getExternalCard().getCsn() == null || dummyUser.getExternalCard().getCsn().isEmpty()) {
						userInfoService.setAdditionalsInfo(user, null);
						cardEtatService.setCardEtat(dummyUser.getExternalCard(), Etat.CADUC, null, null, false, true);
					} else {
						externalCard.setCsn(dummyUser.getExternalCard().getCsn());
						externalCard.setDesfireIds(dummyUser.getExternalCard().getDesfireIds());
						PhotoFile photo = dummyUser.getExternalCard().getPhotoFile();
						if(photo.getBigFile().getBinaryFile() != null) {
							externalCard.getPhotoFile().getBigFile().setBinaryFile(photo.getBigFile().getBinaryFile());
							externalCard.getPhotoFile().setFileSize(photo.getFileSize());
							externalCard.getPhotoFile().setContentType(photo.getContentType());
						} else {
							externalCard.getPhotoFile().getBigFile().setBinaryFile(importExportCardService.loadNoImgPhoto());
							externalCard.getPhotoFile().setFileSize(Long.valueOf(Integer.valueOf(importExportCardService.loadNoImgPhoto().length)));
							externalCard.getPhotoFile().setContentType(ImportExportCardService.DEFAULT_PHOTO_MIME_TYPE);
						}
						userInfoService.setPrintedInfo(externalCard);
						if (Etat.DISABLED.equals(externalCard.getEtat()) || Etat.CADUC.equals(externalCard.getEtat()) && (dummyUser.getDueDate() == null || dummyUser.getDueDate().after(new Date()))) {
							userInfoService.setAdditionalsInfo(user, null);
							cardEtatService.setCardEtat(externalCard, Etat.ENABLED, null, null, false, true);
						}
					}
				}
			}
			
			List<String> fieldNotEquals = dummyUser.getFieldNotEquals(user);
			if(!fieldNotEquals.isEmpty() && (user.getDueDate() != null || dummyUser.getDueDate() != null)) {
				if(dummyUser.getDueDate() == null && user.getDueDate().after(new Date())) {
					// TODO : avec shib, quelle dueDate ??
					log.warn(user.getEppn() + " has no more dueDate from sql/shib/ldap userInfoService (no more entry) "
							+ "and its recording duedate is at the moment after today -> we should force the dueDate to be today ... ?!");
					//user.setDueDate(new Date());
				}
				
				// if user is caduc and have only cards caduc, canceled or destroyed we don't synchronize
				if(dummyUser.getDueDateIncluded() != null && dummyUser.getDueDateIncluded().before(new Date()) || dummyUser.getDueDateIncluded() == null && user.getDueDateIncluded() != null && user.getDueDateIncluded().before(new Date())) {
					boolean haveOnlyCaducOrCanceledOrDestroyedCards = true; 
					for(Card card : user.getCards()) {
						if(!Etat.CADUC.equals(card.getEtat()) && !Etat.CANCELED.equals(card.getEtat()) && !Etat.DESTROYED.equals(card.getEtat())) {
							haveOnlyCaducOrCanceledOrDestroyedCards = false;
						}
					}
					if(haveOnlyCaducOrCanceledOrDestroyedCards) {
						log.trace(eppn + " is already caduc - no need to synchronize");
						return false;
					} else {
						log.debug(eppn + " is caduc - but he has cards that are not caduc/canceled/destroyed - need to synchronize");
					}
				}
				
				log.info(String.format("Synchronize of user %s is needed : %s is/are not synchronized.", eppn, fieldNotEquals));
				userInfoService.setAdditionalsInfo(user, null);
				user.merge();
				// we synchronize users with crous only if user had enable crous and had an email and had cards - same for europeanStudentCard
				if(user.getEmail() != null && !user.getEmail().isEmpty() && Card.countfindCardsByEppnEqualsAndEtatIn(eppn, Arrays.asList(new Etat[] {Etat.ENABLED, Etat.DISABLED, Etat.CADUC}))>0) {
					if(user.getCrous()) {
						crousService.postOrUpdateRightHolder(user.getEppn(), EsupSgcOperation.SYNC);
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
				user.setUpdateDate(new Date());
			} else {
				log.trace("Synchronize of user " + eppn + " was not needed.");
			}
		}
		
		for(Card card : user.getCards()) {
			// resync of cards : dueDate of user = max due_date
			if(card.getDueDate()==null || card.getDueDate().after(user.getDueDate()) || (card.isEnabled() || CardEtatService.etatsRequest.contains(card.getEtat())) && !user.getDueDate().equals(card.getDueDate())) {
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
			// if card is a new (or rejected) request and is already out_of_date we canceled the request
			if((Etat.NEW.equals(card.getEtat()) || Etat.REJECTED.equals(card.getEtat())) && user.getDueDateIncluded().before(new Date())) {
				SimpleDateFormat dateFormatterFr = new SimpleDateFormat("dd/MM/yyyy");
				cardEtatService.setCardEtat(card, Etat.CANCELED, "La date limite / de fin (" + dateFormatterFr.format(user.getDueDateIncluded()) + ") est dépassée, la demande de carte est annulée.", null, false, true);
			}
		}
		if(accessControlMustUpdate && accessControlService!=null) {
			accessControlService.sync(eppn);
		}
		
		return updated;
		
	}

}
