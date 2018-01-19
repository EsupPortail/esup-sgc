package org.esupportail.sgc.services.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.ValidateService;
import org.esupportail.sgc.services.cardid.CardIdsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapEntryIdentificationContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapEncoder;

public class LdapValidateService extends ValidateService {

	private String CSN = "%csn%";
	
	private String CSN_RETURN = "%reverse_csn%";
	
	private String PHOTO = "%photo%";
	
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

	@Override
	public void validate(Card card) {
		try {
			Name dn = getDn(card.getEppn());
			if(dn != null) {
				DirContextOperations context = ldapTemplate.lookupContext(dn);
	
				if(ldapCardIdsMappingMultiValues!=null) {
					for(String ldapattr : ldapCardIdsMappingMultiValues.keySet()) {
						for(String ldapValueRef : ldapCardIdsMappingMultiValues.get(ldapattr)) {
							Object ldapValue = computeLdapValue(card, ldapValueRef);
							context.addAttributeValue(ldapattr, ldapValue, false);
						}
					} 
				}
				
				if(ldapCardIdsMappingValue!=null) {
					for(String ldapattr : ldapCardIdsMappingValue.keySet()) {
						String ldapValueRef = ldapCardIdsMappingValue.get(ldapattr);
						Object ldapValue = computeLdapValue(card, ldapValueRef);
						context.setAttributeValue(ldapattr, ldapValue);
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
	public void invalidate(Card card) {
		try {
			Name dn = getDn(card.getEppn());
			
			if(dn != null) {
				DirContextAdapter context = (DirContextAdapter)ldapTemplate.lookup(dn);
	
				if(ldapCardIdsMappingMultiValues!=null) {
					for(String ldapattr : ldapCardIdsMappingMultiValues.keySet()) {
						for(String ldapValueRef : ldapCardIdsMappingMultiValues.get(ldapattr)) {
							Object ldapValue = computeLdapValue(card, ldapValueRef);
							context.removeAttributeValue(ldapattr, ldapValue);
						}
					} 
				}
				
				if(ldapCardIdsMappingValue!=null) {
					for(String ldapattr : ldapCardIdsMappingValue.keySet()) {
						String ldapValueRef = ldapCardIdsMappingValue.get(ldapattr);
						Object ldapValue = computeLdapValue(card, ldapValueRef);
						context.removeAttributeValue(ldapattr, ldapValue);
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
	

	private Object computeLdapValue(Card card, String ldapValueRef) {
		Object ldapValue = null;
		ldapValueRef = ldapValueRef.replaceAll(CSN, card.getCsn());
		ldapValueRef = ldapValueRef.replaceAll(CSN_RETURN, card.getReverseCsn());
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
	
}
