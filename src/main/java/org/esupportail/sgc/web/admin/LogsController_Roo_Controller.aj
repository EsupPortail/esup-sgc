// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.web.admin.LogsController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

privileged aspect LogsController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String LogsController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("log", Log.findLog(id));
        uiModel.addAttribute("itemId", id);
        return "admin/logs/show";
    }
    
    void LogsController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("log_logdate_date_format", "dd/MM/yyyy - HH:mm");
    }
    
}
