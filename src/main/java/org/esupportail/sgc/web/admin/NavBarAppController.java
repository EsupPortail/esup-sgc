package org.esupportail.sgc.web.admin;
import java.util.Arrays;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.esupportail.sgc.dao.NavBarAppDaoService;
import org.esupportail.sgc.domain.NavBarApp;
import org.esupportail.sgc.domain.NavBarApp.VisibleRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/admin/navbars")
@Controller
public class NavBarAppController {

    @Resource
    NavBarAppDaoService navBarAppDaoService;

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "navbar";
    }
    

    @ModelAttribute("visibleRoles")
    public List<VisibleRole> getVisibleRoles() {
        return Arrays.asList(VisibleRole.values());
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid NavBarApp navBarApp, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, navBarApp);
            return "templates/admin/navbars/create";
        }
        uiModel.asMap().clear();
        navBarAppDaoService.persist(navBarApp);
        return "redirect:/admin/navbars/" + encodeUrlPathSegment(navBarApp.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new NavBarApp());
        return "templates/admin/navbars/create";
    }

	@RequestMapping(produces = "text/html")
    public String list(Model uiModel) {
        uiModel.addAttribute("navbarapps", navBarAppDaoService.findAllNavBarApps("index", "ASC"));
        return "templates/admin/navbars/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid NavBarApp navBarApp, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, navBarApp);
            return "templates/admin/navbars/update";
        }
        uiModel.asMap().clear();
        navBarAppDaoService.merge(navBarApp);
        return "redirect:/admin/navbars/" + encodeUrlPathSegment(navBarApp.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, navBarAppDaoService.findNavBarApp(id));
        return "templates/admin/navbars/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, Model uiModel) {
        NavBarApp navBarApp = navBarAppDaoService.findNavBarApp(id);
        navBarAppDaoService.remove(navBarApp);
        uiModel.asMap().clear();
        return "redirect:/admin/navbars";
    }

	void populateEditForm(Model uiModel, NavBarApp navBarApp) {
        uiModel.addAttribute("navBarApp", navBarApp);
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
