package org.esupportail.sgc.services.esc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.annotation.Resource;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.domain.Card;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class EscDeuInfoServiceTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Lazy
	@Resource
	EscDeuInfoService escDeuInfoService;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;
	
	 @Before
	 public void beforeMethod() throws Exception {
		 try {
			 Assume.assumeTrue(escDeuInfoService.getPublicKeyAsHexa() != null);
		 } catch(NoSuchBeanDefinitionException ex) {
			 log.warn("escDeuInfoService is not defined");
			 Assume.assumeTrue(false);
		 }
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
		Assume.assumeTrue(c !=null);
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoService.sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
	}

	@Test
	public void checkSignatureTest() throws Exception {
		Card c = getCardithEscnFromDb();
		Assume.assumeTrue(c !=null);
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoService.sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
		boolean signedIsOK = escDeuInfoService.checkSignature(escn + uid, signature);
		assertTrue(signedIsOK);
	}

	
	@Test
	public void checkSignatureTestFailed() throws Exception {
		Card c = getCardithEscnFromDb();
		Assume.assumeTrue(c !=null);
		String escn = c.getEscnUidAsHexa();
		String uid = c.getCsn();
		String signature = escDeuInfoService.sign(escn + uid);
		log.info(String.format("Sign of %s : %s", escn + uid, signature));
		boolean signedIsOK = escDeuInfoService.check(escn + uid, signature, "dummy");
		assertTrue(!signedIsOK);
	}
	
	@Test
	public void escnHexaDashedTest() throws Exception {
		String escnDashed = Card.getEscnWithDash("452e2b0097fd2037b8b0001989465982");
		assertEquals(escnDashed, "452e2b00-97fd-2037-b8b0-001989465982");
	}
	
	@Test 
	public void getPublicKeyAsHexaTest() throws Exception {
		String publicKeyAsHexa = escDeuInfoService.getPublicKeyAsHexa();
		log.info(String.format("publicKeyAsHexa [%s] : %s", publicKeyAsHexa.length(), publicKeyAsHexa));
		//assertEquals(540, publicKeyAsHexa.length());
	}
	
	@Test 
	public void getCertInfoTest() throws Exception {
		Map<String, String> certInfo = escDeuInfoService.getCertSubjectName(escDeuInfoService.getPublicKeyAsHexa());
		log.info(String.format("Cert info : %s", certInfo));
		//assertEquals(540, publicKeyAsHexa.length());
	}
	
}

