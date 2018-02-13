package org.esupportail.sgc.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.web.wsrest.EsupNfcTagLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.zxing.WriterException;

@RequestMapping("/")
@Controller
public class IndexController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ExternalCardService externalCardService;
	
	@Resource 
	CardService cardService;
	
	@RequestMapping
	public String index(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	
		if(request.isUserInRole("ROLE_ADMIN")){
			return "redirect:/manager?index=first";
		} else if(auth.isAuthenticated()) {
			return "redirect:/user";
		} else {
			return "redirect:/login";
		}

	}
	
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		return "redirect:/";

	}
	
	
	@RequestMapping(value="/showCard",  method=RequestMethod.GET)
	@Transactional
	public String showCard(@RequestParam String csn, Model uiModel, HttpServletRequest request) {
		Card card = Card.findCardsByCsn(csn).getSingleResult();
		String photo = null;
	
        try {
    		PhotoFile photoFile = card.getPhotoFile();
        	byte[] externalCardPhoto = IOUtils.toByteArray(photoFile.getBigFile().getBinaryFile().getBinaryStream());
			photo = java.util.Base64.getEncoder().encodeToString(externalCardPhoto);
        } catch (Exception e) {
            log.error("No photo found", e);
        }
        
        if(photo == null){
        	
        	File noPhotoFile = new File(getClass().getResource("/nophoto.png").getFile());	
    		try {
    			FileInputStream imageInFile = new FileInputStream(noPhotoFile);
    			byte imageData[] = new byte[(int) noPhotoFile.length()];
    			imageInFile.read(imageData);
    			photo = Base64.getEncoder().encodeToString(imageData);
    		}catch(Exception e){
    			log.error("error get nophoto.png", e);
    		}
        }
        
        uiModel.addAttribute("photo", photo);
        
        String logo = null;
        File logoFile = new File(getClass().getResource("/logo.png").getFile());
        try {
			FileInputStream imageInFile = new FileInputStream(logoFile);
			byte imageData[] = new byte[(int) logoFile.length()];
			imageInFile.read(imageData);
			logo = Base64.getEncoder().encodeToString(imageData);
		}catch(Exception e){
			log.error("error get logo.png", e);
		}       

        try {
	        uiModel.addAttribute("qrCode", cardService.getQrCodeSvg(card.getQrcode()));
		} catch (Exception e) {
			log.error("error get qrCode", e);
		}
        
        
        uiModel.addAttribute("logo", logo);

		uiModel.addAttribute("card", card);
		return "showCard";
	}
	
}
