package org.esupportail.sgc.web.admin;

import org.esupportail.sgc.dao.CrousPatchIdentifierDaoService;
import org.esupportail.sgc.domain.AppliConfig;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.crous.CrousPatchIdentifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/admin/crouspatchids")
@Controller
public class CrousPatchIdentifierController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CrousPatchIdentifierService crousPatchIdentifierService;

    @Resource
    CrousPatchIdentifierDaoService crousPatchIdentifierDaoService;
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crouspatchids";
	}   
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("isInWorking")
	public Boolean isInWorking() {
		return crousPatchIdentifierService.isInWorking();
	}
	
	@ModelAttribute("havePatchIdentifiersToProceed")
	public Boolean havePatchIdentifiersToProceed() {
		return crousPatchIdentifierDaoService.countFindCrousPatchIdentifiersByPatchSuccessNotEquals(true) > 0;
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@ModelAttribute("crousIneAsIdentifier")
	public Boolean getCrousIneAsIdentifier() {
		return appliConfigService.getCrousIneAsIdentifier();
	} 
	
	@RequestMapping(value = "/addCsvFile", method = RequestMethod.POST, produces = "text/html")
	public String addCrousCsvFile(MultipartFile file, Model uiModel) throws IOException, ParseException {
		
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("CrousPatchIdentifierService retrieving file " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			crousPatchIdentifierService.consumeCsv(stream);
		}	
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids";
	}
	
	
	@RequestMapping(value = "/patchIdentifiers", method = RequestMethod.POST, produces = "text/html")
	public String patchIdentifiers(Model uiModel) {
		crousPatchIdentifierService.patchIdentifiers();
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids";
	}
	
	@RequestMapping(value = "/deletePatchIdentifiants", method = RequestMethod.POST, produces = "text/html")
	public String deletePatchIdentifiants(Model uiModel)  {
		crousPatchIdentifierService.deletePatchIdentifiants();	
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids";
	}
	
	@RequestMapping(value = "/generatePatchIdentifiersIne", method = RequestMethod.POST, produces = "text/html")
	public String generatePatchIdentifiersIne(Model uiModel)  {
		crousPatchIdentifierService.generatePatchIdentifiersIne();	
		uiModel.asMap().clear();
		return "redirect:/admin/crouspatchids";
	}
	

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid CrousPatchIdentifier crousPatchIdentifier, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, crousPatchIdentifier);
            return "templates/admin/crouspatchids/create";
        }
        uiModel.asMap().clear();
        crousPatchIdentifierDaoService.persist(crousPatchIdentifier);
        return "redirect:/admin/crouspatchids/" + encodeUrlPathSegment(crousPatchIdentifier.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new CrousPatchIdentifier());
        return "templates/admin/crouspatchids/create";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("crouspatchidentifier", crousPatchIdentifierDaoService.findCrousPatchIdentifier(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/crouspatchids/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.ASC, sort = "oldId") Pageable pageable,
                       Model uiModel) {

        Page<CrousPatchIdentifier> crousPatchIdentifiers = crousPatchIdentifierDaoService.findAll(pageable);
        uiModel.addAttribute("crouspatchidentifiers", crousPatchIdentifiers);

        return "templates/admin/crouspatchids/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid CrousPatchIdentifier crousPatchIdentifier, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, crousPatchIdentifier);
            return "templates/admin/crouspatchids/update";
        }
        uiModel.asMap().clear();
        crousPatchIdentifierDaoService.merge(crousPatchIdentifier);
        return "redirect:/admin/crouspatchids/" + encodeUrlPathSegment(crousPatchIdentifier.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, crousPatchIdentifierDaoService.findCrousPatchIdentifier(id));
        return "templates/admin/crouspatchids/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        CrousPatchIdentifier crousPatchIdentifier = crousPatchIdentifierDaoService.findCrousPatchIdentifier(id);
        crousPatchIdentifierDaoService.remove(crousPatchIdentifier);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/crouspatchids";
    }

	void populateEditForm(Model uiModel, CrousPatchIdentifier crousPatchIdentifier) {
        uiModel.addAttribute("crousPatchIdentifier", crousPatchIdentifier);
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

