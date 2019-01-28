package org.esupportail.sgc.domain;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.Column;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;

@RooJavaBean
@RooJpaActiveRecord(finders = { "findPayBoxFormsByCommandeEquals", "findPayBoxFormsByRequestDateLessThan" })
public class PayBoxForm {

    private String eppn;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
    private Date requestDate;

    private String actionUrl;

    private String site;

    private String rang;

    private String identifiant;

    private String total;

    private String devise;

    private String commande;

    private String clientEmail;

    private String retourVariables;

    private String hash;

    private String time;

    private String callbackUrl;

    private String forwardEffectueUrl;

    private String forwardRefuseUrl;

    private String forwardAnnuleUrl;

    private String hmac;

    public SortedMap<String, String> getOrderedParams() {
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("PBX_SITE", site);
        params.put("PBX_RANG", rang);
        params.put("PBX_IDENTIFIANT", identifiant);
        params.put("PBX_TOTAL", total);
        params.put("PBX_DEVISE", devise);
        params.put("PBX_CMD", commande);
        params.put("PBX_PORTEUR", clientEmail);
        params.put("PBX_RETOUR", retourVariables);
        params.put("PBX_HASH", hash);
        params.put("PBX_TIME", time);
        params.put("PBX_REPONDRE_A", callbackUrl);
        params.put("PBX_EFFECTUE", forwardEffectueUrl);
        params.put("PBX_REFUSE", forwardRefuseUrl);
        params.put("PBX_ANNULE", forwardAnnuleUrl);
        // params.put("PBX_HMAC", hmac);
        return params;
    }

    public String getParamsAsString() {
        String paramsAsString = "";
        SortedMap<String, String> params = getOrderedParams();
        for (String key : params.keySet()) {
            paramsAsString = paramsAsString + key + "=" + params.get(key) + "&";
        }
        paramsAsString = paramsAsString.subSequence(0, paramsAsString.length() - 1).toString();
        try {
            // paramsAsString = URLEncoder.encode(paramsAsString, "utf8");
            // System.out.println(paramsAsString);
            return paramsAsString;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMontant() {
        return new Double(new Double(total) / 100.0).toString();
    }
}
