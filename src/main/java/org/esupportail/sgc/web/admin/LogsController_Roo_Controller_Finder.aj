// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.sgc.web.admin;

import java.util.Date;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.web.admin.LogsController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

privileged aspect LogsController_Roo_Controller_Finder {
    
    @RequestMapping(params = { "find=ByActionEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByActionEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByActionEquals";
    }
    
    @RequestMapping(params = { "find=ByCardIdEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByCardIdEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByCardIdEquals";
    }
    
    @RequestMapping(params = "find=ByCardIdEquals", method = RequestMethod.GET)
    public String LogsController.findLogsByCardIdEquals(@RequestParam("cardId") Long cardId, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByCardIdEquals(cardId, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByCardIdEquals(cardId) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByCardIdEquals(cardId, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByEppnCibleEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByEppnCibleEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByEppnCibleEquals";
    }
    
    @RequestMapping(params = "find=ByEppnCibleEquals", method = RequestMethod.GET)
    public String LogsController.findLogsByEppnCibleEquals(@RequestParam("eppnCible") String eppnCible, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleEquals(eppnCible, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByEppnCibleEquals(eppnCible) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleEquals(eppnCible, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByEppnCibleLike", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByEppnCibleLikeForm(Model uiModel) {
        return "admin/logs/findLogsByEppnCibleLike";
    }
    
    @RequestMapping(params = "find=ByEppnCibleLike", method = RequestMethod.GET)
    public String LogsController.findLogsByEppnCibleLike(@RequestParam("eppnCible") String eppnCible, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleLike(eppnCible, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByEppnCibleLike(eppnCible) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleLike(eppnCible, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByEppnEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByEppnEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByEppnEquals";
    }
    
    @RequestMapping(params = "find=ByEppnEquals", method = RequestMethod.GET)
    public String LogsController.findLogsByEppnEquals(@RequestParam("eppn") String eppn, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnEquals(eppn, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByEppnEquals(eppn) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnEquals(eppn, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByEppnLike", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByEppnLikeForm(Model uiModel) {
        return "admin/logs/findLogsByEppnLike";
    }
    
    @RequestMapping(params = "find=ByEppnLike", method = RequestMethod.GET)
    public String LogsController.findLogsByEppnLike(@RequestParam("eppn") String eppn, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnLike(eppn, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByEppnLike(eppn) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnLike(eppn, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByLogDateLessThan", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByLogDateLessThanForm(Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/findLogsByLogDateLessThan";
    }
    
    @RequestMapping(params = "find=ByLogDateLessThan", method = RequestMethod.GET)
    public String LogsController.findLogsByLogDateLessThan(@RequestParam("logDate") @DateTimeFormat(pattern = "dd/MM/yyyy - HH:mm") Date logDate, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByLogDateLessThan(logDate, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) Log.countFindLogsByLogDateLessThan(logDate) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByLogDateLessThan(logDate, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = { "find=ByRetCodeEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByRetCodeEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByRetCodeEquals";
    }
    
    @RequestMapping(params = { "find=ByTypeEquals", "form" }, method = RequestMethod.GET)
    public String LogsController.findLogsByTypeEqualsForm(Model uiModel) {
        return "admin/logs/findLogsByTypeEquals";
    }
    
}
