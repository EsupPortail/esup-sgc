package org.esupportail.sgc.domain;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.web.multipart.MultipartFile;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "TemplateCard", finders = { "findTemplateCardsByKeyEquals" })
public class TemplateCard {

    @Column
    @NotEmpty
    private String key;

    @Column
    private String name;
    
    @Column
    private int numVersion = 0;

    @Column(columnDefinition = "TEXT")
    private String cssStyle;
    
    @Column(columnDefinition = "TEXT")
    private String cssMobileStyle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String modificateur;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dateModification;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PhotoFile photoFileLogo = new PhotoFile();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PhotoFile photoFileMasque = new PhotoFile();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PhotoFile photoFileQrCode = new PhotoFile();

    @Transient
    private MultipartFile masque;

    @Transient
    private MultipartFile logo;

    @Transient
    private MultipartFile qrCode;
}
