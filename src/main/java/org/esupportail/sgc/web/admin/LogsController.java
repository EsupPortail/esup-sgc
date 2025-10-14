package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.LogDaoService;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.LogService.TYPE;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/admin/logs")
@Controller
public class LogsController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    LogDaoService logDaoService;

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
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "logDate") Pageable pageable,
                       Log searchLog,
                       Model uiModel) {
        if(searchLog == null) {
            searchLog = new Log();
        }
        uiModel.addAttribute("searchLog", searchLog);
        Page<Log> logs = logDaoService.findLogEntries(searchLog, pageable);
        uiModel.addAttribute("logs", logs);
        addDateTimeFormatPatterns(uiModel);

        return "templates/admin/logs/list";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("log_logdate_date_format", "dd/MM/yyyy - HH:mm");
    }
}
