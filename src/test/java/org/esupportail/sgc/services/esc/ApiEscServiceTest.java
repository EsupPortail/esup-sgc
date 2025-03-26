package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.dao.EscPersonDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.EscCard;
import org.esupportail.sgc.domain.EscPerson;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class ApiEscServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ApiEscService apiEscService;
	
	@Resource
	EscPersonDaoService escPersonDaoService;
	
	@Test
	public void getEscrPersonTest() {
		EscPerson escPersonFromDb = escPersonDaoService.findOneEscPerson4test();
		Assume.assumeTrue(escPersonFromDb!=null);
		log.info("escPersonFromDb : " + escPersonFromDb);
		EscPerson escPersonFromEsc = apiEscService.getEscPerson(escPersonFromDb.getEppn());
		log.info("escPersonFromEscr : " + escPersonFromEsc);
	}

	@Test
	public void getCardTypeTest() {
		Card c = new Card();
		c.setEncodedDate(new Date());
		EscCard.CardType cardType = apiEscService.getCardType(c);
		log.info("cardType : " + cardType);
	}

}

