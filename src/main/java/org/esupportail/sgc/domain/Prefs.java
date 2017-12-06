package org.esupportail.sgc.domain;
import java.util.Date;
import javax.persistence.Column;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findPrefsesByEppnEqualsAndKeyEquals" })
public class Prefs {

    @Column
    private String eppn;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateModification;

    @Column(columnDefinition="TEXT")
    private String value;

    @Column
    private String key;
}
