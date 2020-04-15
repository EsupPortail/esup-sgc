package org.esupportail.sgc.services.esc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

import javax.annotation.Resource;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;

/**
 * All signatures involved in the DUEINFO scheme use the ECDSA algorithm with SHA256 hash function and the P-256 curve.
 * 
 */
public class EscDeuInfoService  {

	private final Logger log = LoggerFactory.getLogger(getClass());

	PrivateKey privateKey;

	byte[] publicKey;
	
	//byte[] publicEscCaRootKey;
	
	KeyStore trustStore;
	
	@Resource
	ApiEscrService apiEscrService;
	
	@Resource
	EscUidFactoryService escUidFactoryService;
	
	/*
	public void setDeuInfoRootCa(String escCertRootFile) throws Exception {
		File fEscCertRoot = ResourceUtils.getFile(escCertRootFile);
		publicEscCaRootKey = IOUtils.toByteArray(FileUtils.openInputStream(fEscCertRoot));
	} */

	public void setDeuInfoPublicKey(String certFile) throws Exception {
		File fCertFile = ResourceUtils.getFile(certFile);
		publicKey = IOUtils.toByteArray(FileUtils.openInputStream(fCertFile));
	}

	/**
	 * WARNING : private key must be PKCS#8
	 * -> convert if needed : 
	 *    openssl pkcs8 -topk8 -inform der -outform der -in ca.key-init.der -out ca.key-ok.der -nocrypt
	 */
	public void setDeuInfoPrivateKey(String keyFile) throws Exception {
		File fKeyFile = ResourceUtils.getFile(keyFile);
		byte[] privateKeyBytes = FileUtils.readFileToByteArray(fKeyFile);
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory kf = KeyFactory.getInstance("EC");
		privateKey = kf.generatePrivate(keySpecPKCS8);
	}

	PublicKey getPublicKeyFromCert(byte[] certBytes) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(certBytes);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Certificate certificate = certFactory.generateCertificate(in);
		return certificate.getPublicKey();
	}

	String sign(String escnData) throws NoSuchAlgorithmException, SignatureException, DecoderException, InvalidKeyException {
		Signature ecdsaSign;
		ecdsaSign = Signature.getInstance("SHA256withECDSA");
		ecdsaSign.initSign(privateKey);
		ecdsaSign.update(Hex.decodeHex(escnData.toCharArray()));
		byte[] signature = ecdsaSign.sign();
		return Hex.encodeHexString(signature);
	}

	boolean checkSignature(String escnData, String signature, PublicKey pubKey) throws Exception {
		Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
		ecdsaVerify.initVerify(pubKey);
		ecdsaVerify.update(Hex.decodeHex(escnData.toCharArray()));
		boolean result = ecdsaVerify.verify(Hex.decodeHex(signature.toCharArray()));
		return result;		
	}

	boolean checkSignature(String escnData, String signature) throws Exception {
		PublicKey pubKey = getPublicKeyFromCert(publicKey);
		return checkSignature(escnData, signature, pubKey);
	}

	public boolean check(String escnData, String signature, String certAsHexa) {
		try {
			byte[] certBytes = Hex.decodeHex(certAsHexa.toCharArray());
			PublicKey pubKey = getPublicKeyFromCert(certBytes);
			if(checkSignature(escnData, signature, pubKey)) {
				log.debug("Signature OK");
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

	public String getDeuInfoSignature(Card card) {
		try {
			String escnData = getDeuInfoEscnUid(card);
			return sign(escnData);
		} catch(Exception e) {
			throw new SgcRuntimeException(String.format("Exception during signing DeuInfo data of card %s", card.getCsn()), e);
		}
	}

	public String getDeuInfoEscnUid(Card card) {
		String escn = card.getEscnUidAsHexa();
		String uid = card.getCsn();
		return escn + uid;
	}

	public String getPublicKeyAsHexa() throws Exception {
		return Hex.encodeHexString(publicKey);
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
			Collection<Certificate> certificatesChain = (Collection<Certificate>) certFactory.generateCertificates(chainIn);

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
			log.info("Exception when checking signature", e);
		}
		
		return false;
	}
	
	public String getQrCodeUrl(String escnData) {
		String escnHexa = escnData.substring(0, 32);
		String escn = Card.getEscnWithDash(escnHexa);
		return escUidFactoryService.getQrCodeUrl(escn);
	}

}
