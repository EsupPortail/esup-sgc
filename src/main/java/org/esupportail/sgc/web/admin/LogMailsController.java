package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.domain.Log;
import org.esupportail.sgc.domain.LogMail;
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

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/admin/logmails")
@Controller
@RooWebScaffold(path = "admin/logmails", formBackingObject = LogMail.class, create=false, delete=false, update=false)
@RooWebFinder
public class LogMailsController {
	
	@Resource
	AppliConfigService appliConfigService;
	
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
			uiModel.addAttribute("logmails", LogMail.findLogMailEntries(firstResult, sizeNo, sortFieldName, sortOrder));
			float nrOfPages = (float) LogMail.countLogMails() / sizeNo;
			uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
		} else {
			uiModel.addAttribute("logmails", LogMail.findAllLogMails(sortFieldName, sortOrder));
		}
		addDateTimeFormatPatterns(uiModel);
		return "admin/logmails/list";
	}

}
