package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Entity
public class PayBoxForm {

    private String eppn;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMontant() {
        return Double.valueOf(Double.parseDouble(total) / 100.0).toString();
    }

	public String getEppn() {
        return this.eppn;
    }

	public void setEppn(String eppn) {
        this.eppn = eppn;
    }

	public Date getRequestDate() {
        return this.requestDate;
    }

	public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

	public String getActionUrl() {
        return this.actionUrl;
    }

	public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

	public String getSite() {
        return this.site;
    }

	public void setSite(String site) {
        this.site = site;
    }

	public String getRang() {
        return this.rang;
    }

	public void setRang(String rang) {
        this.rang = rang;
    }

	public String getIdentifiant() {
        return this.identifiant;
    }

	public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

	public String getTotal() {
        return this.total;
    }

	public void setTotal(String total) {
        this.total = total;
    }

	public String getDevise() {
        return this.devise;
    }

	public void setDevise(String devise) {
        this.devise = devise;
    }

	public String getCommande() {
        return this.commande;
    }

	public void setCommande(String commande) {
        this.commande = commande;
    }

	public String getClientEmail() {
        return this.clientEmail;
    }

	public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

	public String getRetourVariables() {
        return this.retourVariables;
    }

	public void setRetourVariables(String retourVariables) {
        this.retourVariables = retourVariables;
    }

	public String getHash() {
        return this.hash;
    }

	public void setHash(String hash) {
        this.hash = hash;
    }

	public String getTime() {
        return this.time;
    }

	public void setTime(String time) {
        this.time = time;
    }

	public String getCallbackUrl() {
        return this.callbackUrl;
    }

	public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

	public String getForwardEffectueUrl() {
        return this.forwardEffectueUrl;
    }

	public void setForwardEffectueUrl(String forwardEffectueUrl) {
        this.forwardEffectueUrl = forwardEffectueUrl;
    }

	public String getForwardRefuseUrl() {
        return this.forwardRefuseUrl;
    }

	public void setForwardRefuseUrl(String forwardRefuseUrl) {
        this.forwardRefuseUrl = forwardRefuseUrl;
    }

	public String getForwardAnnuleUrl() {
        return this.forwardAnnuleUrl;
    }

	public void setForwardAnnuleUrl(String forwardAnnuleUrl) {
        this.forwardAnnuleUrl = forwardAnnuleUrl;
    }

	public String getHmac() {
        return this.hmac;
    }

	public void setHmac(String hmac) {
        this.hmac = hmac;
    }

}
