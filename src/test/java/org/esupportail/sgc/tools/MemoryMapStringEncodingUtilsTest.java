package org.esupportail.sgc.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class MemoryMapStringEncodingUtilsTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String testString = "Voici un paramètre Web avec quelques caractères (ou non ?) assez ennuyeux = > :-) && ^^ - à voir ...";
	
    @Autowired(required = false)
    MemoryMapStringEncodingUtils urlEncodingUtils;
    
    @Test
    public void testEncodeDescodeDir() {
    	log.info("Encode string : " + testString);
    	String stringEncoded = urlEncodingUtils.encodeString(testString);
    	log.info("string encoded : " + stringEncoded);
    	String stringDecoded = urlEncodingUtils.decodeString(stringEncoded);
    	log.info("string decoded : " + stringDecoded);
    	assertEquals(testString, stringDecoded);
    }  
}

