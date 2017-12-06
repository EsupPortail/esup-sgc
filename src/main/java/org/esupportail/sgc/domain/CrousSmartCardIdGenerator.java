package org.esupportail.sgc.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooDbManaged(automaticallyDelete = true)
@RooJpaActiveRecord
@SequenceGenerator(name="crous_smart_card_generator", sequenceName="crous_smart_card_sequence", allocationSize=1)
public class CrousSmartCardIdGenerator {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="crous_smart_card_generator")
	@Column(name = "id")
	private Long id;

}
