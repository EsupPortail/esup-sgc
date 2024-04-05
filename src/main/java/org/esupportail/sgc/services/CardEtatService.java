package org.esupportail.sgc.services;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		workflow.put(Etat.NEW, Arrays.asList(new Etat[]{Etat.REJECTED, Etat.REQUEST_CHECKED, Etat.CANCELED}));
		workflow.put(Etat.RENEWED, Arrays.asList(new Etat[]{Etat.REJECTED, Etat.REQUEST_CHECKED, Etat.CANCELED}));
		workflow.put(Etat.REQUEST_CHECKED, Arrays.asList(new Etat[]{Etat.IN_PRINT, Etat.CANCELED}));
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

	public static Map<Etat, List<Etat>> workflow4messages = new HashMap<Etat, List<Etat>>(workflow);
	static {
		workflow4messages.put(Etat.PRINTED, new ArrayList<>(workflow4messages.get(Etat.PRINTED)));
		workflow4messages.put(Etat.IN_ENCODE, new ArrayList<>(workflow4messages.get(Etat.IN_ENCODE)));
		workflow4messages.put(Etat.DISABLED, new ArrayList<>(workflow4messages.get(Etat.DISABLED)));
		workflow4messages.put(Etat.ENABLED, new ArrayList<>(workflow4messages.get(Etat.ENABLED)));
		workflow4messages.put(Etat.CADUC, new ArrayList<>(workflow4messages.get(Etat.CADUC)));
		workflow4messages.get(Etat.PRINTED).add(Etat.IN_ENCODE);
		workflow4messages.get(Etat.IN_ENCODE).add(Etat.ENCODED);
		workflow4messages.get(Etat.DISABLED).add(Etat.CADUC);
		workflow4messages.get(Etat.ENABLED).add(Etat.CADUC);
		workflow4messages.get(Etat.CADUC).add(Etat.DISABLED);
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

	@Resource
	PrinterService printerService;

	@Resource
	EncodeAndPringLongPollService encodeAndPringLongPollService;
	
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
		return setCardEtat(card, etat, comment, mailMessage, actionFromAnAdmin, force, null, null);
	}

	@Transactional
	public boolean setCardEtat(Card card, Etat etat, String comment, String mailMessage, boolean actionFromAnAdmin, boolean force, String printerEppn) {
		return setCardEtat(card, etat, comment, mailMessage, actionFromAnAdmin, force, printerEppn, null);
	}
	
	@Transactional
	public boolean setCardEtat(Card card, Etat etat, String comment, String mailMessage, boolean actionFromAnAdmin, boolean force, String printerEppn, String csn) {
		
		Etat etatInitial = card.getEtat();
		
		String eppn = card.getEppn();
		if(actionFromAnAdmin) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if(auth != null) {
				eppn = auth.getName();
			}
		}

		// API hack : if card is encoded via API, we can set the card to encoded only if csn is provided ord card.csn is not empty
		if(Etat.ENCODED.equals(etat) && (StringUtils.isNotEmpty(csn) || StringUtils.isNotEmpty(card.getCsn()))) {
			if(StringUtils.isNotEmpty(csn)) {
				log.info("Set card encoded via API : " + card.getId() + " with csn : " + csn);
				card.setCsn(csn);
			} else {
				log.info("Set card encoded via API : " + card.getId() + " with csn : " + card.getCsn());
			}
		} else {
			updateEtatsAvailable4Card(card, printerEppn);
			if (!card.getEtatsAvailable().contains(etat) && !force && !Etat.NEW.equals(etat) && !Etat.RENEWED.equals(etat)) {
				return false;
			}
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

		if(Etat.IN_PRINT.equals(etat)) {
			if(!StringUtils.isEmpty(printerEppn)) {
				encodeAndPringLongPollService.handleCard(printerEppn, card.getQrcode());
			}
			card.setPrinterEppn(printerEppn);
		}


		logService.log(card.getId(), ACTION.ETAT, RETCODE.SUCCESS, card.getEtat() + " -> " + etat, card.getEppn(), null, printerEppn);
		
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
				validateService.validate(card);
			}	
		}
		
		if((Etat.DISABLED.equals(etat) || Etat.CADUC.equals(etat)) && !(Etat.DISABLED.equals(etatInitial) || Etat.CADUC.equals(etatInitial))) {
			for(ValidateService validateService : validateServices) {
				validateService.invalidate(card);
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
		updateEtatsAvailable4Card(card, null);
	}
	public void updateEtatsAvailable4Card(Card card, String printerEppn) {
		String eppn = "system";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null) {
			eppn = auth.getName();
		}
		card.setEtatsAvailable(workflow.get(card.getEtat()));
		if(Etat.IN_PRINT.equals(card.getEtat()) || Etat.IN_ENCODE.equals(card.getEtat())) {
			if(!(eppn.equals(card.getEtatEppn()) || printerEppn!=null && card.getPrinterEppn()!=null && card.getPrinterEppn().equals(printerEppn))) {
				card.setEtatsAvailable(new ArrayList<Etat>());
			}
		}
		if(Etat.REQUEST_CHECKED.equals(card.getEtat()) || Etat.IN_PRINT.equals(card.getEtat())) {
			Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
			if(!StringUtils.isEmpty(printerEppn) && !printerService.isPrinterConnected(printerEppn) || StringUtils.isEmpty(printerEppn) && appliConfigService.getPrinterRoleConfig() && !roles.contains("ROLE_PRINTER")) {
				card.removeEtatAvailable(Etat.IN_PRINT);
			}
		}

		if((Etat.NEW.equals(card.getEtat()) || Etat.RENEWED.equals(card.getEtat())) && card.getUser()!=null && !card.getUser().isEditable()) {
			card.removeEtatAvailable(Etat.REQUEST_CHECKED);
		}
		if(Etat.ENABLED.equals(card.getEtat()) && (card.getExternal() || !card.getUser().isEditable() || hasRequestCard(card.getEppn()))){
			card.removeEtatAvailable(Etat.RENEWED);
		}
		
	}
	
	public List<Etat> getEtatsAvailable4Cards(List<Long> cardIds, String printerEppn) {
		List<Card> cards = Card.findAllCards(cardIds);
		updateEtatsAvailable4Card(cards.get(0), printerEppn);
		Set<Etat> etatsAvailable = new HashSet<Etat>(cards.get(0).getEtatsAvailable());
		for(Card card : cards) {
			updateEtatsAvailable4Card(card, printerEppn);
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
		Collections.sort(cards, Comparator.comparing(Card::getEtat)
	            .thenComparing(Collections.reverseOrder(Comparator.comparing(Card::getDateEtat))));
		cards.sort((c1, c2) -> c1.getEtat().compareTo(c2.getEtat()));
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
			CardActionMessage cardActionMessage = null;
			if(mailMessage == null || mailMessage.isEmpty()) {
				List<CardActionMessage> messages = CardActionMessage.findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(true, etatInitial, etatFinal, user.getUserType(), true);
				if(messages.size()>0) {
					if(messages.size()>1) {
						log.warn(String.format("Multiples messages found for CardActionMessage with auto=true,  etatInitial=%s, etatFinal=%s and user.getUserType()=%s", etatInitial, etatFinal, user.getUserType()));
					}
					cardActionMessage = messages.get(0);
					mailMessage = cardActionMessage.getMessage();
				}
			}
			if(mailMessage != null && !mailMessage.trim().isEmpty()) {
				try {
					cardService.sendMailCard(user, cardActionMessage, appliConfigService.getNoReplyMsg(), user.getEmail(), appliConfigService.getListePpale(),
					appliConfigService.getSubjectAutoCard().concat(" -- ".concat(user.getEppn())), mailMessage);
				} catch (Exception e) {
					log.error("Erreur lors de l'envoi du mail pour la carte de :" + user.getEppn(), e);
				}
			}
		}
		
		for(CardActionMessage mailToCardActionMessage : CardActionMessage.findCardActionMessagesByAutoByEtatInitialAndEtatFinalAndUserTypeWithMailToEmptyOrNull(true, etatInitial, etatFinal, user.getUserType(), false)) {
			try {
				cardService.sendMailCard(user, mailToCardActionMessage, appliConfigService.getNoReplyMsg(), mailToCardActionMessage.getMailTo(), appliConfigService.getListePpale(),
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
			log.debug("replayValidationOrInvalidation for card " + cardId + " on " + validateServicesNames);
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

