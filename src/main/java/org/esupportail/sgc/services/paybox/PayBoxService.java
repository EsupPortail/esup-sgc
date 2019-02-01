package org.esupportail.sgc.services.paybox;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.PayBoxForm;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;

public class PayBoxService {

	private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String RETOUR_VARIABLES = "montant:M;reference:R;auto:A;erreur:E;idtrans:S;signature:K";

    private HashService hashService;
    
    @Resource
    private AppliConfigService appliConfigService;
    
	@Resource
	protected EmailService emailService;	

    private String site;

    private String rang;

    private String identifiant;

    private String devise;

    private List<String> payboxActionUrls;

    private PublicKey payboxPublicKey;

    private String reponseServerUrl;
    
    private String numCommandeFormat = "LEOCARTE-NEW@@%eppn%@@%montantAsCents%-%date%";

    public void setHashService(HashService hashService) {
        this.hashService = hashService;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public void setPayboxActionUrls(List<java.lang.String> payboxActionUrls) {
        this.payboxActionUrls = payboxActionUrls;
    }

    public void setDerPayboxPublicKeyFile(String derPayboxPublicKeyFile) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        org.springframework.core.io.Resource derPayboxPublicKeyRessource = new ClassPathResource(derPayboxPublicKeyFile);
        InputStream fis = derPayboxPublicKeyRessource.getInputStream();
        DataInputStream dis = new DataInputStream(fis);
        byte[] pubKeyBytes = new byte[fis.available()];
        dis.readFully(pubKeyBytes);
        fis.close();
        dis.close();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.payboxPublicKey = kf.generatePublic(x509EncodedKeySpec);
    }

    public void setReponseServerUrl(String reponseServerUrl) {
        this.reponseServerUrl = reponseServerUrl;
    }
    
    public void setNumCommandeFormat(String numCommandeFormat) {
        this.numCommandeFormat = numCommandeFormat;
    }

    public PayBoxForm getPayBoxForm(String eppn, String mail, double montant) {
        String montantAsCents = Integer.toString(new Double(montant * 100).intValue());
        PayBoxForm payBoxForm = new PayBoxForm();
        payBoxForm.setActionUrl(getPayBoxActionUrl());
        payBoxForm.setClientEmail(mail);
        payBoxForm.setCommande(getNumCommande(eppn, montantAsCents));
        payBoxForm.setDevise(devise);
        payBoxForm.setHash(hashService.getHash());
        payBoxForm.setIdentifiant(identifiant);
        payBoxForm.setRang(rang);
        payBoxForm.setRetourVariables(RETOUR_VARIABLES);
        payBoxForm.setSite(site);
        payBoxForm.setTime(getCurrentTime());
        payBoxForm.setRequestDate(new Date());
        payBoxForm.setTotal(montantAsCents);
        String callbackUrl = reponseServerUrl + "/payboxcallback";
        String effectuerUrl = reponseServerUrl + "/user/payboxOk";
        String annulerUrl = reponseServerUrl + "/user/";
        payBoxForm.setCallbackUrl(callbackUrl);
        payBoxForm.setForwardAnnuleUrl(annulerUrl);
        payBoxForm.setForwardEffectueUrl(effectuerUrl);
        payBoxForm.setForwardRefuseUrl(annulerUrl);
        String hMac = hashService.getHMac(payBoxForm.getParamsAsString());
        payBoxForm.setHmac(hMac);
        payBoxForm.setEppn(eppn);
        payBoxForm.setRequestDate(new Date());
        payBoxForm.persist();
        return payBoxForm;
    }

	protected String getNumCommande(String eppn, String montantAsCents) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S");
        String numCommand = numCommandeFormat.replaceAll("%eppn%", eppn)
        								.replaceAll("%montantAsCents%", montantAsCents)
        								.replaceAll("%date%", df.format(new Date()));
        numCommand = replaceWithUserFields(eppn, numCommand);
        return numCommand;
    }

	private String replaceWithUserFields(String eppn, String numCommand) {
		User user = User.findUser(eppn);
		if(user != null) {
			numCommand = numCommand.replaceAll("%email%", user.getEmail());
			numCommand = numCommand.replaceAll("%name%", user.getName());
			numCommand = numCommand.replaceAll("%firstName%", user.getFirstname());
			numCommand = numCommand.replaceAll("%eduPersonPrimaryAffiliation%", user.getEduPersonPrimaryAffiliation());
			numCommand = numCommand.replaceAll("%secondaryId%", user.getSecondaryId());
			numCommand = numCommand.replaceAll("%supannEtuId%", user.getSupannEtuId());
			numCommand = numCommand.replaceAll("%supannEmpId%", user.getSupannEmpId());
			numCommand = numCommand.replaceAll("%supannCodeINE%", user.getSupannCodeINE());
			numCommand = numCommand.replaceAll("%userType%", user.getUserType());
			numCommand = numCommand.replaceAll("%supannEntiteAffectationPrincipale%", user.getSupannEntiteAffectationPrincipale());			
		}
		return numCommand;
	}

	protected String getPayBoxActionUrl() {
        for (String payboxActionUrl : payboxActionUrls) {
            try {
                URL url = new URL(payboxActionUrl);
                URLConnection connection = url.openConnection();
                connection.connect();
                connection.getInputStream().read();
                return payboxActionUrl;
            } catch (Exception e) {
                log.warn("Pb with " + payboxActionUrl, e);
            }
        }
        throw new RuntimeException("No paybox action url is available at the moment !");
    }

    protected String getCurrentTime() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        return nowAsISO;
    }

    public boolean checkPayboxSignature(String queryString, String signature) {
        String sData = queryString.substring(0, queryString.lastIndexOf("&"));
        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(payboxPublicKey);
            sig.update(sData.getBytes());
            boolean signatureOk = true;//sig.verify(sigBytes);
            if (!signatureOk) {
                log.error("Erreur lors de la vérification de la signature, les données ne correspondent pas.");
                log.error(sData);
                log.error(signature);
            }
            return signatureOk;
        } catch (Exception e) {
            log.warn("Pb when checking SSL signature of Paybox", e);
            return false;
        }
    }

    public boolean payboxCallback(String montant, String reference, String auto, String erreur, String idtrans, String signature, String queryString, String ip) {
        List<PayboxTransactionLog> txLogs = PayboxTransactionLog.findPayboxTransactionLogsByIdtransEquals(idtrans).getResultList();
        boolean newTxLog = txLogs.size() == 0;
        PayboxTransactionLog txLog = txLogs.size() > 0 ? txLogs.get(0) : null;
        if (txLog == null) {
            txLog = new PayboxTransactionLog();
        } else {
            if (!"00000".equals(txLog.getErreur())) {
                log.info("This transaction + " + idtrans + " is already OK");
                return true;
            }
        }
        txLog.setMontant(montant);
        txLog.setReference(reference);
        txLog.setAuto(auto);
        txLog.setErreur(erreur);
        txLog.setIdtrans(idtrans);
        txLog.setSignature(signature);
        txLog.setTransactionDate(new Date());
        String eppn = getEppn(reference);
        txLog.setEppn(eppn);
        if (this.checkPayboxSignature(queryString, signature)) {
                if ("00000".equals(erreur)) {
                	log.info("Transaction : " + reference + " pour un montant de " + montant + " OK !");
                	if (newTxLog) {
                		txLog.persist();
                	} else {
                		txLog.merge();
                	}
                	try {
                		String paiementAlertMailto = appliConfigService.getPaiementAlertMailto();
                		String paiementAlertMailbody = appliConfigService.getPaiementAlertMailbody();
                		String mailFrom = appliConfigService.getNoReplyMsg();
                		if(!paiementAlertMailto.isEmpty() && !paiementAlertMailbody.isEmpty()) {
                			emailService.sendMessage(mailFrom, paiementAlertMailto, "Paiement paybox ESUP-SGC", paiementAlertMailbody);
                		}
                	} catch (Exception ex) {
                		log.error("Exception during sending email ?", ex);
                	}
                	return true;
                } else {
                    log.info("'Erreur' " + erreur + "  (annulation) lors de la transaction paybox : " + reference + " pour un montant de " + montant);
                }
            } else {
                log.error("signature checking of paybox failed, transaction " + txLog + " canceled.");
            }
        return false;
    }

    public String getEppn(String reference) {
    	String eppn = null;
    	List<PayBoxForm> payBoxForms =  PayBoxForm.findPayBoxFormsByCommandeEquals(reference).getResultList();
    	if(!payBoxForms.isEmpty()) {
    		eppn= payBoxForms.get(0).getEppn();
    	}
    	return eppn;
    }
    
    // Appelé tous les jours à 10H                                                                                                                                                                             
    @Scheduled(cron="0 0 10 * * *")
    public void purgeOldPayBoxForm() {
		int daysNb = 1;
		Date currentDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.DAY_OF_MONTH, -daysNb);
		Date datePurge = cal.getTime();
		List<PayBoxForm> payboxForms = PayBoxForm.findPayBoxFormsByRequestDateLessThan(datePurge).getResultList();
		for(PayBoxForm payboxForm: payboxForms) {
			payboxForm.remove();
		}
        log.info(payboxForms.size() + " formulaires de paiement en base vieux de " + daysNb  + " jour(s) purgés"); 	
    }
    
}
