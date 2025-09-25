package org.esupportail.sgc.web.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.dao.PhotoFileDaoService;
import org.esupportail.sgc.dao.TemplateCardDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.EsupSgcBmpAsBase64Service;
import org.esupportail.sgc.services.TemplateCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@RequestMapping("/admin/templatecards")
@Controller
public class TemplateCardController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	AppliConfigService appliConfigService;

    @Resource
    CardDaoService cardDaoService;
	
	@Resource	
	TemplateCardService templateCardService;

	@Resource
	EsupSgcBmpAsBase64Service esupSgcBmpAsBase64Service;

    @Resource
    PhotoFileDaoService photoFileDaoService;

    @Resource
    TemplateCardDaoService templateCardDaoService;

	@Resource
	ObjectMapper objectMapper;


    @ModelAttribute("active")
    public String getActiveMenu() {
        return "template";
    }

    @ModelAttribute("footer")
    public String getFooter() {
        return appliConfigService.pageFooter();
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid TemplateCard templateCard, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException {

        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, templateCard);
            return "admin/templatecards/create";
        }
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
        LocalDateTime currentTime = LocalDateTime.now();
        uiModel.asMap().clear();
        templateCard.setDateModification(currentTime);
        templateCard.setModificateur(eppn);
        templateCardService.setTemplateCardPhotofile(templateCard, "logo");
        templateCardService.setTemplateCardPhotofile(templateCard, "masque");
        templateCardService.setTemplateCardPhotofile(templateCard, "qrCode");
        templateCardDaoService.persist(templateCard);

        return "redirect:/admin/templatecards/" + encodeUrlPathSegment(templateCard.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) throws FileNotFoundException {
    	TemplateCard templateCard = templateCardService.getDefaultTemplateCard();
        populateEditForm(uiModel, templateCard);
        return "templates/admin/templatecards/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
         addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("templatecard", templateCardDaoService.findTemplateCard(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/templatecards/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "dateModification") Pageable pageable,
                       Model uiModel) {
        Page<TemplateCard> templatecards = templateCardDaoService.findTemplateCardEntries(pageable);
        uiModel.addAttribute("templatecards", templatecards);
        addDateTimeFormatPatterns(uiModel);
        return "templates/admin/templatecards/list";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html", value="update" )//You should send POST and set _method to PUT (same as sending forms) to make your files visible
    public String update(@Valid TemplateCard templateCard, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException {
        LocalDateTime currentTime = LocalDateTime.now();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String eppn = auth.getName();
    	TemplateCard beforeMaj = templateCardDaoService.findTemplateCard(templateCard.getId());
    	uiModel.asMap().clear();
    	beforeMaj.setKey(templateCard.getKey());
    	beforeMaj.setName(templateCard.getName());
    	beforeMaj.setNumVersion(templateCard.getNumVersion());
    	beforeMaj.setDescription(templateCard.getDescription());
    	beforeMaj.setCssStyle(templateCard.getCssStyle());
		beforeMaj.setCssBackStyle(templateCard.getCssBackStyle());
    	beforeMaj.setDescription(templateCard.getDescription());
    	beforeMaj.setCssMobileStyle(templateCard.getCssMobileStyle());
    	beforeMaj.setDateModification(currentTime);
    	beforeMaj.setCodeBarres(templateCard.isCodeBarres());
		beforeMaj.setBackSupported(templateCard.getBackSupported());
    	beforeMaj.setModificateur(eppn);
    	templateCardDaoService.merge(beforeMaj);
    	if(templateCard.getLogo().isEmpty()){
    		templateCard.setPhotoFileLogo(beforeMaj.getPhotoFileLogo());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "logo");
    		beforeMaj.setPhotoFileLogo(templateCard.getPhotoFileLogo());
            photoFileDaoService.persist(beforeMaj.getPhotoFileLogo());
    	}
    	if(templateCard.getMasque().isEmpty()){
    		templateCard.setPhotoFileMasque(beforeMaj.getPhotoFileMasque());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "masque");
    		beforeMaj.setPhotoFileMasque(templateCard.getPhotoFileMasque());
            photoFileDaoService.persist(beforeMaj.getPhotoFileMasque());
    	}
    	if(templateCard.getQrCode().isEmpty()){
    		templateCard.setPhotoFileQrCode(beforeMaj.getPhotoFileQrCode());
    	}else{
    		templateCardService.setTemplateCardPhotofile(templateCard, "qrCode");
    		beforeMaj.setPhotoFileQrCode(templateCard.getPhotoFileQrCode());
            photoFileDaoService.persist(beforeMaj.getPhotoFileQrCode());
    	}    	
    	
        return "redirect:/admin/templatecards/" + encodeUrlPathSegment(beforeMaj.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) throws FileNotFoundException {
        // We retrieve the templateCard from the database
	 	TemplateCard templateCard = templateCardDaoService.findTemplateCard(id);
		 // if backCss is null or empty, we set it to default cssStyle
		if(StringUtils.isEmpty(templateCard.getCssBackStyle())) {
            try {
                templateCard.setCssBackStyle(templateCardService.getDefaultTemplateCard().getCssBackStyle());
            } catch (FileNotFoundException e) {
                log.warn("Impossible de récupérer le css back par défaut", e);
            }
        }
        populateEditForm(uiModel, templateCardDaoService.findTemplateCard(id));
        return "templates/admin/templatecards/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        TemplateCard templateCard = templateCardDaoService.findTemplateCard(id);
        templateCardDaoService.remove(templateCard);
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
		
		TemplateCard templateCard = templateCardDaoService.findTemplateCard(templateId);
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
		Card card = cardDaoService.findCardsByEppnEquals(searchText).getResultList().get(0);
		LinkedHashMap<String, String> mapCarte = new LinkedHashMap<>();
		mapCarte.put("recto1", card.getUserAccount().getRecto1());
		mapCarte.put("recto2", card.getUserAccount().getRecto2());
		mapCarte.put("recto3", card.getUserAccount().getRecto3());
		mapCarte.put("recto4", card.getUserAccount().getRecto4());
		mapCarte.put("recto5", card.getUserAccount().getRecto5());
		mapCarte.put("recto6", card.getUserAccount().getRecto6());
		mapCarte.put("recto7", card.getUserAccount().getRecto7());
		mapCarte.put("verso1", card.getUserAccount().getVerso1());
		mapCarte.put("verso2", card.getUserAccount().getVerso2());
		mapCarte.put("verso3", card.getUserAccount().getVerso3());
		mapCarte.put("verso4", card.getUserAccount().getVerso4());
		mapCarte.put("verso5", card.getUserAccount().getVerso5());
		mapCarte.put("verso6", card.getUserAccount().getVerso6());
		mapCarte.put("verso7", card.getUserAccount().getVerso7());
		mapCarte.put("freeField1", card.getUserAccount().getFreeField1());
		mapCarte.put("freeField2", card.getUserAccount().getFreeField2());
		mapCarte.put("freeField3", card.getUserAccount().getFreeField3());
		mapCarte.put("freeField4", card.getUserAccount().getFreeField4());
		mapCarte.put("freeField5", card.getUserAccount().getFreeField5());
		mapCarte.put("freeField6", card.getUserAccount().getFreeField6());
		mapCarte.put("freeField7", card.getUserAccount().getFreeField7());
		mapCarte.put("id", card.getId().toString());
		
		try {
			jsonInString = objectMapper.writeValueAsString(mapCarte);
			
		} catch (Exception e) {
			log.warn("Impossible de récupérer les données" , e);
		}
		
    	return jsonInString;
	}


	@RequestMapping(value="/bmp")
	@Transactional
	public ResponseEntity<byte[]> getBmp4test(@RequestParam Long templateId, @RequestParam EsupSgcBmpAsBase64Service.BmpType type, HttpServletResponse response) throws IOException, SQLException {
		TemplateCard templateCard = templateCardDaoService.findTemplateCard(templateId);
		if(templateCard != null) {
			Card card =  cardDaoService.findOneCardForTemplate(templateCard);
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
