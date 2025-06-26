package org.esupportail.sgc.services.ldap;

import org.esupportail.sgc.EsupSgcTestUtilsService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.tools.DateUtils;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
@Transactional
public class LdapValidateServiceTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    EsupSgcTestUtilsService esupSgcTestUtilsService;

    @Resource
    LdapValidateService ldapValidateService;

    @Resource
    DateUtils dateUtils;

    static String ldapValueWithCsn2replace = "{CSN}:" + LdapValidateService.CSN;

    static String ldapValueWithCsnReturn2replace = "{CSN_RETURN}:" + LdapValidateService.CSN_RETURN;

    static String ldapValueWithCsnDec2replace = "{CSN_DEC}:" + LdapValidateService.CSN_DEC;

    static String ldapValueWithEppn2replace = "{EPPN}:" + LdapValidateService.EPPN;

    static String ldapValueWithEnabledDate2replace = "{ENABLED_DATE}:" + LdapValidateService.ENABLED_DATE;

    static String ldapValueWithEtatDate2replace = "{ETAT_DATE}:" + LdapValidateService.ETAT_DATE;

    static String ldapValueWithEtat2replace = "{ETAT}:" + LdapValidateService.ETAT;


    @Test
    public void testComputeLdapValueWithCsn2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null, "no enabled card in db");
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithCsn2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{CSN}:" + enabledCard.getCsn();
        assumeTrue(ldapValueStrExpected.equals(ldapValueStr), () -> "ldapValueStrExpected : " + ldapValueStrExpected + " but ldapValueStr : " + ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithCsnReturn2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null, "no enabled card in db");
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithCsnReturn2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{CSN_RETURN}:" + enabledCard.getReverseCsn();
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithCsnDec2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null);
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithCsnDec2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{CSN_DEC}:" + ldapValidateService.toDecimal(enabledCard.getCsn());
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithEppn2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null);
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithEppn2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{EPPN}:" + enabledCard.getEppn();
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithEnabledDate2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null);
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithEnabledDate2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{ENABLED_DATE}:" + dateUtils.getGeneralizedTime(enabledCard.getEnnabledDate());
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithEtatDate2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null);
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithEtatDate2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{ETAT_DATE}:" + dateUtils.getGeneralizedTime(enabledCard.getDateEtat());
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

    @Test
    public void testComputeLdapValueWithEtat2replace() {
        Card enabledCard = esupSgcTestUtilsService.getEncodedCardFromDb();
        assumeTrue(enabledCard != null);
        Object ldapValue = ldapValidateService.computeLdapValue(enabledCard, ldapValueWithEtat2replace);
        String ldapValueStr = ldapValue.toString();
        log.info("ldapValueStr : " + ldapValueStr);
        String ldapValueStrExpected = "{ETAT}:" + enabledCard.getEtat();
        assertEquals(ldapValueStrExpected, ldapValueStr);
    }

}
