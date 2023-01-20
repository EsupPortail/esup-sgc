package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/admin/payboxtransactions")
@Controller
@RooWebScaffold(path = "admin/payboxtransactions", formBackingObject = PayboxTransactionLog.class, create=false, delete=false, update=false)
@RooWebFinder
public class PayboxTransactionLogController {
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "paybox";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
    
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, 
    		@RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, 
    		Model uiModel, HttpServletRequest request) {
        if(sortFieldName == null || sortFieldName.isEmpty()) {
        	sortFieldName = "transactionDate";
        	sortOrder = "desc";
        }
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("payboxtransactionlogs", PayboxTransactionLog.findPayboxTransactionLogEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) PayboxTransactionLog.countPayboxTransactionLogs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("payboxtransactionlogs", PayboxTransactionLog.findAllPayboxTransactionLogs(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/payboxtransactions/list";
    }
}
