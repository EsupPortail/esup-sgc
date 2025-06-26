package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.PrefsDaoService;
import org.esupportail.sgc.domain.Prefs;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/admin/prefses")
@Controller
public class PrefsController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    PrefsDaoService prefsDaoService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "prefs";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  


	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("prefs", prefsDaoService.findPrefs(id));
        uiModel.addAttribute("itemId", id);
        return "admin/prefses/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "dateModification") Pageable pageable,
                       Model uiModel,
                       HttpServletRequest request) {
        Page<Prefs> prefses = prefsDaoService.findPrefsEntries(pageable);
        uiModel.addAttribute("prefses", prefses);
        addDateTimeFormatPatterns(uiModel);
        return "templates/admin/prefses/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Prefs prefs, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, prefs);
            return "templates/admin/prefses/update";
        }
        uiModel.asMap().clear();
        prefsDaoService.merge(prefs);
        return "redirect:/admin/prefses/" + encodeUrlPathSegment(prefs.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, prefsDaoService.findPrefs(id));
        return "templates/admin/prefses/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Prefs prefs = prefsDaoService.findPrefs(id);
        prefsDaoService.remove(prefs);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/prefses";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("prefs_datemodification_date_format", "dd/MM/yyyy");
    }

	void populateEditForm(Model uiModel, Prefs prefs) {
        uiModel.addAttribute("prefs", prefs);
        addDateTimeFormatPatterns(uiModel);
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
