package org.esupportail.sgc.domain.ldap;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJson
public class PersonLdap {
	private String uid;
	private String cn ;
	private String sn; 
	private String givenName ;
	private String displayName ;
	private String mail ;
	private String md5UserPassword ;
	private String cryptUserPassword ;
	private String shaUserPassword ;
	private String eduPersonAffiliation ;
	private String eduPersonPrimaryAffiliation ;
	private String eduPersonPrincipalName ;
	private String mailDrop ;
	private String mailHost ;
	private String sambaSID ;
	private String sambaPrimaryGroupSID ;
	private String sambaPwdLastSet ;
	private String sambaLMPassword ;
	private String sambaNTPassword ;
	private String sambaAcctFlags ;
	private String homeDirectory ;
	private String uidNumber ;
	private String gidNumber ;
	private String postalAddress ;
	private String facsimileTelephoneNumber ;
	private String telephoneNumber ;
	private String supannCivilite ;
	private String supannListeRouge ;
	private String supannEtablissement ;
	private String supannEntiteAffectation ;
	private String supannEntiteAffectationPrincipale ;
	private String supannEmpId ;
	private String supannEmpCorps ;
	private String supannActivite ;
	private String supannAutreTelephone ;
	private String supannCodeINE ;
	private String supannEtuId ;
	private String supannEtuEtape ;
	private String supannEtuAnneeInscription ;
	private String supannEtuSecteurDisciplinaire ;
	private String supannEtuDiplome ;
	private String supannEtuTypeDiplome ;
	private String supannEtuCursusAnnee ;
	private String supannParrainDN ;
	private String supannMailPerso ;
	private String supannAliasLogin ;
	private String supannRoleGenerique ;
	private String schacDateOfBirth;
  	public String getSchacDateOfBirth() {
		return schacDateOfBirth;
	}
	public void setSchacDateOfBirth(String schacDateOfBirth) {
		this.schacDateOfBirth = schacDateOfBirth;
	}
	// Affectation des variables
	private String supannAutreMail ;
	private Long mailuserquota ;
	private String swissEduPersonCardUID;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCn() {
		return cn;
	}
	public void setCn(String cn) {
		this.cn = cn;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getMd5UserPassword() {
		return md5UserPassword;
	}
	public void setMd5UserPassword(String md5UserPassword) {
		this.md5UserPassword = md5UserPassword;
	}
	public String getCryptUserPassword() {
		return cryptUserPassword;
	}
	public void setCryptUserPassword(String cryptUserPassword) {
		this.cryptUserPassword = cryptUserPassword;
	}
	public String getShaUserPassword() {
		return shaUserPassword;
	}
	public void setShaUserPassword(String shaUserPassword) {
		this.shaUserPassword = shaUserPassword;
	}
	public String getEduPersonAffiliation() {
		return eduPersonAffiliation;
	}
	public void setEduPersonAffiliation(String eduPersonAffiliation) {
		this.eduPersonAffiliation = eduPersonAffiliation;
	}
	public String getEduPersonPrimaryAffiliation() {
		return eduPersonPrimaryAffiliation;
	}
	public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
		this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
	}
	public String getEduPersonPrincipalName() {
		return eduPersonPrincipalName;
	}
	public void setEduPersonPrincipalName(String eduPersonPrincipalName) {
		this.eduPersonPrincipalName = eduPersonPrincipalName;
	}
	public String getMailDrop() {
		return mailDrop;
	}
	public void setMailDrop(String mailDrop) {
		this.mailDrop = mailDrop;
	}
	public String getMailHost() {
		return mailHost;
	}
	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}
	public String getSambaSID() {
		return sambaSID;
	}
	public void setSambaSID(String sambaSID) {
		this.sambaSID = sambaSID;
	}
	public String getSambaPrimaryGroupSID() {
		return sambaPrimaryGroupSID;
	}
	public void setSambaPrimaryGroupSID(String sambaPrimaryGroupSID) {
		this.sambaPrimaryGroupSID = sambaPrimaryGroupSID;
	}
	public String getSambaPwdLastSet() {
		return sambaPwdLastSet;
	}
	public void setSambaPwdLastSet(String sambaPwdLastSet) {
		this.sambaPwdLastSet = sambaPwdLastSet;
	}
	public String getSambaLMPassword() {
		return sambaLMPassword;
	}
	public void setSambaLMPassword(String sambaLMPassword) {
		this.sambaLMPassword = sambaLMPassword;
	}
	public String getSambaNTPassword() {
		return sambaNTPassword;
	}
	public void setSambaNTPassword(String sambaNTPassword) {
		this.sambaNTPassword = sambaNTPassword;
	}
	public String getSambaAcctFlags() {
		return sambaAcctFlags;
	}
	public void setSambaAcctFlags(String sambaAcctFlags) {
		this.sambaAcctFlags = sambaAcctFlags;
	}
	public String getHomeDirectory() {
		return homeDirectory;
	}
	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
	public String getUidNumber() {
		return uidNumber;
	}
	public void setUidNumber(String uidNumber) {
		this.uidNumber = uidNumber;
	}
	public String getGidNumber() {
		return gidNumber;
	}
	public void setGidNumber(String gidNumber) {
		this.gidNumber = gidNumber;
	}
	public String getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}
	public String getFacsimileTelephoneNumber() {
		return facsimileTelephoneNumber;
	}
	public void setFacsimileTelephoneNumber(String facsimileTelephoneNumber) {
		this.facsimileTelephoneNumber = facsimileTelephoneNumber;
	}
	public String getTelephoneNumber() {
		return telephoneNumber;
	}
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	public String getSupannCivilite() {
		return supannCivilite;
	}
	public void setSupannCivilite(String supannCivilite) {
		this.supannCivilite = supannCivilite;
	}
	public String getSupannListeRouge() {
		return supannListeRouge;
	}
	public void setSupannListeRouge(String supannListeRouge) {
		this.supannListeRouge = supannListeRouge;
	}
	public String getSupannEtablissement() {
		return supannEtablissement;
	}
	public void setSupannEtablissement(String supannEtablissement) {
		this.supannEtablissement = supannEtablissement;
	}
	public String getSupannEntiteAffectation() {
		return supannEntiteAffectation;
	}
	public void setSupannEntiteAffectation(String supannEntiteAffectation) {
		this.supannEntiteAffectation = supannEntiteAffectation;
	}
	public String getSupannEntiteAffectationPrincipale() {
		return supannEntiteAffectationPrincipale;
	}
	public void setSupannEntiteAffectationPrincipale(String supannEntiteAffectationPrincipale) {
		this.supannEntiteAffectationPrincipale = supannEntiteAffectationPrincipale;
	}
	public String getSupannEmpId() {
		return supannEmpId;
	}
	public void setSupannEmpId(String supannEmpId) {
		this.supannEmpId = supannEmpId;
	}
	public String getSupannEmpCorps() {
		return supannEmpCorps;
	}
	public void setSupannEmpCorps(String supannEmpCorps) {
		this.supannEmpCorps = supannEmpCorps;
	}
	public String getSupannActivite() {
		return supannActivite;
	}
	public void setSupannActivite(String supannActivite) {
		this.supannActivite = supannActivite;
	}
	public String getSupannAutreTelephone() {
		return supannAutreTelephone;
	}
	public void setSupannAutreTelephone(String supannAutreTelephone) {
		this.supannAutreTelephone = supannAutreTelephone;
	}
	public String getSupannCodeINE() {
		return supannCodeINE;
	}
	public void setSupannCodeINE(String supannCodeINE) {
		this.supannCodeINE = supannCodeINE;
	}
	public String getSupannEtuId() {
		return supannEtuId;
	}
	public void setSupannEtuId(String supannEtuId) {
		this.supannEtuId = supannEtuId;
	}
	public String getSupannEtuEtape() {
		return supannEtuEtape;
	}
	public void setSupannEtuEtape(String supannEtuEtape) {
		this.supannEtuEtape = supannEtuEtape;
	}
	public String getSupannEtuAnneeInscription() {
		return supannEtuAnneeInscription;
	}
	public void setSupannEtuAnneeInscription(String supannEtuAnneeInscription) {
		this.supannEtuAnneeInscription = supannEtuAnneeInscription;
	}
	public String getSupannEtuSecteurDisciplinaire() {
		return supannEtuSecteurDisciplinaire;
	}
	public void setSupannEtuSecteurDisciplinaire(String supannEtuSecteurDisciplinaire) {
		this.supannEtuSecteurDisciplinaire = supannEtuSecteurDisciplinaire;
	}
	public String getSupannEtuDiplome() {
		return supannEtuDiplome;
	}
	public void setSupannEtuDiplome(String supannEtuDiplome) {
		this.supannEtuDiplome = supannEtuDiplome;
	}
	public String getSupannEtuTypeDiplome() {
		return supannEtuTypeDiplome;
	}
	public void setSupannEtuTypeDiplome(String supannEtuTypeDiplome) {
		this.supannEtuTypeDiplome = supannEtuTypeDiplome;
	}
	public String getSupannEtuCursusAnnee() {
		return supannEtuCursusAnnee;
	}
	public void setSupannEtuCursusAnnee(String supannEtuCursusAnnee) {
		this.supannEtuCursusAnnee = supannEtuCursusAnnee;
	}
	public String getSupannParrainDN() {
		return supannParrainDN;
	}
	public void setSupannParrainDN(String supannParrainDN) {
		this.supannParrainDN = supannParrainDN;
	}
	public String getSupannMailPerso() {
		return supannMailPerso;
	}
	public void setSupannMailPerso(String supannMailPerso) {
		this.supannMailPerso = supannMailPerso;
	}
	public String getSupannAliasLogin() {
		return supannAliasLogin;
	}
	public void setSupannAliasLogin(String supannAliasLogin) {
		this.supannAliasLogin = supannAliasLogin;
	}
	public String getSupannRoleGenerique() {
		return supannRoleGenerique;
	}
	public void setSupannRoleGenerique(String supannRoleGenerique) {
		this.supannRoleGenerique = supannRoleGenerique;
	}
	public String getSupannAutreMail() {
		return supannAutreMail;
	}
	public void setSupannAutreMail(String supannAutreMail) {
		this.supannAutreMail = supannAutreMail;
	}
	public Long getMailuserquota() {
		return mailuserquota;
	}
	public void setMailuserquota(Long mailuserquota) {
		this.mailuserquota = mailuserquota;
	}
	public String getSwissEduPersonCardUID() {
		return swissEduPersonCardUID;
	}
	public void setSwissEduPersonCardUID(String swissEduPersonCardUID) {
		this.swissEduPersonCardUID = swissEduPersonCardUID;
	}
	
	}
	
	
