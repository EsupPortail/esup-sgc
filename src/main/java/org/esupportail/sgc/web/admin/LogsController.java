package org.esupportail.sgc.web.admin;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.LogService.TYPE;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
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
@RooWebFinder
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
	
	@ModelAttribute("enumTypes")
	public List<TYPE> getEnumTypes() {
		List<TYPE> types = Arrays.asList(TYPE.values());
		return types;
	}
	
	@ModelAttribute("enumRetCodes")
	public List<RETCODE> getEnumRetCodes() {
		List<RETCODE> retCodes = Arrays.asList(RETCODE.values());
		return retCodes;
	}
	
	@ModelAttribute("enumActions")
	public List<ACTION> getEnumActions() {
		List<ACTION> actions = Arrays.asList(ACTION.values());
		return actions;
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
    
    @RequestMapping(params = "find=ByActionEquals", method = RequestMethod.GET)
    public String findLogsByActionEquals(@RequestParam("action") String action, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(!action.isEmpty()){
	    	if (page != null || size != null) {
	            int sizeNo = size == null ? 10 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            uiModel.addAttribute("logs", Log.findLogsByActionEquals(action, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
	            float nrOfPages = (float) Log.countFindLogsByActionEquals(action) / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            uiModel.addAttribute("logs", Log.findLogsByActionEquals(action, sortFieldName, sortOrder).getResultList());
	        }
        }else{
            uiModel.addAttribute("logs", Log.findLogEntries(0, 10, sortFieldName, sortOrder));
            float nrOfPages = (float) Log.countLogs() / 10;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}
        uiModel.addAttribute("actionSelected",action);
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = "find=ByTypeEquals", method = RequestMethod.GET)
    public String findLogsByTypeEquals(@RequestParam("type") String type, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(!type.isEmpty()){
	    	if (page != null || size != null) {
	            int sizeNo = size == null ? 10 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            uiModel.addAttribute("logs", Log.findLogsByTypeEquals(type, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
	            float nrOfPages = (float) Log.countFindLogsByTypeEquals(type) / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            uiModel.addAttribute("logs", Log.findLogsByTypeEquals(type, sortFieldName, sortOrder).getResultList());
	        }
    	}else{
            uiModel.addAttribute("logs", Log.findLogEntries(0, 10, sortFieldName, sortOrder));
            float nrOfPages = (float) Log.countLogs() / 10;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}
        uiModel.addAttribute("typeSelected",type);
        addDateTimeFormatPatterns(uiModel);
        return "admin/logs/list";
    }
    
    @RequestMapping(params = "find=ByRetCodeEquals", method = RequestMethod.GET)
    public String findLogsByRetCodeEquals(@RequestParam("retCode") String retCode, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(!retCode.isEmpty()){
	        if (page != null || size != null) {
	            int sizeNo = size == null ? 10 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            uiModel.addAttribute("logs", Log.findLogsByRetCodeEquals(retCode, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
	            float nrOfPages = (float) Log.countFindLogsByRetCodeEquals(retCode) / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            uiModel.addAttribute("logs", Log.findLogsByRetCodeEquals(retCode, sortFieldName, sortOrder).getResultList());
	        }
    	}else{
            uiModel.addAttribute("logs", Log.findLogEntries(0, 10, sortFieldName, sortOrder));
            float nrOfPages = (float) Log.countLogs() / 10;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("retCodeSelected",retCode);
        return "admin/logs/list";
    }
        
}
