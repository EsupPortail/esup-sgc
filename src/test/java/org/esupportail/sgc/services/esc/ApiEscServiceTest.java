package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.dao.EscPersonDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.EscCard;
import org.esupportail.sgc.domain.EscPerson;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class ApiEscServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	List<ApiEscService> apiEscServices;
	
	@Resource
	EscPersonDaoService escPersonDaoService;
	
	@Test
	public void getEscrPersonTest() {
		EscPerson escPersonFromDb = escPersonDaoService.findOneEscPerson4test();
		assumeTrue(escPersonFromDb!=null);
		log.info("escPersonFromDb : " + escPersonFromDb);
		for(ApiEscService apiEscService : apiEscServices) {
			EscPerson escPersonFromEsc = apiEscService.getEscPerson(escPersonFromDb.getEppn());
			log.info("escPersonFromEscr : " + escPersonFromEsc);
		}
	}

	@Test
	public void getCardTypeTest() {
		Card c = new Card();
		c.setEncodedDate(LocalDateTime.now());
		for(ApiEscService apiEscService : apiEscServices) {
			EscCard.CardType cardType = apiEscService.getCardType(c);
			log.info("cardType : " + cardType);
		}
	}

}

