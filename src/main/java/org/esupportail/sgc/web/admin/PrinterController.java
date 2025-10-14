package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.PrinterDaoService;
import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RequestMapping("/admin/printers")
@Controller
public class PrinterController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    PrinterDaoService printerDaoService;

	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "printers";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		return pathSegment;
	}

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("printer", printerDaoService. findPrinter(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/printers/show";
    }

	@RequestMapping(produces = "text/html")
    public String list( Model uiModel) {
        uiModel.addAttribute("printers", printerDaoService.findAllPrinters(null, null));
        addDateTimeFormatPatterns(uiModel);
        return "templates/admin/printers/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Printer printer, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, printer);
            return "admin/printers/update";
        }
        uiModel.asMap().clear();
        printerDaoService.merge(printer);
        return "redirect:/admin/printers/" + encodeUrlPathSegment(printer.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, printerDaoService. findPrinter(id));
        return "templates/admin/printers/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Printer printer = printerDaoService. findPrinter(id);
        printerDaoService.remove(printer);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/printers";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("printer_connectiondate_date_format", "dd/MM/yyyy - HH:mm");
    }

	void populateEditForm(Model uiModel, Printer printer) {
        uiModel.addAttribute("printer", printer);
        addDateTimeFormatPatterns(uiModel);
    }
}
