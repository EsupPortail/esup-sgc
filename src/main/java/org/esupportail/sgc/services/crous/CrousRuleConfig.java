package org.esupportail.sgc.services.crous;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import java.util.Date;

@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
@RooJavaBean
public class CrousRuleConfig {

	String rne;
	
	String numeroCrous;

	Long priority = 0L;

}
