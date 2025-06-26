package org.esupportail.sgc.services.paybox;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HashService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String ALGO_HSAH = "SHA512";

	SecretKeySpec secretKey;


	public String getHash() {
		return ALGO_HSAH;
	}

	public void setHmacKey(String hmacKey) {
		secretKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(hmacKey), "HmacSHA512" );		
	}

	public String getHMac(String input) {
		try {
			Mac mac = Mac.getInstance("HmacSHA512");
			mac.init(secretKey);
			final byte[] macData = mac.doFinal(input.getBytes());
			byte[] hex = new Hex().encode(macData);
			String hmac = new String(hex, "ISO-8859-1").toUpperCase();
			log.debug(input);
			log.debug(hmac);
			return hmac;
		} catch (Exception e) {
			log.error("Error during encoding data ...");
			throw new RuntimeException(e);
		}
	}


}
