package org.esupportail.sgc.web.admin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardActionMessageService;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.domain.User;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/actionmessages")
@Controller
@RooWebScaffold(path = "admin/actionmessages", formBackingObject = CardActionMessage.class)
public class CardActionMessageController {
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "actionmsgs";
	}
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CardActionMessageService cardActionMessageService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("etats")
	public List<String> getListeEtats() {
		List<String> etats = new ArrayList<>();
		for(Etat etat : Arrays.asList(Card.Etat.values())){
			etats.add(etat.name());
		}
		Collections.sort(etats);
		return etats;
	}
	
	@ModelAttribute("userTypes")
	public List<String> getUserTypes() {
		return User.findDistinctUserType();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
    
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, 
    		@RequestParam(value = "sortFieldName", required = false, defaultValue="etatFinal") String sortFieldName, @RequestParam(value = "sortOrder", required = false, defaultValue="ASC") String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("cardactionmessages", CardActionMessage.findCardActionMessageEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) CardActionMessage.countCardActionMessages() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("cardactionmessages", CardActionMessage.findAllCardActionMessages(sortFieldName, sortOrder));
        }
        return "admin/actionmessages/list";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid CardActionMessage cardActionMessage, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, cardActionMessage);
            return "admin/actionmessages/create";
        }
        uiModel.asMap().clear();
        cardActionMessageService.persist(cardActionMessage);
        return "redirect:/admin/actionmessages/" + encodeUrlPathSegment(cardActionMessage.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid CardActionMessage cardActionMessage, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, cardActionMessage);
            return "admin/actionmessages/update";
        }
        uiModel.asMap().clear();
        cardActionMessageService.merge(cardActionMessage);
        return "redirect:/admin/actionmessages/" + encodeUrlPathSegment(cardActionMessage.getId().toString(), httpServletRequest);
    }


	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        CardActionMessage cardActionMessage = CardActionMessage.findCardActionMessage(id);
        cardActionMessageService.remove(cardActionMessage);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/actionmessages";
    }
    
	
}
