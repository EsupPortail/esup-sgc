package org.esupportail.sgc.web.admin;
import javax.annotation.Resource;

import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/logs")
@Controller
@RooWebScaffold(path = "admin/logs", formBackingObject = Log.class, create=false, delete=false)
public class LogsController {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "logs";
	}
	
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            if(sortFieldName==null){
            	sortFieldName = "logDate";
            }
            if(sortOrder==null){
            	sortOrder = "DESC";
            }
            uiModel.addAttribute("logs", Log.findLogEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) Log.countLogs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findAllLogs(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    
    @RequestMapping(params = "find=ByCardIdEquals", method = RequestMethod.GET)
    public String findLogsByCardIdEqual(@RequestParam("cardId") Long cardId, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByCardIdEquals(cardId, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float)  Log.countFindLogsByCardIdEquals(cardId) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByCardIdEquals(cardId, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = "find=ByEppnEquals", method = RequestMethod.GET)
    public String findLogsByEppnEqual(@RequestParam("eppn") String eppn, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnEquals(eppn, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float)  Log.countFindLogsByEppnEquals(eppn) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnEquals(eppn, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = "find=ByEppnCibleEquals", method = RequestMethod.GET)
    public String findLogsByEppnCibleEqual(@RequestParam("eppn") String eppn, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleEquals(eppn, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float)  Log.countFindLogsByEppnCibleEquals(eppn) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logs", Log.findLogsByEppnCibleEquals(eppn, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
}
