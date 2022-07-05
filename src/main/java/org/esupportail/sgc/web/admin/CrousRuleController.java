package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousRule;
import org.esupportail.sgc.services.crous.CrousRuleConfig;
import org.esupportail.sgc.services.crous.EsistCrousService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;

@RequestMapping("/admin/crousrules")
@Controller
@RooWebScaffold(path = "admin/crousrules", formBackingObject = CrousRule.class)
public class CrousRuleController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	EsistCrousService esistCrousService;

	@Resource
	AppliConfigService appliConfigService;

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crousrules";
	}

	@RequestMapping(value = "/{id}", produces = "text/html")
	public String show(Model uiModel) {
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(value="/updateCrousRules", method = RequestMethod.POST, produces = "text/html")
	public String updateCrousRules() {
		esistCrousService.updateCrousRules();
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(produces = "text/html")
	public String list(Model uiModel) {
		uiModel.addAttribute("crousruleconfigs", CrousRuleConfig.findAllCrousRuleConfigs());
		uiModel.addAttribute("crousrulesApi", CrousRule.findAllCrousRulesApi());
		uiModel.addAttribute("crousrulesCustom", CrousRule.findAllCrousRulesCustom());
		uiModel.addAttribute("defaultCnousIdCompagnyRate", appliConfigService.getDefaultCnousIdCompagnyRate());
		uiModel.addAttribute("defaultCnousIdRate", appliConfigService.getDefaultCnousIdRate());
		return "admin/crousrules/list";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String create(@Valid CrousRule crousRule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, crousRule);
			return "admin/crousrules/create";
		}
		uiModel.asMap().clear();
		crousRule.setUpdateDate(new Date());
		crousRule.persist();
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
	public String update(@Valid CrousRule crousRule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, crousRule);
			return "admin/crousrules/update";
		}
		uiModel.asMap().clear();
		crousRule.setUpdateDate(new Date());
		crousRule.merge();
		return "redirect:/admin/crousrules";
	}

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		return pathSegment;
	}
}
