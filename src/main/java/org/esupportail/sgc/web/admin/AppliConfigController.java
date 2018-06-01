package org.esupportail.sgc.web.admin;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.AppliConfig.TypeConfig;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/config")
@Controller
@RooWebScaffold(path = "admin/config", formBackingObject = AppliConfig.class)
public class AppliConfigController {
	
	@Resource
	AppliConfigService appliConfigService;
	
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
    public String filterTabs(@RequestParam("searchField") String searchField, @RequestParam(value = "page", required = true) Long page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("appliconfigs", AppliConfig.findAppliConfigsByType(TypeConfig.valueOf(searchField), sortFieldName, sortOrder).setFirstResult(firstResult).getResultList());
            float nrOfPages = (float) AppliConfig.countFindAppliConfigsByType(TypeConfig.valueOf(searchField)) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("appliconfigs", AppliConfig.findAllAppliConfigs(sortFieldName, sortOrder));
        }
        uiModel.addAttribute("selectedType", searchField);
        return "admin/config/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfig);
            return "admin/config/update";
        }
        uiModel.asMap().clear();
        appliConfigService.merge(appliConfig);
        return "redirect:/admin/config/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        AppliConfig appliConfig = AppliConfig.findAppliConfig(id);
        appliConfigService.remove(appliConfig);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/config";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfig);
            return "admin/config/create";
        }
        uiModel.asMap().clear();
        appliConfigService.persist(appliConfig);
        return "redirect:/admin/config/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }
    
}
