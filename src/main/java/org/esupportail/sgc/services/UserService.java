package org.esupportail.sgc.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;

import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.domain.ldap.PersonLdap;
import org.esupportail.sgc.security.PermissionService;
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

	@Resource
	CardService cardService;
	
	@Resource
	CardEtatService cardEtatService;
	
	@Resource
	ExtUserRuleService extUserRuleService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	LdapPersonService ldapPersonService;
	
	@Resource
	DateUtils dateUtils;

    @Resource
    CardDaoService cardDaoService;

    @Resource
    UserDaoService userDaoService;

	public boolean isFirstRequest(User user){
		return cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(user.getEppn(), Arrays.asList(new Etat[] {Etat.CANCELED})) == 0;
	}

    public boolean isFreeRenewal(User user){
		return user.isRequestFree() && !isFirstRequest(user) && !isOutOfDueDate(user) && !hasRequestCard(user) && !user.getHasExternalCard();
    }

	public boolean isPaidRenewal(User user){
		boolean isPaidRenewal = false;
		String reference = cardService.getPaymentWithoutCard(user.getEppn());
		if(!reference.isEmpty()){
			isPaidRenewal = true;
		}
		return isPaidRenewal;
	}
	
	public boolean displayRenewalForm(User user, boolean isFreeRenewal, boolean isPaidRenewal) {

		boolean displayRenewalForm = isFreeRenewal || isPaidRenewal;
		
		if(user != null && user.getHasExternalCard()) {
			displayRenewalForm = false;
		}
		
		displayRenewalForm = displayRenewalForm && !hasRequestCard(user);
		return displayRenewalForm;
	}

	public boolean displayForm(User user, boolean requestUserIsManager, boolean displayRenewalForm, boolean isFirstRequest){
		boolean displayForm = displayRenewalForm;
		if(isFirstRequest){
			displayForm = ((isEsupSgcUser(user) && !isOutOfDueDate(user))) ;
		} 
		return displayForm || requestUserIsManager;
	}
	
	private boolean hasRequestCard(User user){
		return cardEtatService.hasRequestCard(user.getEppn());
	}
	
	
	public boolean isEsupSgcUser(User user) {
		String eppn = user.getEppn();
		return user.getReachableRoles().contains("ROLE_USER") || extUserRuleService.isExtEsupSgcUser(eppn);
	}
	
	public boolean isEsupManager(User user) {
		return user.getReachableRoles().contains("ROLE_MANAGER");
	}

	public Boolean canPaidRenewal(User user) {
		if(user != null && user.getHasExternalCard()) {
			return false;
		}
		return !isOutOfDueDate(user) && !user.isRequestFree() && !isPaidRenewal(user) && !hasRequestCard(user);
	}
	
	private boolean isOutOfDueDate(User user) {
		return user.getDueDate()==null || user.getDueDate().isBefore(LocalDateTime.now());
	}
	
	public boolean hasDeliveredCard(User user){
		
		// si mode livraison non activée, on considère la carte comme livrée
		if(!"TRUE".equalsIgnoreCase(appliConfigService.getModeLivraison())) {
			return true;
		}
		
		boolean hasDeliveredCard = true;
		if (!user.getCards().isEmpty()){
			for(Card card : user.getCards()){
				if(!Etat.CANCELED.equals(card.getEtat()) && card.getDeliveredDate() == null){
					hasDeliveredCard = false; 
					break;
				}
			}
		}
		return hasDeliveredCard;
	}
	
	public boolean isISmartphone(String userAgent){
		boolean isISmartphone = false;
		
		if(userAgent.contains("iPhone") || userAgent.contains("iPad")){
			isISmartphone = true;
		}
		
		return isISmartphone;
	}
	
	public HashMap<String,String> getConfigMsgsUser(){
		
		HashMap<String,String> getConfigMsgsUser = new HashMap<String, String>();
		
		getConfigMsgsUser.put("helpMsg", appliConfigService.getUserHelpMsg());
		getConfigMsgsUser.put("freeRenewalMsg", appliConfigService.getUseFreeRenewalMsg());
		getConfigMsgsUser.put("paidRenewalMsg", appliConfigService.getUserPaidRenewalMsg());
		getConfigMsgsUser.put("canPaidRenewalMsg", appliConfigService.getUserCanPaidRenewalMsg());
		getConfigMsgsUser.put("newCardMsg", appliConfigService.getNewCardlMsg());
		getConfigMsgsUser.put("checkedOrEncodedCardMsg", appliConfigService.getCheckedOrEncodedCardMsg());
		getConfigMsgsUser.put("rejectedCardMsg", appliConfigService.getRejectedCardMsg());
		getConfigMsgsUser.put("enabledCardMsg", appliConfigService.getEnabledCardMsg());
		getConfigMsgsUser.put("enabledCardPersMsg", appliConfigService.getEnabledCardPersMsg());
		getConfigMsgsUser.put("userFormRejectedMsg", appliConfigService.getUserFormRejectedMsg());
		getConfigMsgsUser.put("userFormRules", appliConfigService.getUserFormRules());
		getConfigMsgsUser.put("userFreeForcedRenewal", appliConfigService.getUserFreeForcedRenewal());
		getConfigMsgsUser.put("userTipMsg", appliConfigService.getUserTipMsg());
		
		return getConfigMsgsUser;
		
	}
	
	public Map<String,Boolean> displayFormParts(User user, boolean requestUserIsManager){
		Map<String,Boolean> displayFormParts = new HashMap<String, Boolean>();
		StopWatch stopWatch = new PrettyStopWatch();
		stopWatch.start("displayCnil");
		displayFormParts.put("displayCnil", cardService.displayFormCnil(user.getUserType()));
		stopWatch.start("displayCrous");
		displayFormParts.put("displayCrous", cardService.displayFormCrous(user));
		stopWatch.start("enableCrous");
		displayFormParts.put("enableCrous", cardService.isCrousEnabled(user));
		stopWatch.start("displayRules");
		displayFormParts.put("displayRules", cardService.displayFormRules(user.getUserType()));
		stopWatch.start("displayAdresse");
		displayFormParts.put("displayAdresse", cardService.displayFormAdresse(user.getUserType()));
		stopWatch.start("isPaidRenewal");
		boolean isPaidRenewal = this.isPaidRenewal(user);
		displayFormParts.put("isPaidRenewal",  isPaidRenewal);
		stopWatch.start("isFreeRenewal");
		boolean isFreeRenewal = this.isFreeRenewal(user);
		displayFormParts.put("isFreeRenewal",  isFreeRenewal);
		stopWatch.start("isFirstRequest");
		boolean isFirstRequest = this.isFirstRequest(user);
		displayFormParts.put("isFirstRequest", isFirstRequest);
		stopWatch.start("displayRenewalForm");
		boolean displayRenewalForm = this.displayRenewalForm(user, isFreeRenewal, isPaidRenewal);
		displayFormParts.put("displayRenewalForm",  displayRenewalForm);
		stopWatch.start("displayForm");
		displayFormParts.put("displayForm",  this.displayForm(user, requestUserIsManager, displayRenewalForm, isFirstRequest));
		stopWatch.start("canPaidRenewal");
		displayFormParts.put("canPaidRenewal",  this.canPaidRenewal(user));
		stopWatch.start("hasDeliveredCard");
		displayFormParts.put("hasDeliveredCard",  this.hasDeliveredCard(user));
		stopWatch.start("enableEuropeanCard");
		displayFormParts.put("enableEuropeanCard",  cardService.isEuropeanCardEnabled(user));
		stopWatch.start("displayEuropeanCard");
		displayFormParts.put("displayEuropeanCard",  cardService.displayFormEuropeanCardEnabled(user));
		stopWatch.stop();
		log.trace(stopWatch.prettyPrint());
		return displayFormParts;
		
	}
	
	public List<User> getSgcLdapMix(String cn, String ldapTemplateName){
		
		ArrayList<User> users = new ArrayList<User>();
		
		List<PersonLdap> ldapResults =   ldapPersonService.searchByCommonName(cn, ldapTemplateName);
		
		for(PersonLdap item: ldapResults){
			if(item.getEduPersonPrincipalName() !=null && !item.getEduPersonPrincipalName().isEmpty()) {
				User user = userDaoService.findUser(item.getEduPersonPrincipalName());
				if(user == null){
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
