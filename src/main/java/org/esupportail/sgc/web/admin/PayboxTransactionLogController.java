package org.esupportail.sgc.web.admin;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.esupportail.sgc.dao.PayboxTransactionLogDaoService;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/payboxtransactions")
@Controller
public class PayboxTransactionLogController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    PayboxTransactionLogDaoService payboxTransactionLogDaoService;

    /*
  Empty value strings are trimmed to null.
  Usefull for jpa repositories to avoid empty string queries.
 */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
	
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
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "transactionDate") Pageable pageable,
                       PayboxTransactionLog searchLog,
    		Model uiModel, HttpServletRequest request) {
        if(searchLog == null) {
            searchLog = new PayboxTransactionLog();
        }
        Page<PayboxTransactionLog> payboxtransactionlogs = payboxTransactionLogDaoService.findPayboxTransactionLogEntries(searchLog, pageable);
        Long count = payboxTransactionLogDaoService.countPayboxTransactionLogs();
        uiModel.addAttribute("payboxtransactionlogs", payboxtransactionlogs);
        uiModel.addAttribute("searchLog", searchLog);

        return "templates/admin/payboxtransactions/list";

    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("payboxtransactionlog", payboxTransactionLogDaoService.findPayboxTransactionLog(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/payboxtransactions/show";
    }

}
