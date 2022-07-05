package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class EsistCrousService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;

	@Resource
	CrousService crousService;

	public List<Long> compute(User user) {
		if(crousService.isEnabled() && CrousRule.countCrousRules()==0) {
			throw new SgcRuntimeException("L'usage de l'API CROUS est activé mais aucune règle tarifaire n'a été configurée, il faut qu'un administrateur les configure via l'IHM depuis le menu Admin < Tarifs CROUS", null);
		}
		Long defaultCnousIdCompagnyRate  = appliConfigService.getDefaultCnousIdCompagnyRate();
		Long defaultCnousIdRate  = appliConfigService.getDefaultCnousIdRate();
		List<Long> idCompagnyRateAndIdRate = Arrays.asList(new Long[] {defaultCnousIdCompagnyRate, defaultCnousIdRate});
		CrousRule matchRule = null;
		Long indice = user.getIndice();
		String referenceStatut = user.getCnousReferenceStatut().name();
		String rneEtablissement = user.getRneEtablissement();
		for(CrousRule rule : CrousRule.findAllCrousRules("priority", "desc")) {
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

	@Transactional
	public void updateCrousRules() {
		for(CrousRuleConfig crousRuleConfig : CrousRuleConfig.findAllCrousRuleConfigs()) {
			List<CrousRule> crousRulesFromApi = crousService.getTarifRules(crousRuleConfig.getNumeroCrous(), crousRuleConfig.getRne());
			if(crousRulesFromApi.isEmpty()) {
				log.error(String.format("No CROUS Rules from API CROUS for %s / %s, we don't delete crous rules saved in database (if any) - please delete crousRuleConfig %s if needed",
						crousRuleConfig.getNumeroCrous(), crousRuleConfig.getRne(), crousRuleConfig.getId()));
				continue;
			}
			for(CrousRule crousRule : CrousRule.findAllCrousRules(crousRuleConfig)) {
				crousRule.remove();
			}
			for (CrousRule crousRule : crousRulesFromApi) {
				crousRule.setCrousRuleConfig(crousRuleConfig);
				crousRule.setPriority(crousRuleConfig.getPriority());
				crousRule.setUpdateDate(new Date());
				crousRule.persist();
			}
		}
	}

	@Transactional
	public void deleteCrousRuleConfig(Long id) {
		CrousRuleConfig crousRuleConfig = CrousRuleConfig.findCrousRuleConfig(id);
		for(CrousRule crousRule : CrousRule.findAllCrousRules(crousRuleConfig)) {
			crousRule.remove();
		}
		crousRuleConfig.remove();
	}
}
