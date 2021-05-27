package org.esupportail.sgc.services.esc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.Map;

import javax.annotation.Resource;

import org.bouncycastle.util.encoders.Hex;
import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.domain.Card;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class EscDeuInfoServiceTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	EscDeuInfoMetaService escDeuInfoMetaService;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;
	
	@Value("classpath:META-INF/security/esc/test-ca.intermediate.cert.pem")
	org.springframework.core.io.Resource resourceCertAsHexa;
	
	@Value("classpath:META-INF/security/esc/test-ca-chain.cert.pem")
	org.springframework.core.io.Resource resourceChainCertAsHexa;
	
	 @Before
	 public void beforeMethod() throws Exception {
		 Card c = getCardithEscnFromDb();
		Assume.assumeTrue(c != null);
		Assume.assumeTrue(escDeuInfoMetaService.getPublicKeyAsHexa(c) != null);
	 }	
	
	Card getCardithEscnFromDb() {
		try {
			return Card.findCardsWithEscnAndCsn().setMaxResults(1).getSingleResult();
		} catch(Exception e) {
			log.warn("No Card with ESCN in DB, we can't make many tests here : " + e.getMessage());
		}
		return null;
	}

	@Test
	public void signTest() throws Exception {
		Card c = getCardithEscnFromDb();
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
	}

	@Test
	public void checkSignatureTest() throws Exception {
		Card c = getCardithEscnFromDb();
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
		boolean signedIsOK = escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).checkSignature(escn + uid, signature);
		assertTrue(signedIsOK);
	}

	
	@Test
	public void checkSignatureTestFailed() throws Exception {
		Card c = getCardithEscnFromDb();
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
		boolean signedIsOK = escDeuInfoMetaService.check(escn + uid, signature, "dummy", true);
		assertTrue(!signedIsOK);
	}
	
	@Test
	public void escnHexaDashedTest() throws Exception {
		String escnDashed = Card.getEscnWithDash("452e2b0097fd2037b8b0001989465982");
		assertEquals(escnDashed, "452e2b00-97fd-2037-b8b0-001989465982");
	}
	
	@Test 
	public void getPublicKeyAsHexaTest() throws Exception {
		Card c = getCardithEscnFromDb();
		String publicKeyAsHexa = escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).getPublicKeyAsHexa();
		log.info(String.format("publicKeyAsHexa [%s] : %s", publicKeyAsHexa.length(), publicKeyAsHexa));
		//assertEquals(540, publicKeyAsHexa.length());
	}
	
	@Test 
	public void getCertInfoTest() throws Exception {
		Card c = getCardithEscnFromDb();
		Map<String, String> certInfo = escDeuInfoMetaService.getCertSubjectName(escDeuInfoMetaService.getEscDeuInfoService(c.getUser().getPic()).getPublicKeyAsHexa());
		log.info(String.format("Cert info : %s", certInfo));
		//assertEquals(540, publicKeyAsHexa.length());
	}
	
	@Test 
	public void checkCertTest() throws Exception {
		byte[] chainCertAsHexaAsBytesArray =Files.readAllBytes(resourceChainCertAsHexa.getFile().toPath());
		byte[] certAsHexaAsBytesArray = Files.readAllBytes(resourceCertAsHexa.getFile().toPath());
		String chainCertAsHexa = Hex.toHexString(chainCertAsHexaAsBytesArray);
		String certAsHexa = Hex.toHexString(certAsHexaAsBytesArray);
		boolean certAndChainIsOk = escDeuInfoMetaService.checkCert(certAsHexa, chainCertAsHexa);
		assertTrue(certAndChainIsOk);
	}
	
}

