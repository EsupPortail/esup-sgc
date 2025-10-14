package org.esupportail.sgc.web.admin;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousRule;
import org.esupportail.sgc.services.crous.CrousRuleConfigDaoService;
import org.esupportail.sgc.services.crous.CrousRuleDaoService;
import org.esupportail.sgc.services.crous.EsistCrousService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/admin/crousrules")
@Controller
public class CrousRuleController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	EsistCrousService esistCrousService;

	@Resource
	AppliConfigService appliConfigService;

    @Resource
    CrousRuleDaoService crousRuleDaoService;

    @Resource
    CrousRuleConfigDaoService crousRuleConfigDaoService;

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crousrules";
	}

	@RequestMapping(value="/updateCrousRules", method = RequestMethod.POST, produces = "text/html")
	public String updateCrousRules() {
		esistCrousService.updateCrousRules();
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(produces = "text/html")
	public String list(Model uiModel) {
		uiModel.addAttribute("crousruleconfigs", crousRuleConfigDaoService.findAllCrousRuleConfigs());
		uiModel.addAttribute("crousrulesApi", crousRuleDaoService.findAllCrousRulesApi());
		uiModel.addAttribute("crousrulesCustom", crousRuleDaoService.findAllCrousRulesCustom());
		uiModel.addAttribute("defaultCnousIdCompagnyRate", appliConfigService.getDefaultCnousIdCompagnyRate());
		uiModel.addAttribute("defaultCnousIdRate", appliConfigService.getDefaultCnousIdRate());
        return "templates/admin/crousrules/list";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String create(@Valid CrousRule crousRule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, crousRule);
			return "templates/admin/crousrules/create";
		}
		uiModel.asMap().clear();
		crousRule.setUpdateDate(LocalDateTime.now());
		crousRuleDaoService.persist(crousRule);
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
	public String update(@Valid CrousRule crousRule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, crousRule);
			return "templates/admin/crousrules/update";
		}
		uiModel.asMap().clear();
		crousRule.setUpdateDate(LocalDateTime.now());
        crousRuleDaoService.merge(crousRule);
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new CrousRule());
        return "templates/admin/crousrules/update";
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, crousRuleDaoService.findCrousRule(id));
        return "templates/admin/crousrules/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        CrousRule crousRule = crousRuleDaoService.findCrousRule(id);
        crousRuleDaoService.remove(crousRule);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/crousrules";
    }

	void populateEditForm(Model uiModel, CrousRule crousRule) {
        uiModel.addAttribute("crousRule", crousRule);
        uiModel.addAttribute("crousruleconfigs", crousRuleConfigDaoService.findAllCrousRuleConfigs());
    }
}
