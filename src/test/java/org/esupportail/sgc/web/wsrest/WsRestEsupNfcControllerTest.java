package org.esupportail.sgc.web.wsrest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import jakarta.annotation.Resource;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml", "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml"})
@WebAppConfiguration
public class WsRestEsupNfcControllerTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	WsRestEsupNfcController wsRestEsupNfcControllerTest;
	
	@Resource
	EsupSgcTestUtilsService esupSgcTestUtilsService;
    @Autowired
    private CardDaoService cardDaoService;

    @Test
	public void getLocationsTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocations(eppn);
		log.info(String.format("Locations for %s : %s", eppn, locations));
	}
	
	@Test
	public void getLocationsLivreurTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocationsLivreur(eppn);
		log.info(String.format("LocationsLivreur for %s : %s", eppn, locations));
	}
		
	@Test
	public void getLocationsSearchTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocationsSearch(eppn);
		log.info(String.format("LocationsSearch for %s : %s", eppn, locations));
	}
		
	@Test
	public void getLocationsSecondaryIdTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocationsSecondaryId(eppn);
		log.info(String.format("LocationsSecondaryId for %s : %s", eppn, locations));
	}
	
	@Test
	public void getLocationsUpdaterTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocationsUpdater(eppn);
		log.info(String.format("LocationsUpdater for %s : %s", eppn, locations));
	}
	
	@Test
	public void getLocationsVersoTest() {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		List<String> locations = wsRestEsupNfcControllerTest.getLocationsVerso(eppn);
		log.info(String.format("LocationsVerso for %s : %s", eppn, locations));
	}
	
	@Test
	public void getVersoTextTest() throws IOException, ParseException {
		String eppn = esupSgcTestUtilsService.getEppnFromConfig();
		assumeTrue(eppn != null);
		String csn = getCsn(eppn);
		assumeTrue(csn != null);
		List<String> versoText = wsRestEsupNfcControllerTest.getVersoText(csn, new MockHttpServletRequest());
		log.info(String.format("getVersoText for %s (card %s) : %s", eppn, csn, versoText));
	}

	private String getCsn(String eppn) {
		for(Card card : cardDaoService.findCardsByEppnEquals(eppn).getResultList()) {
			if(card.getCsn() != null && !card.getCsn().isEmpty()) {
				return card.getCsn();
			}
		}
		return null;
	}
	
}
