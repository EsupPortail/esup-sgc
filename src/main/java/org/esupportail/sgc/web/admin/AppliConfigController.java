package org.esupportail.sgc.web.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.AppliConfigDaoService;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliConfig.TypeConfig;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/admin/config")
@Controller
public class AppliConfigController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    AppliConfigDaoService appliConfigDaoService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "configs";
	}
	
	@ModelAttribute("listTypes")
	public List<String> getTypes() {
		return  appliConfigService.getTypes();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}   
	
    @RequestMapping(value="/tabs", method=RequestMethod.GET)
    public String filterTabs(@RequestParam("searchField") String searchField,
                             @PageableDefault(size = 10, direction = Sort.Direction.ASC, sort = "key") Pageable pageable,
                             Model uiModel) {

        Page<AppliConfig> appliconfigs = appliConfigDaoService.findAppliConfigs(StringUtils.isEmpty(searchField) ? null : TypeConfig.valueOf(searchField), pageable);
        uiModel.addAttribute("selectedType", searchField);
        uiModel.addAttribute("appliconfigs", appliconfigs);

        return "templates/admin/config/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfig);
            return "templates/admin/config/update";
        }
        uiModel.asMap().clear();
        appliConfigService.merge(appliConfig);
        return "redirect:/admin/config/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        AppliConfig appliConfig = appliConfigDaoService.findAppliConfig(id);
        appliConfigService.remove(appliConfig);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/config";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        uiModel.asMap().clear();
        appliConfigService.persist(appliConfig);
        return "redirect:/admin/config/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }


	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.ASC, sort = "key") Pageable pageable,
                       Model uiModel) {
        return filterTabs(null, pageable, uiModel);
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, appliConfigDaoService.findAppliConfig(id));
        return "templates/admin/config/update";
    }

	void populateEditForm(Model uiModel, AppliConfig appliConfig) {
        uiModel.addAttribute("appliConfig", appliConfig);
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
