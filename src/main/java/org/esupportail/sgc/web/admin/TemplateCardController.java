package org.esupportail.sgc.web.admin;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.EsupSgcBmpAsBase64Service;
import org.esupportail.sgc.services.TemplateCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/admin/templatecards")
@Controller
@RooWebScaffold(path = "admin/templatecards", formBackingObject = TemplateCard.class)
public class TemplateCardController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;	
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "template";
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@Resource	
	TemplateCardService templateCardService;

	@Resource
	EsupSgcBmpAsBase64Service esupSgcBmpAsBase64Service;
	
  @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid TemplateCard templateCard, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException {

        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, templateCard);
            return "admin/templatecards/create";
        }
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
        Calendar cal = Calendar.getInstance();
  		Date currentTime = cal.getTime();
        uiModel.asMap().clear();
        templateCard.setDateModification(currentTime);
        templateCard.setModificateur(eppn);
        templateCardService.setTemplateCardPhotofile(templateCard, "logo");
        templateCardService.setTemplateCardPhotofile(templateCard, "masque");
        templateCardService.setTemplateCardPhotofile(templateCard, "qrCode");
        templateCard.persist();

        return "redirect:/admin/templatecards/" + encodeUrlPathSegment(templateCard.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) throws FileNotFoundException {
    	TemplateCard templateCard = templateCardService.getDefaultTemplateCard();
        populateEditForm(uiModel, templateCard);
        return "admin/templatecards/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
         addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("templatecard", TemplateCard.findTemplateCard(id));
        uiModel.addAttribute("itemId", id);
        return "admin/templatecards/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("templatecards", TemplateCard.findTemplateCardEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) TemplateCard.countTemplateCards() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("templatecards", TemplateCard.findAllTemplateCards(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        
        return "admin/templatecards/list";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html", value="update" )//You should send POST and set _method to PUT (same as sending forms) to make your files visible
    public String update(@Valid TemplateCard templateCard, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException {
        Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
    	TemplateCard beforeMaj = TemplateCard.findTemplateCard(templateCard.getId());
    	uiModel.asMap().clear();
    	beforeMaj.setKey(templateCard.getKey());
    	beforeMaj.setName(templateCard.getName());
    	beforeMaj.setNumVersion(templateCard.getNumVersion());
    	beforeMaj.setDescription(templateCard.getDescription());
    	beforeMaj.setCssStyle(templateCard.getCssStyle());
    	beforeMaj.setDescription(templateCard.getDescription());
    	beforeMaj.setCssMobileStyle(templateCard.getCssMobileStyle());
    	beforeMaj.setDateModification(currentTime);
    	beforeMaj.setCodeBarres(templateCard.isCodeBarres());
    	beforeMaj.setModificateur(eppn);
    	beforeMaj.merge();
    	if(templateCard.getLogo().isEmpty()){
    		templateCard.setPhotoFileLogo(beforeMaj.getPhotoFileLogo());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "logo");
    		beforeMaj.setPhotoFileLogo(templateCard.getPhotoFileLogo());
    		beforeMaj.getPhotoFileLogo().persist();
    	}
    	if(templateCard.getMasque().isEmpty()){
    		templateCard.setPhotoFileMasque(beforeMaj.getPhotoFileMasque());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "masque");
    		beforeMaj.setPhotoFileMasque(templateCard.getPhotoFileMasque());
    		beforeMaj.getPhotoFileMasque().persist();
    	}
    	if(templateCard.getQrCode().isEmpty()){
    		templateCard.setPhotoFileQrCode(beforeMaj.getPhotoFileQrCode());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "qrCode");
    		beforeMaj.setPhotoFileQrCode(templateCard.getPhotoFileQrCode());
    		beforeMaj.getPhotoFileQrCode().persist();
    	}    	
    	
        return "redirect:/admin/templatecards/" + encodeUrlPathSegment(beforeMaj.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, TemplateCard.findTemplateCard(id));
        return "admin/templatecards/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        TemplateCard templateCard = TemplateCard.findTemplateCard(id);
        templateCard.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/templatecards";
    }
    
    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("templateCard_datemodification_date_format", "dd/MM/yyyy");
    }
    
    void populateEditForm(Model uiModel, TemplateCard templateCard) {
        uiModel.addAttribute("templateCard", templateCard);
        addDateTimeFormatPatterns(uiModel);
    }
    
    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
		pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
    
	@RequestMapping(value="/photo/{type}/{templateId}")
	@Transactional
	public void getPhoto(@PathVariable String type, @PathVariable Long templateId, HttpServletResponse response) throws IOException, SQLException {
		
		TemplateCard templateCard = TemplateCard.findTemplateCard(templateId);
		PhotoFile photoFile = null;
		if(templateCard != null) {
			if("logo".equals(type)){
				photoFile = templateCard.getPhotoFileLogo();
			}else if("masque".equals(type)){
				photoFile = templateCard.getPhotoFileMasque();
			}else if("qrCode".equals(type)){
				photoFile = templateCard.getPhotoFileQrCode();
			}
			
			Long size = photoFile.getFileSize();
			String contentType = photoFile.getContentType();
			response.setContentType(contentType);
			response.setContentLength(size.intValue());
			IOUtils.copy(photoFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
		}
	}
  
	@RequestMapping(value="fillTemplate", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody 
	public String getCardRectoData( @RequestParam(value="searchText") String searchText) {
		String jsonInString = "Aucune donnée à récupérer";
		Card card = Card.findCardsByEppnEquals(searchText).getResultList().get(0);
		LinkedHashMap<String, String> mapCarte = new LinkedHashMap<>();
		mapCarte.put("recto1", card.getUserAccount().getRecto1());
		mapCarte.put("recto2", card.getUserAccount().getRecto2());
		mapCarte.put("recto3", card.getUserAccount().getRecto3());
		mapCarte.put("recto4", card.getUserAccount().getRecto4());
		mapCarte.put("recto5", card.getUserAccount().getRecto5());
		mapCarte.put("recto6", card.getUserAccount().getRecto6());
		mapCarte.put("recto7", card.getUserAccount().getRecto7());
		mapCarte.put("id", card.getId().toString());
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonInString = mapper.writeValueAsString(mapCarte);
			
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données" , e);
		}
		
    	return jsonInString;
	}


	@RequestMapping(value="/bmp")
	@Transactional
	public ResponseEntity<byte[]> getBmp4test(@RequestParam Long templateId, @RequestParam EsupSgcBmpAsBase64Service.BmpType type, HttpServletResponse response) throws IOException, SQLException {
		TemplateCard templateCard = TemplateCard.findTemplateCard(templateId);
		if(templateCard != null) {
			Card card =  Card.findOneCardForTemplate(templateCard);
			if (card != null) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("image/bmp"));
				String bmpAsBase64 = esupSgcBmpAsBase64Service.getBmpCard(card.getId(), type);
				return new ResponseEntity(Base64.decodeBase64(bmpAsBase64), headers, HttpStatus.OK);
			}
		}
		return new ResponseEntity(null, null, HttpStatus.NOT_FOUND);
	}
    
}
