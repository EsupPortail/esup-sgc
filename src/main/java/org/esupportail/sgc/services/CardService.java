package org.esupportail.sgc.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class CardService {	
    
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource 
	private UserInfoService userInfoService;
	
	@Resource
	protected EmailService emailService;	
	
	@Resource
	protected CardEtatService cardEtatService;	
	
	@Resource
	protected AppliConfigService appliConfigService;	
	
	@Resource
	LogService logService;
	
	public Card findLastCardByEppnEquals(String eppn) {
		Card lastCard = null;
		List<Card> cards =  Card.findCardsByEppnEquals(eppn,"requestDate","DESC").getResultList();
		if(!cards.isEmpty()) {
			lastCard = cards.get(0);
		}
		return lastCard;
	}

	public Map<String,String> getPhotoParams(){
		
		return Params.getPhotoParams();
		
	}

	public Object findCard(Long id) {
		Card card = Card.findCard(id);
		cardEtatService.updateEtatsAvailable4Card(card);
		return card;
	}
	
	
	public boolean deleteMsg(String id){
		
		boolean success = true;
		
		if(AppliConfig.findAppliConfigsByKeyLike(id).getMaxResults()>0){
			// TODO - à virer ??
			AppliConfig.findAppliConfigsByKeyLike(id).getResultList().get(0).remove();
		}else{
			success = false;
		}
		
		return success;
	}
	
	public boolean displayFormCnil (String type){
		
		boolean displayCnil = false;
		
		if(appliConfigService.displayFormCnil().contains(type)){
			displayCnil = true;
		}
		
		return displayCnil;
	}
	
	public boolean displayFormCrous (String eppn, String type){
		
		boolean displayCrous = false;
		
		User user = User.findUser(eppn);
		
		// When crous is accepted ones, we can't unaccept it
		if((user==null || !user.getCrous()) && appliConfigService.displayFormCrous().contains(type)){
			displayCrous = true;
		}
		
		return displayCrous;
	}
	
	public boolean displayFormAdresse (String type){
		
		boolean displayAdresse = false;
		
		if(appliConfigService.displayFormAdresse().contains(type)){
			displayAdresse = true;
		}
		
		return displayAdresse;
	}
	
	public boolean displayFormRules(String type){
		
		boolean displayRules = false;
		
		if(appliConfigService.displayFormRules().contains(type)){
			displayRules = true;
		}
		
		return displayRules;
	}
	
	public boolean displayFormEuropeanCard (String type){
		
		boolean displayFormEuropeanCard = false;
		
		if(appliConfigService.displayFormEuropeanCard().contains(type)){
			displayFormEuropeanCard = true;
		}
		
		return displayFormEuropeanCard;
	}
	
	public String getPaymentWithoutCard(String eppn){
		String reference = "";
		User user = User.findUser(eppn);
		List <PayboxTransactionLog> payboxList =  PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList();

		if(!payboxList.isEmpty()){
			List<String> references = new ArrayList<String>();
			for(PayboxTransactionLog log : payboxList){
				if ("00000".equals(log.getErreur())) {
					references.add(log.getReference());
				}
			}			
			List <Card> cards = user.getCards();
			if(!cards.isEmpty()){
				for(Card card : cards){
					if(card.getPayCmdNum()!= null && !card.getPayCmdNum().isEmpty()){
						if(references.contains(card.getPayCmdNum())){
							references.remove(card.getPayCmdNum());
						}
					}
				}

			}
			if(!references.isEmpty()){
				reference = references.get(0);
			}
		}
		return reference;
		
	}

	public void sendMailCard(String mailFrom, String mailTo, String sendCC, String subject, String mailMessage){
		if(!sendCC.isEmpty()){
				emailService.sendMessageCc(mailFrom, mailTo, sendCC, subject, mailMessage);
		}else{
			emailService.sendMessage(mailFrom, mailTo, subject, mailMessage);
		}
	}
}
