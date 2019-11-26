package org.esupportail.sgc.web.admin;
import java.util.Arrays;
import java.util.List;

import org.esupportail.sgc.domain.NavBarApp;
import org.esupportail.sgc.domain.NavBarApp.VisibleRole;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/navbars")
@Controller
@RooWebScaffold(path = "admin/navbars", formBackingObject = NavBarApp.class)
public class NavBarAppController {

    @ModelAttribute("active")
    public String getActiveMenu() {
        return "navbar";
    }
    

    @ModelAttribute("visibleRoles")
    public List<VisibleRole> getVisibleRoles() {
        return Arrays.asList(VisibleRole.values());
    }
    
}
