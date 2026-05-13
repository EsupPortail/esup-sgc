package org.esupportail.sgc.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Resource;

import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.services.ldap.LdapPersonService;
import org.esupportail.sgc.tools.DateUtils;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;


@Service
public class UserService {

	public final Logger log = LoggerFactory.getLogger(getClass());

	@Resource CardService cardService;
	@Resource CardEtatService cardEtatService;
	@Resource ExtUserRuleService extUserRuleService;
	@Resource AppliConfigService appliConfigService;
	@Resource LdapPersonService ldapPersonService;
	@Resource DateUtils dateUtils;
	@Resource CardDaoService cardDaoService;
	@Resource UserDaoService userDaoService;

	// =========================================================================
	// Méthodes publiques — API observable (utilisée depuis les contrôleurs et
	// les tests unitaires). Chacune est autonome et fait ses propres appels.
	// =========================================================================

	public boolean isFirstRequest(User user) {
		return cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(
				user.getEppn(), Arrays.asList(Etat.CANCELED)) == 0;
	}

	public boolean isFreeRenewal(User user) {
		return user.isRequestFree()
				&& !isFirstRequest(user)
				&& !isOutOfDueDate(user)
				&& !hasRequestCard(user)
				&& !user.getHasExternalCard();
	}

	public boolean isFreeNew(User user) {
		return user.isFirstRequestFree()
				&& !isOutOfDueDate(user)
				&& !hasRequestCard(user)
				&& !user.getHasExternalCard();
	}

	public boolean isPaidRenewal(User user) {
		return !cardService.getPaymentWithoutCard(user.getEppn()).isEmpty();
	}

	public boolean displayRenewalForm(User user, boolean isFreeRenewal, boolean isPaidRenewal) {
		return (isFreeRenewal || isPaidRenewal)
				&& !user.getHasExternalCard()
				&& !hasRequestCard(user);
	}

	public boolean displayNewForm(User user, boolean isFreeNew, boolean isPaidNew) {
		return isEsupSgcUser(user)
				&& (isFreeNew || isPaidNew)
				&& !user.getHasExternalCard()
				&& !hasRequestCard(user);
	}

	public boolean displayForm(User user, boolean requestUserIsManager,
			boolean displayRenewalForm, boolean isFirstRequest, boolean displayNewForm) {
		boolean displayForm = displayRenewalForm || displayNewForm;
		if (isFirstRequest && !displayNewForm) {
			displayForm = isEsupSgcUser(user) && !isOutOfDueDate(user);
		}
		return displayForm || requestUserIsManager;
	}

	public boolean canPaidRenewal(User user) {
		return !user.getHasExternalCard()
				&& !isOutOfDueDate(user)
				&& !user.isRequestFree()
				&& !isPaidRenewal(user)
				&& !hasRequestCard(user)
				&& !isFirstRequest(user);
	}

	public boolean canPaidNew(User user) {
		return !user.getHasExternalCard()
				&& !isOutOfDueDate(user)
				&& !user.isFirstRequestFree()
				&& !isPaidRenewal(user)
				&& !hasRequestCard(user)
				&& isFirstRequest(user);
	}

	public boolean hasDeliveredCard(User user) {
		if (!"TRUE".equalsIgnoreCase(appliConfigService.getModeLivraison())) {
			return true; // mode livraison désactivé : on considère la carte comme livrée
		}
		return user.getCards().stream()
				.filter(card -> !Etat.CANCELED.equals(card.getEtat()))
				.allMatch(card -> card.getDeliveredDate() != null);
	}

	public boolean isEsupSgcUser(User user) {
		return user.getReachableRoles().contains("ROLE_USER")
				|| extUserRuleService.isExtEsupSgcUser(user.getEppn());
	}

	public boolean isEsupManager(User user) {
		return user.getReachableRoles().contains("ROLE_MANAGER");
	}

	public boolean isISmartphone(String userAgent) {
		return userAgent.contains("iPhone") || userAgent.contains("iPad");
	}

	// =========================================================================
	// Constructeur du contexte d'affichage — calcule chaque valeur une seule
	// fois pour éviter les appels redondants à la base de données / services.
	//
	// Les fondations (isFirstRequest, hasRequestCard, isPaidRenewal) déclenchent
	// des requêtes SQL ou des appels réseau : elles sont résolues en premier et
	// passées aux dérivés. La logique de chaque dérivé est identique à celle de
	// la méthode publique correspondante — toute divergence est détectée par les
	// tests unitaires de UserServiceDisplayFormTest.
	// =========================================================================

	public UserFormContext displayFormParts(User user, boolean requestUserIsManager) {
		StopWatch stopWatch = new PrettyStopWatch();

		// ── Fondations : appels potentiellement coûteux (DB / service externe) ──
		stopWatch.start("isFirstRequest");
		final boolean isFirstRequest   = isFirstRequest(user);
		stopWatch.start("hasRequestCard");
		final boolean hasRequestCard   = hasRequestCard(user);
		stopWatch.start("isPaidRenewal");
		final boolean isPaidRenewal    = isPaidRenewal(user);

		// ── Dérivés directs depuis l'objet User (lecture en mémoire) ──
		final boolean isOutOfDueDate  = isOutOfDueDate(user);
		final boolean hasExternalCard = user.getHasExternalCard();
		final boolean isEsupSgcUser   = isEsupSgcUser(user);

		// ── Flags de droits ──
		stopWatch.start("isFreeRenewal");
		final boolean isFreeRenewal = user.isRequestFree()
				&& !isFirstRequest && !isOutOfDueDate && !hasRequestCard && !hasExternalCard;

		stopWatch.start("isFreeNew");
		final boolean isFreeNew     = user.isFirstRequestFree()
				&& !isOutOfDueDate && !hasRequestCard && !hasExternalCard;

		// ── Flags d'affichage formulaire ──
		stopWatch.start("displayRenewalForm");
		final boolean displayRenewalForm = (isFreeRenewal || isPaidRenewal)
				&& !hasExternalCard && !hasRequestCard;

		stopWatch.start("displayNewForm");
		final boolean displayNewForm = isEsupSgcUser && (isFreeNew || isPaidRenewal)
				&& !hasExternalCard && !hasRequestCard;

		stopWatch.start("displayForm");
		boolean displayForm = displayRenewalForm || displayNewForm;
		if (isFirstRequest && !displayNewForm) {
			displayForm = isEsupSgcUser && !isOutOfDueDate;
		}
		displayForm = displayForm || requestUserIsManager;

		stopWatch.start("canPaidRenewal");
		final boolean canPaidRenewal = !hasExternalCard && !isOutOfDueDate
				&& !user.isRequestFree() && !isPaidRenewal && !hasRequestCard && !isFirstRequest;

		stopWatch.start("canPaidNew");
		final boolean canPaidNew     = !hasExternalCard && !isOutOfDueDate
				&& !user.isFirstRequestFree() && !isPaidRenewal && !hasRequestCard && isFirstRequest;

		stopWatch.start("hasDeliveredCard");
		final boolean hasDeliveredCard = hasDeliveredCard(user);

		// ── Appels CardService ──
		stopWatch.start("displayCnil");
		final boolean displayCnil         = cardService.displayFormCnil(user.getUserType());
		stopWatch.start("displayCrous");
		final boolean displayCrous        = cardService.displayFormCrous(user);
		stopWatch.start("enableCrous");
		final boolean enableCrous         = cardService.isCrousEnabled(user);
		stopWatch.start("displayRules");
		final boolean displayRules        = cardService.displayFormRules(user.getUserType());
		stopWatch.start("displayAdresse");
		final boolean displayAdresse      = cardService.displayFormAdresse(user.getUserType());
		stopWatch.start("enableEuropeanCard");
		final boolean enableEuropeanCard  = cardService.isEuropeanCardEnabled(user);
		stopWatch.start("displayEuropeanCard");
		final boolean displayEuropeanCard = cardService.displayFormEuropeanCardEnabled(user);
		stopWatch.stop();

		log.trace(stopWatch.prettyPrint());
		return new UserFormContext(
				displayCnil, displayCrous, enableCrous,
				displayRules, displayAdresse,
				isPaidRenewal, isFreeRenewal, isFreeNew,
				isFirstRequest, displayRenewalForm, displayNewForm,
				displayForm, canPaidRenewal, canPaidNew,
				hasDeliveredCard, enableEuropeanCard, displayEuropeanCard
		);
	}

	// =========================================================================
	// Helpers privés
	// =========================================================================

	private boolean hasRequestCard(User user) {
		return cardEtatService.hasRequestCard(user.getEppn());
	}

	private boolean isOutOfDueDate(User user) {
		return user.getDueDate() == null || user.getDueDate().isBefore(LocalDateTime.now());
	}

	// =========================================================================
	// Autres méthodes publiques
	// =========================================================================

	public HashMap<String, String> getConfigMsgsUser() {
		HashMap<String, String> msgs = new HashMap<>();
		msgs.put("helpMsg",               appliConfigService.getUserHelpMsg());
		msgs.put("freeRenewalMsg",        appliConfigService.getUseFreeRenewalMsg());
		msgs.put("paidRenewalMsg",        appliConfigService.getUserPaidRenewalMsg());
		msgs.put("canPaidRenewalMsg",     appliConfigService.getUserCanPaidRenewalMsg());
		msgs.put("canPaidNewMsg",         appliConfigService.getUserCanPaidNewMsg());
		msgs.put("newCardMsg",            appliConfigService.getNewCardlMsg());
		msgs.put("checkedOrEncodedCardMsg", appliConfigService.getCheckedOrEncodedCardMsg());
		msgs.put("rejectedCardMsg",       appliConfigService.getRejectedCardMsg());
		msgs.put("enabledCardMsg",        appliConfigService.getEnabledCardMsg());
		msgs.put("enabledCardPersMsg",    appliConfigService.getEnabledCardPersMsg());
		msgs.put("userFormRejectedMsg",   appliConfigService.getUserFormRejectedMsg());
		msgs.put("userFormRules",         appliConfigService.getUserFormRules());
		msgs.put("userFreeForcedRenewal", appliConfigService.getUserFreeForcedRenewal());
		msgs.put("userTipMsg",            appliConfigService.getUserTipMsg());
		return msgs;
	}

	public List<User> getSgcLdapMix(String cn, String ldapTemplateName) {
		ArrayList<User> users = new ArrayList<>();
		for (PersonLdap item : ldapPersonService.searchByCommonName(cn, ldapTemplateName)) {
			if (item.getEduPersonPrincipalName() != null && !item.getEduPersonPrincipalName().isEmpty()) {
				User user = userDaoService.findUser(item.getEduPersonPrincipalName());
				if (user == null) {
					user = new User();
					user.setEppn(item.getEduPersonPrincipalName());
					user.setFirstname(item.getGivenName());
					user.setName(item.getSn());
					user.setEmail(item.getMail());
					user.setSupannEntiteAffectationPrincipale(item.getSupannEntiteAffectationPrincipale());
					user.setUserType(item.getEduPersonPrimaryAffiliation());
					user.setBirthday(dateUtils.parseSchacDateOfBirth(item.getSchacDateOfBirth()));
				}
				users.add(user);
			}
		}
		return users;
	}

	public List<String> getLdapTemplatesNames() {
		return ldapPersonService.getLdapTemplatesNames();
	}
}
