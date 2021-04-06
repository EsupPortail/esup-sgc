package org.esupportail.sgc.services.esc;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.annotation.Resource;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * All signatures involved in the DUEINFO scheme use the ECDSA algorithm with SHA256 hash function and the P-256 curve.
 * 
 */
public class EscDeuInfoService  {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String pic;
	
	PrivateKey privateKey;

	byte[] publicKey;
	
	//byte[] publicEscCaRootKey;
	
	KeyStore trustStore;
	
	@Resource
	EscDeuInfoMetaService escDeuInfoMetaService;
	
	
	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}
	
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


	String sign(String escnData) throws NoSuchAlgorithmException, SignatureException, DecoderException, InvalidKeyException {
		Signature ecdsaSign;
		ecdsaSign = Signature.getInstance("SHA256withECDSA");
		ecdsaSign.initSign(privateKey);
		ecdsaSign.update(Hex.decodeHex(escnData.toCharArray()));
		byte[] signature = ecdsaSign.sign();
		return Hex.encodeHexString(signature);
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

	boolean checkSignature(String escnData, String signature) throws Exception {
		PublicKey pubKey = escDeuInfoMetaService.getPublicKeyFromCert(publicKey);
		return escDeuInfoMetaService.checkSignature(escnData, signature, pubKey);
	}

}
