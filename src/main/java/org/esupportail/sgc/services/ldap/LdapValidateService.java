package org.esupportail.sgc.services.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.esupportail.sgc.services.esc.ApiEscService;
import org.esupportail.sgc.tools.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapEntryIdentificationContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapEncoder;

public class LdapValidateService extends ValidateService {

	final static String CSN = "%csn%";
	
	final static String CSN_RETURN = "%reverse_csn%";

	final static String CSN_DEC = "%csn_dec%";

	final static String CSN_RETURN_DEC = "%reverse_csn_dec%";
	
	final static String SECONDARYID = "%secondary_id%";
	
	final static String EPPN = "%eppn%";

	final static String ENABLED_DATE = "%enabled_date%";

	final static String DUE_DATE = "%due_date%";

	final static String ETAT_DATE = "%etat_date%";

	final static String ETAT = "%etat%";
	
	final static String PHOTO = "%photo%";
	
	final static String ESCN = "%escn%";
	
	final static String ESI = "%esi%";
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private LdapTemplate ldapTemplate;
	
	private String peopleSearchFilter = "(eduPersonPrincipalName={0})";
	
	Map<String, List<String>> ldapCardIdsMappingMultiValues;
	
	Map<String, String> ldapCardIdsMappingValue;
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	public void setLdapCardIdsMappingMultiValues(Map<String, List<String>> ldapCardIdsMappingMultiValues) {
		this.ldapCardIdsMappingMultiValues = ldapCardIdsMappingMultiValues;
	}

	public void setLdapCardIdsMappingValue(Map<String, String> ldapCardIdsMappingValue) {
		this.ldapCardIdsMappingValue = ldapCardIdsMappingValue;
	}

	public void setPeopleSearchFilter(String peopleSearchFilter) {
		this.peopleSearchFilter = peopleSearchFilter;
	}

	@Autowired
	List<ApiEscService> apiEscServices;

	@Resource
	DateUtils dateUtils;

	@Override
	public void validateInternal(Card card) {
		try {
			Name dn = getDn(card.getEppn());
			if(dn != null) {
				DirContextOperations context = ldapTemplate.lookupContext(dn);
	
				if(ldapCardIdsMappingMultiValues!=null) {
					for(String ldapattr : ldapCardIdsMappingMultiValues.keySet()) {
						for(String ldapValueRef : ldapCardIdsMappingMultiValues.get(ldapattr)) {
							Object ldapValue = computeLdapValue(card, ldapValueRef);
							context.addAttributeValue(ldapattr, ldapValue, false);
							log.debug(String.format("Ldap Add : %s : %s -> %s", card.getEppn(), ldapattr, ldapValue));
						}
					} 
				}
				
				if(ldapCardIdsMappingValue!=null) {
					for(String ldapattr : ldapCardIdsMappingValue.keySet()) {
						String ldapValueRef = ldapCardIdsMappingValue.get(ldapattr);
						Object ldapValue = computeLdapValue(card, ldapValueRef);
						context.setAttributeValue(ldapattr, ldapValue);
						log.debug(String.format("Ldap Set : %s : %s -> %s", card.getEppn(), ldapattr, ldapValue));
					} 
				}
				try {
					ldapTemplate.modifyAttributes(context);
					log.info("Ldap Validation OK for " + card.getEppn());
				} catch(AttributeInUseException attrInUseEx) {
					log.trace("Attribut already exist");
				}
			} else {
				log.info("No entry for this user [" + card.getEppn() + "] in this ldap, we don't do anything for validation.");
			}
		} catch (InvalidNameException e) {
			throw new SgcRuntimeException("Pb retrieving " + card.getEppn() + " on ldap " + this.getBeanName(), e);
		}
	}


	@Override
	public void invalidateInternal(Card card) {
		try {
			Name dn = getDn(card.getEppn());
			
			if(dn != null) {
				DirContextAdapter context = (DirContextAdapter)ldapTemplate.lookup(dn);
	
				if(ldapCardIdsMappingMultiValues!=null) {
					for(String ldapattr : ldapCardIdsMappingMultiValues.keySet()) {
						for(String ldapValueRef : ldapCardIdsMappingMultiValues.get(ldapattr)) {
							Object ldapValue = computeLdapValue(card, ldapValueRef);
							context.removeAttributeValue(ldapattr, ldapValue);
							log.debug(String.format("Ldap Remove : %s : %s -> %s", card.getEppn(), ldapattr, ldapValue));
						}
					} 
				}
				
				if(ldapCardIdsMappingValue!=null) {
					for(String ldapattr : ldapCardIdsMappingValue.keySet()) {
						String ldapValueRef = ldapCardIdsMappingValue.get(ldapattr);
						Object ldapValue = computeLdapValue(card, ldapValueRef);
						context.removeAttributeValue(ldapattr, ldapValue);
						log.debug(String.format("Ldap Remove : %s : %s -> %s", card.getEppn(), ldapattr, ldapValue));
					} 
				}
	
				ldapTemplate.modifyAttributes(context);
				log.info("Ldap Invalidation OK for " + card.getEppn());
			} else {
				// if dn is null, the user is no more in ldap -> cards ids are already removed
				log.info("No entry for this user [" + card.getEppn() + "] in this ldap, we don't do anything for invalidation.");
			}
		} catch (InvalidNameException e) {
			throw new SgcRuntimeException("Pb retrieving " + card.getEppn() + " on ldap " + this.getBeanName(), e);
		}
	}
	

	protected Object computeLdapValue(Card card, String ldapValueRef) {
		
		log.trace("ldapValue before computing : " + ldapValueRef);
		
		Object ldapValue = null;
		ldapValueRef = ldapValueRef.replaceAll(CSN, card.getCsn());
		ldapValueRef = ldapValueRef.replaceAll(CSN_RETURN, card.getReverseCsn());
		ldapValueRef = ldapValueRef.replaceAll(CSN_DEC, toDecimal(card.getCsn()));
		ldapValueRef = ldapValueRef.replaceAll(CSN_RETURN_DEC, toDecimal(card.getReverseCsn()));
		ldapValueRef = ldapValueRef.replaceAll(EPPN, card.getEppn());
		ldapValueRef = ldapValueRef.replaceAll(ENABLED_DATE, dateUtils.getGeneralizedTime(card.getEnnabledDate()));
		ldapValueRef = ldapValueRef.replaceAll(DUE_DATE, dateUtils.getGeneralizedTime(card.getDueDate()));
		ldapValueRef = ldapValueRef.replaceAll(ETAT_DATE, dateUtils.getGeneralizedTime(card.getDateEtat()));
		ldapValueRef = ldapValueRef.replaceAll(ETAT, card.getEtat().name());
		if(card.getUser().getSecondaryId() != null) {
			ldapValueRef = ldapValueRef.replaceAll(SECONDARYID, card.getUser().getSecondaryId());
		}
		if(card.getEscnUid() != null && !card.getEscnUid().isEmpty()) {
			ldapValueRef = ldapValueRef.replaceAll(ESCN, card.getEscnUid());
		}
		if(ldapValueRef.contains(ESI)) {
			for(ApiEscService apiEscService : apiEscServices) {
				String esi = apiEscService.getEuropeanPersonIdentifier(card.getEppn());
				if(esi != null) {
					ldapValueRef = ldapValueRef.replaceAll(ESI, esi);
				}
			}
		}

		for(String appName : card.getDesfireIds().keySet()) {
			ldapValueRef = ldapValueRef.replaceAll("%" + appName + "%", card.getDesfireIds().get(appName));
		}
		
		if(PHOTO.equals(ldapValueRef)) {
			InputStream photoStream;
			try {
				photoStream = card.getPhotoFile().getBigFile().getBinaryFile().getBinaryStream();
				ldapValue = IOUtils.toByteArray(photoStream);
			} catch (SQLException | IOException e) {
				log.error("Can't get photo for ldap", e);
			}
		} else {	
			// nettoyage des %xxx% qui resteraient et ne seraient pas informés ... ->  ldapValue
			ldapValue = ldapValueRef.replaceAll("%.*%", "");
		}
		
		log.trace("ldapValue after computing : " + ldapValue);
		
		return ldapValue;
	}
	
	private Name getDn(String eppn) throws InvalidNameException {
		
		String formattedPeopleSearchFilter = computePeopleSearchFilter(new String[] {eppn, eppn.replaceAll("@.*", "")});

		List<LdapEntryIdentification> ldapEntries = ldapTemplate.search(
				"",
				formattedPeopleSearchFilter,
				new LdapEntryIdentificationContextMapper());
		
		if(!ldapEntries.isEmpty()) {
			LdapEntryIdentification ldapEntry = ldapEntries.get(0);		
			return ldapEntry.getRelativeName();
		} else {
			return null;
		}
	}
	
	String computePeopleSearchFilter(final Object[] params) {
	    // Escape the params acording to RFC2254
	    Object[] encodedParams = new String[params.length];
	
	    for (int i=0; i < params.length; i++) {
	        encodedParams[i] = LdapEncoder.filterEncode(params[i].toString());
	    }
	
	    String formattedFilter = MessageFormat.format(peopleSearchFilter, encodedParams);
	    log.debug("Using filter: " + formattedFilter);
	    
	    return formattedFilter;
	}

	String toDecimal(String hex) {
		if(hex == null) {
			return "";
		}
		if(hex.isEmpty()) {
			return "";
		}
		return Long.toString(Long.parseLong(hex, 16));
	}

}
