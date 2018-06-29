package org.esupportail.sgc.domain;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.web.multipart.MultipartFile;

@RooJavaBean
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord(versionField = "", table = "TemplateCard", finders = { "findTemplateCardsByKeyEquals" })
public class TemplateCard {

    @Column
    @NotEmpty
    private String key;

    @Column
    private String name = "";
    
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
    private Boolean codeBarres= false;

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
       
    public boolean isCodeBarres() {
        return this.codeBarres!=null && this.codeBarres;
    }
   
    public static List<Object> countTemplateCardByNameVersion() {
        String sql = "SELECT CONCAT(name, ' / V', num_version) as nom, count(*) from card,template_card where card.template_card= template_card.id GROUP BY nom";

        EntityManager em = TemplateCard.entityManager();
        Query q = em.createNativeQuery(sql);

        return q.getResultList();
    }

	public String toString() {
		return String.format("%s / V%d", this.getName(), this.getNumVersion());
	}
   
}
