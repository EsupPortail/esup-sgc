// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.web.admin.PayboxTransactionLogController;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

privileged aspect PayboxTransactionLogController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String PayboxTransactionLogController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("payboxtransactionlog", PayboxTransactionLog.findPayboxTransactionLog(id));
        uiModel.addAttribute("itemId", id);
        return "admin/payboxtransactions/show";
    }
    
    void PayboxTransactionLogController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("payboxTransactionLog_transactiondate_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
    }
    
}
