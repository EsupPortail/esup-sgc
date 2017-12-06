package org.esupportail.sgc.domain;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(finders = { "findEsupNfcSgcJwsDevicesByEppnInitEquals" })
public class EsupNfcSgcJwsDevice {

    String eppnInit;

    String numeroId;
}
