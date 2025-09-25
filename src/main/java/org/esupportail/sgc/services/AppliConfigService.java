package org.esupportail.sgc.services;

import jakarta.annotation.Resource;
import org.esupportail.sgc.dao.AppliConfigDaoService;
import org.esupportail.sgc.dao.AppliVersionDaoService;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliConfig.TypeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Service
public class AppliConfigService {
	
	private static final String DELIMITER_MULTIPLE_VALUES = ";";

	private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private AppliVersionDaoService appliVersionDaoService;

    enum AppliConfigKey {
		DISPLAY_FORM_CNIL, DISPLAY_FORM_CROUS, DISPLAY_FORM_ADRESSE, DISPLAY_FORM_RULES, MONTANT_RENOUVELLEMENT, MAIL_LISTE_PRINCIPALE, MAIL_SUBJECT_AUTO,
		MAIL_NO_REPLY, PAYBOX_MSG_SUCCESS, USER_MSG_HELP, USER_MSG_FREE_RENEWAL, USER_MSG_PAID_RENEWAL, USER_MSG_CAN_PAID_RENEWAL, USER_MSG_NEW_CARD, USER_MSG_CHECKED_OR_ENCODED_CARD,
		USER_MSG_REJECTED_CARD, USER_MSG_ENABLED_CARD, USER_MSG_ENABLED_PERS_CARD, ANNEE_UNIV, USER_MSG_FORM_REJECTED, USER_FORM_RULES, USER_FREE_FORCED_RENEWAL, 
		USER_TIP_MSG, ENABLE_AUTO, HELP_MANAGER, HELP_USER, HELP_ADMIN, QRCODE_ESC_ENABLED, QRCODE_FORMAT, MODE_LIVRAISON, ENABLE_CROUS, 
		ENABLE_EUROPEAN_CARD, DISPLAY_FORM_EUROPEAN_CARD, PAGE_FOOTER, EXT_USER_EPPN_REGEXP, RETENTION_LOGS_DB_DAYS, P2S_EXPORT_CSV_FILE_NAME, P2S_EXPORT_CSV_NB_LINES_PER_FILE,
		SYNCHRONIC_EXPORT_CSV_FILE_NAME, TIL_EXPORT_CSV_FILE_NAME, DEFAULT_CNOUS_ID_COMPAGNY_RATE, DEFAULT_CNOUS_ID_RATE, DEFAULT_DATE_FIN_DROITS, PHOTO_SIZE_MAX, PHOTO_BORDEREAU, 
		PAIEMENT_ALERT_MAILTO, PAIEMENT_ALERT_MAILBODY, CROUS_INE_AS_IDENTIFIER, EUROPEAN_CARD_INFO,

		BMP_COMMAND_COLOR_PRINTER, BMP_COMMAND_BLACK_PRINTER, BMP_COMMAND_BACK_PRINTER, BMP_COMMAND_VIRTUAL,

		ESUP_SGC_ETABLISSEMENT_NAME,

		PRINTER_ROLE_CONFIG
	}

    @Resource
    AppliConfigDaoService appliConfigDaoService;

	private List<String> splitConfigValues(AppliConfig appliConfig) {
		String userTypeAsString = appliConfig.getValue().trim();
		List<String> userTypes = Arrays.asList(userTypeAsString.split(""));
		if(userTypeAsString.contains(DELIMITER_MULTIPLE_VALUES)) {
			userTypes = Arrays.asList(userTypeAsString.split(DELIMITER_MULTIPLE_VALUES));
		}
		return userTypes;
	}
	
	public List<String> getTypes() {
		return Arrays.asList(new String[] {TypeConfig.HTML.name(), TypeConfig.TEXT.name(), TypeConfig.BOOLEAN.name()});
	}  
	
	public void merge(AppliConfig appliConfig) {
        appliConfigDaoService.merge(appliConfig);
	}
	
	
	public void remove(AppliConfig appliConfig) {
        appliConfigDaoService.remove(appliConfig);
	}

	
	public void persist(AppliConfig appliConfig) {
        appliConfigDaoService.persist(appliConfig);
	}
	
	
	public List<String> userTypes2displayFormCnil() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.DISPLAY_FORM_CNIL);
		return splitConfigValues(appliConfig);
	}
	
	
	public List<String> userTypes2displayFormCrous() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.DISPLAY_FORM_CROUS);
		return splitConfigValues(appliConfig);
	}
	
	
	public List<String> userTypes2displayFormAdresse() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.DISPLAY_FORM_ADRESSE);
		return splitConfigValues(appliConfig);
	}
	
		
	public List<String> userTypes2displayFormRules() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.DISPLAY_FORM_RULES);
		return splitConfigValues(appliConfig);
	}
	
	public Double getMontantRenouvellement() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.MONTANT_RENOUVELLEMENT);
		return Double.valueOf(appliConfig.getValue());
	}
	
	
	public String getListePpale() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.MAIL_LISTE_PRINCIPALE);
		return appliConfig.getValue();
	}
	
	public String getSubjectAutoCard() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.MAIL_SUBJECT_AUTO);
		return appliConfig.getValue();
	}
	
	
	public String getNoReplyMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.MAIL_NO_REPLY);
		return appliConfig.getValue();
	}
	
	
	public String getPayboxMessage() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.PAYBOX_MSG_SUCCESS);
		return appliConfig.getValue();
	}
	
	
	public String getUserHelpMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_HELP);
		return appliConfig.getValue();
	}
	
	
	public String getUseFreeRenewalMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_FREE_RENEWAL);
		return appliConfig.getValue();
	}
	
	
	public String getUserPaidRenewalMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_PAID_RENEWAL);
		return appliConfig.getValue();
	}		
	
	
	public String getUserCanPaidRenewalMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_CAN_PAID_RENEWAL);
		return appliConfig.getValue();
	}
	
	public String getNewCardlMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_NEW_CARD);
		return appliConfig.getValue();
	}	
	
	
	public String getCheckedOrEncodedCardMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_CHECKED_OR_ENCODED_CARD);
		return appliConfig.getValue();
	}	
	
	
	public String getRejectedCardMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_REJECTED_CARD);
		return appliConfig.getValue();
	}	
	
	
	public String getEnabledCardMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_ENABLED_CARD);
		return appliConfig.getValue();
	}
	
	
	public String getEnabledCardPersMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_ENABLED_PERS_CARD);
		return appliConfig.getValue();
	}
	
	
	public String getCurrentAnneeUniv() {
		if(appliConfigDaoService.countFindAppliConfigsByKeyEquals("ANNEE_UNIV")>0){
			AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.ANNEE_UNIV);
			return appliConfig.getValue();
		}else{
			return null;
		}
	}
	
	
	public String getUserFormRejectedMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_MSG_FORM_REJECTED);
		return appliConfig.getValue();
	}
	
	
	public String getUserFormRules() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_FORM_RULES);
		return appliConfig.getValue();
	}
	
	
	public String getUserFreeForcedRenewal() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_FREE_FORCED_RENEWAL);
		return appliConfig.getValue();
	}
	
	
	public String getUserTipMsg() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.USER_TIP_MSG);
		return appliConfig.getValue();
	}

	public Boolean getEnableAuto() {
		AppliConfig enableAutoConfig = getAppliConfigByKey(AppliConfigKey.ENABLE_AUTO);
		return enableAutoConfig!=null && "true".equalsIgnoreCase(enableAutoConfig.getValue());
	}
	
	
	public String getHelpManager() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.HELP_MANAGER);
		return appliConfig.getValue();
	}
	
	
	public String getHelpUser() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.HELP_USER);
		return appliConfig.getValue();
	}
	
	
	public String getHelpAdmin() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.HELP_ADMIN);
		return appliConfig.getValue();
	}

	public String getEuropeanCardInfo() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.EUROPEAN_CARD_INFO);
		return appliConfig.getValue();
	}
	
	//
	public Boolean isQrCodeEscEnabled() {
		AppliConfig enableAutoConfig = getAppliConfigByKey(AppliConfigKey.QRCODE_ESC_ENABLED);
		return enableAutoConfig!=null && "true".equalsIgnoreCase(enableAutoConfig.getValue());
	}

	public String getQrcodeFormat() {
		AppliConfig qrcodeWidth = getAppliConfigByKey(AppliConfigKey.QRCODE_FORMAT);
		return qrcodeWidth==null ? "PNG" : qrcodeWidth.getValue();
	}
		
	public String getModeLivraison() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.MODE_LIVRAISON);
		return appliConfig.getValue();
	}
	
	
	public List<String> userTypes4isCrousEnabled() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.ENABLE_CROUS);
		return splitConfigValues(appliConfig);
	}
	
	public List<String> userTypes4isEuropeanCardEnabled() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.ENABLE_EUROPEAN_CARD);
		return splitConfigValues(appliConfig);
	}
	
	public List<String> userTypes2displayFormEuropeanCard() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.DISPLAY_FORM_EUROPEAN_CARD);
		return splitConfigValues(appliConfig);
	}
	
	public String pageFooter() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.PAGE_FOOTER);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public String getEppnRegexpExt() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.EXT_USER_EPPN_REGEXP);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public int getDaysNbConfig() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.RETENTION_LOGS_DB_DAYS);
		return appliConfig==null ? 36000 : Integer.parseInt(appliConfig.getValue());
	}
	
	public String getP2sExportcsvFilename() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.P2S_EXPORT_CSV_FILE_NAME);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public int getP2sExportcsvNbLinesMax() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.P2S_EXPORT_CSV_NB_LINES_PER_FILE);
		return appliConfig==null ? 100 : Integer.parseInt(appliConfig.getValue());
	}
	
	public String getSynchronicExportcsvFilename() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.SYNCHRONIC_EXPORT_CSV_FILE_NAME);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public String getTilExportcsvFilename() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.TIL_EXPORT_CSV_FILE_NAME);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public Long getDefaultCnousIdCompagnyRate() {
		return Long.valueOf(getAppliConfigByKey(AppliConfigKey.DEFAULT_CNOUS_ID_COMPAGNY_RATE).getValue());
	}
	
	public Long getDefaultCnousIdRate() {
		return Long.valueOf(getAppliConfigByKey(AppliConfigKey.DEFAULT_CNOUS_ID_RATE).getValue());
	}
	
	public LocalDateTime getDefaultDateFinDroits() {
		String timeString = getAppliConfigByKey(AppliConfigKey.DEFAULT_DATE_FIN_DROITS).getValue();
		LocalDateTime dateFinDroits = LocalDate.parse(timeString).atStartOfDay();
		return dateFinDroits;
	}
	
	public Long getFileSizeMax() {
		String fileSizeMax = "200000";
		if(getAppliConfigByKey(AppliConfigKey.PHOTO_SIZE_MAX) != null){
			fileSizeMax = getAppliConfigByKey(AppliConfigKey.PHOTO_SIZE_MAX).getValue();
		}
		
		return Long.valueOf(fileSizeMax);
	}
	
	public Boolean getPhotoBordereau() {
		return appliConfigDaoService.countFindAppliConfigsByKeyLike(AppliConfigKey.PHOTO_BORDEREAU.name())==0 ? false : Boolean.valueOf(getAppliConfigByKey(AppliConfigKey.PHOTO_BORDEREAU).getValue());
	}
	
	public String getPaiementAlertMailto() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.PAIEMENT_ALERT_MAILTO);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public String getPaiementAlertMailbody() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.PAIEMENT_ALERT_MAILBODY);
		return appliConfig==null ? "" : appliConfig.getValue();
	}
	
	public Boolean getCrousIneAsIdentifier() {
		AppliConfig crousIneAsIdentifier = getAppliConfigByKey(AppliConfigKey.CROUS_INE_AS_IDENTIFIER);
		return crousIneAsIdentifier!=null && "true".equalsIgnoreCase(crousIneAsIdentifier.getValue());
	}

	protected AppliConfig getAppliConfigByKey(AppliConfigKey appliConfigKey) {
		return appliConfigDaoService.findAppliConfigByKey(appliConfigKey.name());
	}

	private String getEsupSgcEtablissementName() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.ESUP_SGC_ETABLISSEMENT_NAME);
		return appliConfig==null ? "" : appliConfig.getValue().replaceAll("[^a-zA-Z0-9]", "");
	}
	
	public String getEsupSgcAsHttpUserAgent() {
		String userAgent = String.format("ESUP-SGC/%s ; %s", appliVersionDaoService.getAppliVersion().getEsupSgcVersion(), getEsupSgcEtablissementName());
		return userAgent;
	}

	public String getEsupSgcWoEtablissementAsHttpUserAgent() {
		String userAgent = String.format("ESUP-SGC/%s", appliVersionDaoService.getAppliVersion().getEsupSgcVersion());
		return userAgent;
	}

	public String getBmpCardCommandColor4printer() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.BMP_COMMAND_COLOR_PRINTER);
		return appliConfig==null ? "" : appliConfig.getValue();
	}

	public String getBmpCardCommandBlack4printer() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.BMP_COMMAND_BLACK_PRINTER);
		return appliConfig==null ? "" : appliConfig.getValue();
	}

	public String getBmpCardCommandBack4printer() {
		AppliConfig appliConfig = getAppliConfigByKey(AppliConfigKey.BMP_COMMAND_BACK_PRINTER);
		return appliConfig==null ? "" : appliConfig.getValue();
	}

	public String getBmpCardCommandVirtual() {
		AppliConfig appliConfig = null;
		try {
			appliConfig = getAppliConfigByKey(AppliConfigKey.BMP_COMMAND_VIRTUAL);
		} catch(Exception e) {
			log.warn("Config BMP_COMMAND_VIRTUAL not found", e);
		}
		return appliConfig==null ? "" : appliConfig.getValue();
	}

	public Boolean getPrinterRoleConfig() {
		return appliConfigDaoService.countFindAppliConfigsByKeyLike(AppliConfigKey.PRINTER_ROLE_CONFIG.name())==0 ? false : Boolean.valueOf(getAppliConfigByKey(AppliConfigKey.PRINTER_ROLE_CONFIG).getValue());
	}

}
