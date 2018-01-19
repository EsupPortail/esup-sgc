package org.esupportail.sgc.domain;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "id", "version", "hibernateLazyInitializer", "handler", "crousSmartCardIdGenerator"})
@RooJpaActiveRecord(finders = { "findCrousSmartCardsByUidEquals" })
public class CrousSmartCard {
    
    Long idTransmitter;

    Long idMapping;

    @Column(unique = true)
    Long idZdc;

    Date zdcCreationDate;

    String pixSs;

    String pixNn;

    String appl;

    @Column(unique = true, nullable=false)
    String uid;

    String rid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getZdcCreationDate() {
        return zdcCreationDate;
    }

    public static CrousSmartCard findCrousSmartCard(String uid) {
    	CrousSmartCard smartCard = null;
    	List<CrousSmartCard> smartCards = CrousSmartCard.findCrousSmartCardsByUidEquals(uid).getResultList();
    	if(!smartCards.isEmpty()) {
    		smartCard = smartCards.get(0);
    	}
        return smartCard;
    }
}
