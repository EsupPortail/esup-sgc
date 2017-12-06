package org.esupportail.sgc.web.wsrest;
import javax.annotation.Resource;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.web.manager.ManagerCardController;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Transactional
@RequestMapping("/wsrest/view")
@Controller
public class WsRestHtmlController {
	
	@Resource
	ManagerCardController managerCardController;
	
	@Resource
	CardEtatService cardEtatService;
	
	@RequestMapping(value="/{eppn}/cardInfo", method = RequestMethod.GET)
	public String viewCardInfo(@PathVariable String eppn, Model uiModel) {
		
		User user = User.findUser(eppn);
		if(user != null){
			Hibernate.initialize(user.getCards());
			uiModel.addAttribute("steps", cardEtatService.getTrackingSteps());
			uiModel.addAttribute("user", user);
			uiModel.addAttribute("payboxList", PayboxTransactionLog.findPayboxTransactionLogsByEppnEquals(eppn).getResultList());
		}
		uiModel.addAttribute("user", user);
		return "user/card-info";
	}

	@RequestMapping(value="/{eppn}/userInfo", method = RequestMethod.GET)
	public String viewUserInfo(@PathVariable String eppn, Model uiModel) {
		
        User user = User.findUser(eppn);
        if(user != null){
	        if(!user.getCards().isEmpty()){
	        	for(Card cardItem : user.getCards()){
	        		cardEtatService.updateEtatsAvailable4Card(cardItem);
	        		cardItem.setIsPhotoEditable(cardEtatService.isPhotoEditable(cardItem));
	        	}
	        	uiModel.addAttribute("currentCard", user.getCards().get(0));
	        	
	        }
	       
        }
        uiModel.addAttribute("user", user);
		return "manager/show";
	}

}


