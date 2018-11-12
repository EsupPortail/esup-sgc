package org.esupportail.sgc.services.crous;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EsistCrousService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	List<String> esistFilePathes = new ArrayList<String>();
	
	String esistFileOverridePath;

	List<CrousRule> rules = new ArrayList<CrousRule>();
	
	@Resource
	AppliConfigService appliConfigService;

	public void setEsistFiles(List<String> esistFilePathes) {
		this.esistFilePathes = esistFilePathes;

	}
	
	@PostConstruct
	public void parseEsistFiles(){

		for(String esistFilePath : esistFilePathes) {
			try {
				
				ClassPathResource esistResource = new ClassPathResource(esistFilePath);
				
				File esistXmlFile = esistResource.getFile();
				
				Map<String, String> employeursRne = new HashMap<String, String>();
	
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document= builder.parse(esistXmlFile);
				Element root = document.getDocumentElement();
	
				if(root.getElementsByTagName("RneEmployeur").getLength()>0) {
					NodeList rneEmployeurRows = root.getElementsByTagName("RneEmployeur").item(0).getChildNodes();
					final int nbRneEmployeurRows = rneEmployeurRows.getLength();
					for (int i = 0; i<nbRneEmployeurRows; i++) {
						if(rneEmployeurRows.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element row = (Element) rneEmployeurRows.item(i);
							String rne = row.getElementsByTagName("rne").item(0).getTextContent();
							String employeur = row.getElementsByTagName("employeur").item(0).getTextContent();
							employeursRne.put(employeur, rne);
						}
					}
				}
	
				NodeList rulesRows = root.getElementsByTagName("EmployeurStatutIndiceSocieteTarif").item(0).getChildNodes();
				final int nbRulesRows = rulesRows.getLength();
				for (int i = 0; i<nbRulesRows; i++) {
					if(rulesRows.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element row = (Element) rulesRows.item(i);
						CrousRule rule = new CrousRule();
						if(row.getElementsByTagName("codeemployeur").getLength()>0) {
							String codeemployeur = row.getElementsByTagName("codeemployeur").item(0).getTextContent();
							rule.setRne(employeursRne.get(codeemployeur));
						}
						if(row.getElementsByTagName("referencestatut").getLength()>0) {
							String referencestatut = row.getElementsByTagName("referencestatut").item(0).getTextContent();
							rule.setReferenceStatus(referencestatut);
						}
						if(row.getElementsByTagName("indicemin").getLength()>0) {
							String indicemin = row.getElementsByTagName("indicemin").item(0).getTextContent();
							rule.setIndiceMin(Long.valueOf(indicemin));
						}
						if(row.getElementsByTagName("indicemax").getLength()>0) {
							String indicemax = row.getElementsByTagName("indicemax").item(0).getTextContent();
							rule.setIndiceMax(Long.valueOf(indicemax));
						}
						String codesociete = row.getElementsByTagName("codesociete").item(0).getTextContent();
						String codetarif = row.getElementsByTagName("codetarif").item(0).getTextContent();
						rule.setCodeSociete(Long.valueOf(codesociete));
						rule.setCodeTarif(Long.valueOf(codetarif));
						rules.add(rule);
					}
				}
			} catch (Exception e) {
				throw new SgcRuntimeException("Error retrieving ESIST File", e);
			}
			log.info("Esist file parsed : " + rules);
		}
	}

	public List<Long> compute(User user) {
		Long defaultCnousIdCompagnyRate  = appliConfigService.getDefaultCnousIdCompagnyRate();
		Long defaultCnousIdRate  = appliConfigService.getDefaultCnousIdRate();
		List<Long> idCompagnyRateAndIdRate = Arrays.asList(new Long[] {defaultCnousIdCompagnyRate, defaultCnousIdRate});
		CrousRule matchRule = null;
		Long indice = user.getIndice();
		String referenceStatut = user.getCnousReferenceStatut().name();
		String rneEtablissement = user.getRneEtablissement();
		for(CrousRule rule : rules) {
			if((rule.getReferenceStatus() == null || referenceStatut.equalsIgnoreCase(rule.getReferenceStatus())) &&
					(rule.getRne() == null || rneEtablissement.equalsIgnoreCase(rule.getRne())) && 
					(rule.getIndiceMin() == null || indice >= rule.getIndiceMin()) && 
					(rule.getIndiceMax() == null || indice <= rule.getIndiceMax())) {
				matchRule = rule;
				break;
			}
		}
		if(matchRule == null) {
			log.trace("No crous rule matches for this user : " + user.getEppn() + " -> psg");
		} else {
			idCompagnyRateAndIdRate = Arrays.asList(new Long[] {matchRule.getCodeSociete(), matchRule.getCodeTarif()});
		}
		return idCompagnyRateAndIdRate;
	}

}
