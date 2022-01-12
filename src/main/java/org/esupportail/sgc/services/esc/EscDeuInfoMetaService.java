package org.esupportail.sgc.services.esc;

import java.io.ByteArrayInputStream;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class EscDeuInfoMetaService  {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	List<EscDeuInfoService> escDeuInfoServicesList;
		
	@Resource
	ApiEscrService apiEscrService;
	
	@Resource
	EscUidFactoryService escUidFactoryService;
	
	protected EscDeuInfoService getEscDeuInfoService(String pic) {
		if(escDeuInfoServicesList == null) {
			return null;
		}
		Map<String, EscDeuInfoService> escDeuInfoServices = escDeuInfoServicesList.stream().collect(Collectors.toMap(EscDeuInfoService::getPic, escDeuInfoService -> escDeuInfoService));;
		if(StringUtils.isEmpty(pic) && escDeuInfoServices.size()==1) {
			return escDeuInfoServices.values().iterator().next();
		} else if(!StringUtils.isEmpty(pic) && escDeuInfoServices.containsKey(pic)) {
			return escDeuInfoServices.get(pic);
		} 
		String errorMessage = "";
		if(StringUtils.isEmpty(pic)) {
			errorMessage = String.format("pic is null and there are not one (and only one) escDeuInfoServices configured but %s",  escDeuInfoServices.size());
		} else {
			errorMessage = String.format("No EscDeuInfoService found for PIC %s",  pic);
		}
		throw new SgcRuntimeException(errorMessage, null);
	}

	public String getDeuInfoEscnUid(Card card) {
		return getEscDeuInfoService(card.getUser().getPic()).getDeuInfoEscnUid(card);
	}

	public String getDeuInfoSignature(Card card) {
		return getEscDeuInfoService(card.getUser().getPic()).getDeuInfoSignature(card);
	}

	public String getPublicKeyAsHexa(Card card) throws Exception {
		return getEscDeuInfoService(card.getUser().getPic()).getPublicKeyAsHexa();
	}
	
	boolean checkSignature(String escnData, String signature, PublicKey pubKey) throws Exception {
		Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
		ecdsaVerify.initVerify(pubKey);
		ecdsaVerify.update(Hex.decodeHex(escnData.toCharArray()));
		boolean result = ecdsaVerify.verify(Hex.decodeHex(signature.toCharArray()));
		return result;		
	}


	public boolean check(String escnData, String signature, String certAsHexa, Boolean checkCertificate) {
		try {
			byte[] certBytes = Hex.decodeHex(certAsHexa.toCharArray());
			PublicKey pubKey = getPublicKeyFromCert(certBytes);
			if(checkSignature(escnData, signature, pubKey)) {
				log.debug("Signature OK");
				if(!checkCertificate) {
					return true;
				}
				String escn = escnData.substring(0, 32);
				String picInstitutionCode = escn.substring(23);
				String chainCertAsHexa = apiEscrService.getCaChainCertAsHexa(picInstitutionCode);
				log.debug("getChain from pic [" + picInstitutionCode +"] OK");
				return checkCert(certAsHexa, chainCertAsHexa);
			}
		} catch(HttpClientErrorException ce) {
			log.warn(String.format("Exception when getting  CaChainCert from ESCR for escnData %s", escnData), ce);
		} catch(Exception e) {
			log.warn(String.format("Exception when checking signature : %s", e));
			log.debug("Exception when checking signature", e);
		}
		return false;
	}

	boolean checkCert(String certAsHexa, String chainCertAsHexa) {
		
		try {
			
			byte[] certBytes = Hex.decodeHex(certAsHexa.toCharArray());
			byte[] chainCertBytes = Hex.decodeHex(chainCertAsHexa.toCharArray());
			
			//ByteArrayInputStream publicEscCaRootKeyIn = new ByteArrayInputStream(publicEscCaRootKey);
			ByteArrayInputStream in = new ByteArrayInputStream(certBytes);
			ByteArrayInputStream chainIn = new ByteArrayInputStream(chainCertBytes);
			
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			
			//Certificate rootCert = certFactory.generateCertificate(publicEscCaRootKeyIn);
			Certificate cert = certFactory.generateCertificate(in);
			Collection<Certificate> certificatesChainRev = (Collection<Certificate>) certFactory.generateCertificates(chainIn);
			List<Certificate> certificatesChain = new ArrayList<>(certificatesChainRev);
			Collections.reverse(certificatesChain);
			
			Certificate childCert = cert;
			int k = 0;
			for(Certificate c : certificatesChain) {
				/*
				if(k == certificatesChain.size()-1) {
					if(!c.equals(rootCert)) {
						log.warn("The last certificat of the chain is not the Root CA of ESC !");
						return false;
					}
					log.debug("The last certificat of the chain is the Root CA of ESC");
				} 
				*/
				childCert.verify(c.getPublicKey());	
				childCert = c;
				k++;
			}
			 
			return true;
			
		} catch(Exception e) {
			log.warn(String.format("Exception when checking certificate : %s", e));
			log.info("Exception when checking certificate", e);
		}
		
		return false;
	}
	
	public String getQrCodeUrl(String escnData) {
		String escnHexa = escnData.substring(0, 32);
		String escn = Card.getEscnWithDash(escnHexa);
		return escUidFactoryService.getQrCodeUrl(escn);
	}

	public Map<String, String> getCertSubjectName(String certAsHexa) {
		Map<String, String> subjectMap = new HashMap<String, String>();
		try {
			byte[] certBytes;
			certBytes = Hex.decodeHex(certAsHexa.toCharArray());
			ByteArrayInputStream in = new ByteArrayInputStream(certBytes);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate x509cert = (X509Certificate)certFactory.generateCertificate(in);
			String dn = x509cert.getSubjectX500Principal().getName();
			LdapName ldapDN = new LdapName(dn);
			for(Rdn rdn: ldapDN.getRdns()) {
				// we don't want no display string attrs, like 1.2.840.113549.1.9.1 for example
				if("1.2.840.113549.1.9.1".equals(rdn.getType())) {
					 //String mailBase64 = rdn.getValue();
					 byte[] mailBytes = (byte[])rdn.getValue();
					 String mail = new String(mailBytes);
					 // TODO : check what is 2 first bytes of "1.2.840.113549.1.9.1 ?
					 String mailOnly = mail.substring(2);
					 subjectMap.put("Mail", mailOnly);
				} else {
					subjectMap.put(rdn.getType(), new String(rdn.getValue().toString().getBytes("ISO-8859-1"), "utf-8"));
				}
			}
			log.debug(String.format("subjectMap from cert : %s", subjectMap));
		} catch (DecoderException|CertificateException|InvalidNameException|UnsupportedEncodingException e) {
			log.warn("Can't get cert subject name from this certificat " + certAsHexa, e);
		}
		return subjectMap;
	}
	

	PublicKey getPublicKeyFromCert(byte[] certBytes) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(certBytes);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate certificate = certFactory.generateCertificate(in);
		return certificate.getPublicKey();
	}

}
