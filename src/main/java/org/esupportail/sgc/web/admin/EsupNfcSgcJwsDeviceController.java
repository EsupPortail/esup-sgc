package org.esupportail.sgc.web.admin;

import jakarta.validation.Valid;
import org.esupportail.sgc.dao.EsupNfcSgcJwsDeviceDaoService;
import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;
import org.esupportail.sgc.services.AppliConfigService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/admin/jwsdevices")
@Controller
public class EsupNfcSgcJwsDeviceController {
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    EsupNfcSgcJwsDeviceDaoService esupNfcSgcJwsDeviceDaoService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "nfc";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "id") Pageable pageable,
                       Model uiModel) {

        Page<EsupNfcSgcJwsDevice> esupNfcSgcJwsDevices = esupNfcSgcJwsDeviceDaoService.findEsupNfcSgcJwsDeviceEntries(pageable);
        uiModel.addAttribute("esupnfcsgcjwsdevices", esupNfcSgcJwsDevices);

        return "templates/admin/jwsdevices/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid EsupNfcSgcJwsDevice esupNfcSgcJwsDevice, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, esupNfcSgcJwsDevice);
            return "templates/admin/jwsdevices/update";
        }
        uiModel.asMap().clear();
        esupNfcSgcJwsDeviceDaoService.merge(esupNfcSgcJwsDevice);
        return "redirect:/admin/jwsdevices/" + encodeUrlPathSegment(esupNfcSgcJwsDevice.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, esupNfcSgcJwsDeviceDaoService.findEsupNfcSgcJwsDevice(id));
        return "templates/admin/jwsdevices/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        EsupNfcSgcJwsDevice esupNfcSgcJwsDevice = esupNfcSgcJwsDeviceDaoService.findEsupNfcSgcJwsDevice(id);
        esupNfcSgcJwsDeviceDaoService.remove(esupNfcSgcJwsDevice);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/jwsdevices";
    }

	void populateEditForm(Model uiModel, EsupNfcSgcJwsDevice esupNfcSgcJwsDevice) {
        uiModel.addAttribute("esupNfcSgcJwsDevice", esupNfcSgcJwsDevice);
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}
