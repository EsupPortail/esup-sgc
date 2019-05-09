package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.esupportail.sgc.services.sync.ResynchronisationUserService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardEtatService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final static List<Etat> etatsEncoded = Arrays.asList(new Etat[] {Etat.ENCODED, Etat.ENABLED, Etat.DISABLED, Etat.CADUC});
	
	public final static List<Etat> etatsPrinted = Arrays.asList(new Etat[] {Etat.PRINTED, Etat.ENCODED, Etat.ENABLED, Etat.DISABLED, Etat.CADUC});
	
	public final static List<Etat> etatsEnableable = Arrays.asList(new Etat[] {Etat.ENABLED, Etat.DISABLED, Etat.CADUC}); 
	
	public final static List<Etat> etatsRequest = Arrays.asList(new Etat[] {Etat.NEW, Etat.RENEWED, Etat.REQUEST_CHECKED, Etat.REJECTED, Etat.IN_PRINT, Etat.PRINTED, Etat.IN_ENCODE, Etat.ENCODED});
	
	private final static List<Etat> etatsPhotoEditable = Arrays.asList(new Etat[] {Etat.NEW, Etat.RENEWED, Etat.REQUEST_CHECKED, Etat.IN_PRINT}); 
	
	private final static Map<Etat, List<Etat>> workflow = new HashMap<Etat, List<Etat>>();
	static {
		workflow.put(Etat.NEW, Arrays.asList(new Etat[]{Etat.REJECTED, Etat.REQUEST_CHECKED})); // NEW -> CANCELED is not an action made via the gui
		workflow.put(Etat.RENEWED, Arrays.asList(new Etat[]{Etat.REJECTED, Etat.REQUEST_CHECKED}));
		workflow.put(Etat.REQUEST_CHECKED, Arrays.asList(new Etat[]{Etat.IN_PRINT}));
		workflow.put(Etat.IN_PRINT, Arrays.asList(new Etat[]{Etat.REQUEST_CHECKED, Etat.IN_PRINT, Etat.PRINTED}));
		// workflow.put(Etat.PRINTED, Arrays.asList(new Etat[]{Etat.IN_ENCODE}));
		workflow.put(Etat.PRINTED, Arrays.asList(new Etat[]{}));
		workflow.put(Etat.IN_ENCODE, Arrays.asList(new Etat[]{Etat.PRINTED})); // IN_ENCODE -> ENCODED is not an action made via the gui
		workflow.put(Etat.ENCODED, Arrays.asList(new Etat[]{Etat.ENABLED}));
		workflow.put(Etat.ENABLED, Arrays.asList(new Etat[]{Etat.DISABLED, Etat.RENEWED}));
		workflow.put(Etat.DISABLED, Arrays.asList(new Etat[]{Etat.ENABLED, Etat.DESTROYED}));
		workflow.put(Etat.CADUC, Arrays.asList(new Etat[]{Etat.DESTROYED})); // CADUC -> DISABLED is not an action made via the gui
		workflow.put(Etat.REJECTED, Arrays.asList(new Etat[]{})); 
		workflow.put(Etat.DESTROYED, Arrays.asList(new Etat[]{}));
		workflow.put(Etat.CANCELED, Arrays.asList(new Etat[]{}));
	}

	@Resource
	LogService logService;
	
	@Autowired
	List<ValidateService> validateServices;
	

	@Resource
	EmailService emailService;
	
	@Resource
	CardService cardService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	ResynchronisationUserService resynchronisationUserService;
	
	@Resource
	UserInfoService userInfoService;
	
	@Autowired(required = false)
	CardIdsService cardIdsService;
	
	@Transactional
	public void disableCardWithMotif(Card card, MotifDisable motifDisable, boolean actionFromAnAdmin) {
		card.setMotifDisable(motifDisable);
		setCardEtat(card, Etat.DISABLED, "Carte désactivée à la demande de l'utilisateur.", null, actionFromAnAdmin, false);
	}
	
	@Async
	@Transactional
	public void setCardEtatAsync(Long cardId, Etat etat, String comment, String mailMessage, boolean actionFromAnAdmin, boolean force) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.warn("Error during sleep ...", e);
		}	
		this.setCardEtat(Card.findCard(cardId), etat, comment, mailMessage, actionFromAnAdmin, force);
	}
	
	@Transactional
	public boolean setCardEtat(Card card, Etat etat, String comment, String mailMessage, boolean actionFromAnAdmin, boolean force) {
		
		Etat etatInitial = card.getEtat();
		
		String eppn = card.getEppn();
		if(actionFromAnAdmin) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if(auth != null) {
				eppn = auth.getName();
			}
		}
		
		updateEtatsAvailable4Card(card);
		if(!card.getEtatsAvailable().contains(etat) && !force && !Etat.NEW.equals(etat) && !Etat.RENEWED.equals(etat)) {
			return false;
		}

		// only one card can be enabled at any time for a user
		if(Etat.ENABLED.equals(etat)) {
			User user = card.getUser();
			for(Card otherCard : user.getCards()) {
				if(Etat.ENABLED.equals(otherCard.getEtat()) && !card.getId().equals(otherCard.getId())) {
					this.setCardEtat(otherCard, Etat.DISABLED, "Activation d'une nouvelle carte - désactivation des anciennes.", null, false, false);
				}
			}
		}
		
		if(Etat.IN_PRINT.equals(etat) && cardIdsService!=null) {
			// be sure that card have qrcode : 
			if(card.getQrcode() == null || card.getQrcode().isEmpty()) {
				cardIdsService.generateQrcode4Card(card);
			}
		}
		
		if(Etat.IN_PRINT.equals(card.getEtat()) && (Etat.PRINTED.equals(etat) || Etat.ENCODED.equals(etat))) {
			userInfoService.setPrintedInfo(card);
		}		
			
		logService.log(card.getId(), ACTION.ETAT, RETCODE.SUCCESS, card.getEtat() + " -> " + etat, card.getEppn(), null);
		
		card.setEtat(etat);
		card.setEtatEppn(eppn);
		card.setDateEtat(new Date());
		card.setCommentaire(comment);
		

		if(Etat.ENCODED.equals(etat)) {
			Date encodedDate = new Date();
			card.setEncodedDate(encodedDate);
			card.setLastEncodedDate(encodedDate);
		}
		
		if(Etat.ENABLED.equals(etat)) {
			card.setEnnabledDate(new Date());
			card.setMotifDisable(null);
			card.setDueDate(card.getUser().getDueDate());
			for(ValidateService validateService : validateServices) {
				if(!card.getExternal() || validateService.getUse4ExternalCard()) {
					validateService.validate(card);
				}
			}	
		}
		
		if(Etat.DISABLED.equals(etat) || Etat.CADUC.equals(etat)) {
			for(ValidateService validateService : validateServices) {
				if(!card.getExternal() || validateService.getUse4ExternalCard()) {
					validateService.invalidate(card);
				}
			}
		}
		
		if(Etat.REJECTED.equals(etat)){
			this.updateNbRejets(card);
		}
		
		if(Etat.CANCELED.equals(etat)){
			card.setPayCmdNum(null);
		}
		
		card.getUser().setHasCardRequestPending(etatsRequest.contains(card.getEtat()) || this.hasRequestCard(eppn));
		
		this.sendMailInfo(etatInitial, card.getEtat(), card.getUser(), mailMessage);
		
		return true;
	}
	
	public void updateEtatsAvailable4Card(Card card) {
		String eppn = "system";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null) {
			eppn = auth.getName();
		}
		card.setEtatsAvailable(workflow.get(card.getEtat()));
		if(Etat.IN_PRINT.equals(card.getEtat()) || Etat.IN_ENCODE.equals(card.getEtat())) {
			if(!eppn.equals(card.getEtatEppn())) {
				card.setEtatsAvailable(new ArrayList<Etat>());
			}
		}
		if((Etat.NEW.equals(card.getEtat()) || Etat.RENEWED.equals(card.getEtat())) && card.getUser()!=null && !card.getUser().isEditable()) {
			List<Etat> etatsAvailable = new ArrayList<Etat>(card.getEtatsAvailable());
			etatsAvailable.remove(Etat.REQUEST_CHECKED);
			card.setEtatsAvailable(etatsAvailable);
		}
		if(Etat.ENABLED.equals(card.getEtat()) && (card.getExternal() || !card.getUser().isEditable()) || hasRequestCard(card.getEppn())){
			List<Etat> etatsAvailable = new ArrayList<Etat>(card.getEtatsAvailable());
			etatsAvailable.remove(Etat.RENEWED);
			card.setEtatsAvailable(etatsAvailable);
		}
		
	}
	
	public List<Etat> getEtatsAvailable4Cards(List<Long> cardIds) {
		List<Card> cards = Card.findAllCards(cardIds);
		updateEtatsAvailable4Card(cards.get(0));
		Set<Etat> etatsAvailable = new HashSet<Etat>(cards.get(0).getEtatsAvailable());
		for(Card card : cards) {
			updateEtatsAvailable4Card(card);
			etatsAvailable.retainAll(new HashSet<Etat>(card.getEtatsAvailable()));
			if(etatsAvailable.isEmpty()) {
				continue;
			}
		}
		return new ArrayList<Etat>(etatsAvailable);
	}

	public boolean hasRequestCard(String eppn) {
		return Card.countfindCardsByEppnEqualsAndEtatIn(eppn, etatsRequest)>0;
	}
	

	public boolean hasRejectedCard(String eppn) {
		return Card.countfindCardsByEppnEqualsAndEtatIn(eppn, Arrays.asList(new Etat[] {Etat.REJECTED}))>0;
	}
	
	public boolean hasNewCard(String eppn){
		
		return Card.countfindCardsByEppnEqualsAndEtatIn(eppn, Arrays.asList(new Etat[] {Etat.NEW}))>0;
	}
	

	public List<Card> getAllEncodedCards() {
		return Card.findCardsByEtatIn(etatsEncoded).getResultList();
	}

	public List<Card> getAllEncodedCards(List<String> eppns) {
		return Card.findCardsByEppnInAndEtatIn(eppns, etatsEncoded, null, null).getResultList();
	}

	public List<Card> getAllEnableableCardsWithEppnDistinct() {
		List<Card> cards = Card.findCardsByEtatIn(etatsEnableable).getResultList();
		return groupByEppn(cards);
	}

	public List<Card> getAllEnableableCardsWithEppnDistinct(List<String> eppns) {
		List<Card> cards =  Card.findCardsByEppnInAndEtatIn(eppns, etatsEnableable).getResultList();
		return groupByEppn(cards);
	}

	private List<Card>  groupByEppn(List<Card> cards) {
		Map<String,Card> cards4eppn = new HashMap<String,Card>();
		for(Card card : cards) {
			if(!cards4eppn.containsKey(card.getEppn())) {
				cards4eppn.put(card.getEppn(), card);
			}
		}
		return new ArrayList<Card>(cards4eppn.values());
	}
	
	public boolean isPhotoEditable(Card card) {
		boolean photoEditable = false;
		
		if(etatsPhotoEditable.contains(card.getEtat())){
			photoEditable = true;
		}
		
		return photoEditable;
	}
	
	public void sendMailInfo(Etat etatInitial, Etat etatFinal, User user, String mailMessage){
		if(user.getEmail() != null && !user.getEmail().isEmpty()) {
			if(mailMessage == null || mailMessage.isEmpty()) {
				List<CardActionMessage> messages = CardActionMessage.findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(true, etatInitial, etatFinal, user.getUserType(), true);
				if(messages.size()>0) {
					if(messages.size()>1) {
						log.warn(String.format("Multiples messages found for CardActionMessage with auto=true,  etatInitial=%s, etatFinal=%s and user.getUserType()=%s", etatInitial, etatFinal, user.getUserType()));
					}
					mailMessage = messages.get(0).getMessage();
				}
			}
			if(mailMessage != null && !mailMessage.trim().isEmpty()) {
				try {
					cardService.sendMailCard(appliConfigService.getNoReplyMsg(), user.getEmail(), appliConfigService.getListePpale(), 
					appliConfigService.getSubjectAutoCard().concat(" -- ".concat(user.getEppn())), mailMessage);
				} catch (Exception e) {
					log.error("Erreur lors de l'envoi du mail pour la carte de :" + user.getEppn(), e);
				}
			}
		}
		
		for(CardActionMessage mailToCardActionMessage : CardActionMessage.findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(true, etatInitial, etatFinal, user.getUserType(), false)) {
			try {
				cardService.sendMailCard(appliConfigService.getNoReplyMsg(), mailToCardActionMessage.getMailTo(), appliConfigService.getListePpale(), 
				appliConfigService.getSubjectAutoCard().concat(" -- ".concat(user.getEppn())), mailToCardActionMessage.getMessage());
			} catch (Exception e) {
				log.error(String.format("Erreur lors de l'envoi du mail à pour la carte de %s ", mailToCardActionMessage.getMailTo(), user.getEppn()), e);
			}
		}
	}

	@Transactional
	@Async("synchroExecutor")
	public void replayValidationOrInvalidation(Long cardId, List<String> validateServicesNames, Boolean resynchro) {
		Card card = Card.findCard(cardId);
		// synchronized on eppn to avoid parallel modifications on ldap (and avoid to add /remove ldap_value %secondary_id%)
		String lockKey = "CardEtatService.replayValidationOrInvalidation-" + card.getEppn();
		synchronized (lockKey.intern()) {
			if(Etat.ENABLED.equals(card.getEtat())) {
				if(resynchro) {
					resynchronisationUserService.synchronizeUserInfo(card.getEppn());
				}
				for(ValidateService validateService : validateServices) {
					if(validateServicesNames.contains(validateService.getBeanName())) {
						validateService.validate(card);
					}
				}	
			}		
			if(Etat.DISABLED.equals(card.getEtat()) || Etat.CADUC.equals(card.getEtat())) {
				if(resynchro) {
					resynchronisationUserService.synchronizeUserInfo(card.getEppn());
				}
				for(ValidateService validateService : validateServices) {
					if(validateServicesNames.contains(validateService.getBeanName())) {
						validateService.invalidate(card);
					}
				}
			}
		}
	}
	
	public ArrayList<String> getDistinctEtats(){
		
		ArrayList<String> etats = new ArrayList<String>();
		for(Etat etat:  Arrays.asList(Etat.values())){
			etats.add(etat.name());
		}
		List <String>  distinctEtats = Card.findDistinctEtats();
		etats.retainAll(distinctEtats);
		
		return etats;
	}
	
	/**
	 * Used for the tracking steps infos
	 */
	public List<String> getTrackingSteps(){
		List<String> steps = new ArrayList<String>();
		steps.add(Etat.NEW.name());
		steps.add(Etat.REQUEST_CHECKED.name());
		steps.add(Etat.IN_PRINT.name());
		steps.add(Etat.PRINTED.name());
		steps.add(Etat.IN_ENCODE.name());
		steps.add(Etat.ENCODED.name());
		steps.add(Etat.ENABLED.name());
		return steps;
	}

	protected void updateNbRejets(Card card) {
		if( card.getNbRejets()==null){
			card.setNbRejets(Long.valueOf(0));
		}
		Long nbRejets = 1 + card.getNbRejets();
		card.setNbRejets(nbRejets);
		card.merge();
 	}
	
	public List<String> getValidateServicesNames() {
		List<String> validateServicesNames = new ArrayList<String>();
		for(ValidateService validateService : validateServices) {
			validateServicesNames.add(validateService.getBeanName());
		}
		return validateServicesNames;
	}
	
	public Boolean areCardsReadyToBeDelivered(List<Long> cardIds){
		return Card.areCardsReadyToBeDelivered(cardIds);
	}
	
	public Boolean areCardsReadyToBeValidated(List<Long> cardIds){
		return Card.areCardsReadyToBeValidated(cardIds);
	}
}

