package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.CardActionMessageDaoService;
import org.esupportail.sgc.dao.LogMailDaoService;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.LogMail;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.LogService.TYPE;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/admin/logmails")
@Controller
public class LogMailsController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    CardActionMessageDaoService cardActionMessageDaoService;

    @Resource
    LogMailDaoService logMailDaoService;

	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "logmails";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}

	@RequestMapping(produces = "text/html")
	public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "logDate") Pageable pageable,
                       LogMail searchLog,
                       Model uiModel) {
        Page<LogMail> logmails = logMailDaoService.findLogMailEntries(searchLog, pageable);
		uiModel.addAttribute("logmails", logmails);
		addDateTimeFormatPatterns(uiModel);
        return "templates/admin/logmails/list";
	}


	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("logmail", logMailDaoService.findLogMail(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/logmails/show";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logMail_logdate_date_format", "dd/MM/yyyy - HH:mm");
    }

}
