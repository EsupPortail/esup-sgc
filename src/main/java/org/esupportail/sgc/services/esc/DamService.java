package org.esupportail.sgc.services.esc;

import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Random;

@Service
public class DamService {

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    @Resource
    CardDaoService cardDaoService;

    @Transactional
    public String createDamDiversBaseKey(String csn) {
        Card card = cardDaoService.findCardByCsn(csn);
        String diversDamBaseKey = cardDaoService.findCardByCsn(csn).getDiversDamBaseKey();
        if (diversDamBaseKey == null) {
            Random rand = new Random();
            byte[] diversDamBaseKeyRandom = new byte[16];
            for (int i = 0 ; i < diversDamBaseKeyRandom.length; i++) {
                diversDamBaseKeyRandom[i] = (byte) rand.nextInt(0xFF);
            }
            diversDamBaseKey = byteArrayToHexString(diversDamBaseKeyRandom);
            card.setDiversDamBaseKey(diversDamBaseKey);
        }
        return diversDamBaseKey;
    }

    public String getDamDiversBaseKey(String csn) {
        Card card = cardDaoService.findCardByCsn(csn);
        return card.getDiversDamBaseKey();
    }


    @Transactional
    public void resetDamDiversBaseKey(String csn) {
        Card card = cardDaoService.findCardByCsn(csn);
        card.setDiversDamBaseKey(null);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}
