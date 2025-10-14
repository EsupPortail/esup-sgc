package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.crous.CrousRuleConfig;
import org.esupportail.sgc.services.crous.CrousRuleConfigDaoService;
import org.esupportail.sgc.services.crous.EsistCrousService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/admin/crousruleconfigs")
@Controller
public class CrousRuleConfigController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	EsistCrousService esistCrousService;

    @Resource
    UserDaoService userDaoService;

    @Resource
    CrousRuleConfigDaoService crousRuleConfigDaoService;

	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crousrules";
	}

	@RequestMapping(params = "form", produces = "text/html")
	public String createForm(Model uiModel) {
		populateEditForm(uiModel, new CrousRuleConfig());
		List<String> rneCodes = userDaoService.findDistinctRnecodes();
		uiModel.addAttribute("rneCodes", rneCodes);
        return "templates/admin/crousruleconfigs/create";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
	public String create(@Valid CrousRuleConfig crousRuleConfig, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttrs) {
		if (bindingResult.hasErrors()) {
			populateEditForm(uiModel, crousRuleConfig);
			return "templates/admin/crousruleconfigs/create";
		}
		uiModel.asMap().clear();
        crousRuleConfigDaoService.persist(crousRuleConfig);
		redirectAttrs.addFlashAttribute("messageSuccess", "La Configuration de la récupération dès règles a été ajoutée en base. " +
				"Vous devez maintenant 'Mettre à jour les règles depuis l'API CROUS/IZLY' pour effectivement récupérer ces règles liées.");
		return "redirect:/admin/crousrules";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
	public String delete(@PathVariable("id") Long id, Model uiModel) {
		esistCrousService.deleteCrousRuleConfig(id);
		uiModel.asMap().clear();
		return "redirect:/admin/crousrules";
	}

	void populateEditForm(Model uiModel, CrousRuleConfig crousRuleConfig) {
        uiModel.addAttribute("crousRuleConfig", crousRuleConfig);
    }
}
