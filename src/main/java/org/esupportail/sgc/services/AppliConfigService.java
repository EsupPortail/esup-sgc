package org.esupportail.sgc.services;

import java.util.Arrays;
import java.util.List;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliConfig.TypeConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
public class AppliConfigService {
	
	public List<String> getTypes() {
		return Arrays.asList(new String[] {TypeConfig.HTML.name(), TypeConfig.TEXT.name(), TypeConfig.BOOLEAN.name()});
	}  
	
	
	public void merge(AppliConfig appliConfig) {
		appliConfig.merge();
	}
	
	
	public void remove(AppliConfig appliConfig) {
		appliConfig.remove();
	}

	
	public void persist(AppliConfig appliConfig) {
		appliConfig.persist();
	}
	
	
	public String displayFormCnil() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("DISPLAY_FORM_CNIL");
		return appliConfig.getValue();
	}
	
	
	public String displayFormCrous() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("DISPLAY_FORM_CROUS");
		return appliConfig.getValue();
	}
	
	
	public String displayFormAdresse() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("DISPLAY_FORM_ADRESSE");
		return appliConfig.getValue();
	}
	
		
	public String displayFormRules() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("DISPLAY_FORM_RULES");
		return appliConfig.getValue();
	}
	
	public String displayFormEuropeanCard() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("DISPLAY_FORM_EUROPEAN");
		return appliConfig.getValue();
	}

	//
	public Double getMontantRenouvellement() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MONTANT_RENOUVELLEMENT");
		return new Double(appliConfig.getValue());
	}
	
	
	public String getListePpale() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MAIL_LISTE_PRINCIPALE");
		return appliConfig.getValue();
	}
	
	
	public String getListeCc() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MAIL_LISTE_CC");
		return appliConfig.getValue();
	}
	
	
	public String getSubjectAutoCard() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MAIL_SUBJECT_AUTO");
		return appliConfig.getValue();
	}
	
	
	public String getNoReplyMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MAIL_NO_REPLY");
		return appliConfig.getValue();
	}
	
	
	public String getPayboxMessage() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("PAYBOX_MSG_SUCCESS");
		return appliConfig.getValue();
	}
	
	
	public String getUserHelpMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_HELP");
		return appliConfig.getValue();
	}
	
	
	public String getUseFreeRenewalMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_FREE_RENEWAL");
		return appliConfig.getValue();
	}
	
	
	public String getUserPaidRenewalMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_PAID_RENEWAL");
		return appliConfig.getValue();
	}		
	
	
	public String getUserCanPaidRenewalMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_CAN_PAID_RENEWAL");
		return appliConfig.getValue();
	}
	
	public String getNewCardlMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_NEW_CARD");
		return appliConfig.getValue();
	}	
	
	
	public String getCheckedOrEncodedCardMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_CHECKED_OR_ENCODED_CARD");
		return appliConfig.getValue();
	}	
	
	
	public String getRejectedCardMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_REJECTED_CARD");
		return appliConfig.getValue();
	}	
	
	
	public String getEnabledCardMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_ENABLED_CARD");
		return appliConfig.getValue();
	}
	
	
	public String getEnabledCardPersMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_ENABLED_PERS_CARD");
		return appliConfig.getValue();
	}
	
	
	public String getCurrentAnneeUniv() {
		if(AppliConfig.countFindAppliConfigsByKeyEquals("ANNEE_UNIV")>0){
			AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("ANNEE_UNIV");
			return appliConfig.getValue();
		}else{
			return null;
		}
	}
	
	
	public String getUserFormRejectedMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_MSG_FORM_REJECTED");
		return appliConfig.getValue();
	}
	
	
	public String getUserFormRules() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_FORM_RULES");
		return appliConfig.getValue();
	}
	
	
	public String getUserFreeForcedRenewal() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_FREE_FORCED_RENEWAL");
		return appliConfig.getValue();
	}
	
	
	public String getUserTipMsg() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("USER_TIP_MSG");
		return appliConfig.getValue();
	}
	
	
	public String getBannedIpStats() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("STATS_BANNED_IP");
		return appliConfig.getValue();
	}
	
	//
	public Boolean getEnableAuto() {
		AppliConfig enableAutoConfig = AppliConfig.findAppliConfigByKey("ENABLE_AUTO");
		return enableAutoConfig!=null && "true".equalsIgnoreCase(enableAutoConfig.getValue());
	}
	
	
	public String getHelpManager() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("HELP_MANAGER");
		return appliConfig.getValue();
	}
	
	
	public String getHelpUser() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("HELP_USER");
		return appliConfig.getValue();
	}
	
	
	public String getHelpAdmin() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("HELP_ADMIN");
		return appliConfig.getValue();
	}
	
	//
	public Boolean isQrCodeEscEnabled() {
		AppliConfig enableAutoConfig = AppliConfig.findAppliConfigByKey("QRCODE_ESC_ENABLED");
		return enableAutoConfig!=null && "true".equalsIgnoreCase(enableAutoConfig.getValue());
	}

	
	public String getQrcodeWith() {
		AppliConfig qrcodeWidth = AppliConfig.findAppliConfigByKey("QRCODE_WIDTH");
		return qrcodeWidth==null ? "1.2cm" : qrcodeWidth.getValue();
	}
	
	public String getQrcodeFormat() {
		AppliConfig qrcodeWidth = AppliConfig.findAppliConfigByKey("QRCODE_FORMAT");
		return qrcodeWidth==null ? "PNG" : qrcodeWidth.getValue();
	}
	
	
	public String getModeLivraison() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MODE_LIVRAISON");
		return appliConfig.getValue();
	}
	
		
	public String getModeBornes() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("MODE_BORNES");
		return appliConfig.getValue();
	}
	
	public String getCardMask() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("CARD_MASK");
		return appliConfig.getValue();
	}
	
	public String getCardLogo() {
		AppliConfig appliConfig = AppliConfig.findAppliConfigByKey("CARD_LOGO");
		return appliConfig.getValue();
	}

}

