package org.esupportail.sgc.web.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.esupportail.sgc.dao.CardActionMessageDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.CardActionMessage;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardActionMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequestMapping("/admin/actionmessages")
@Controller
public class CardActionMessageController {
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "actionmsgs";
	}
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	CardActionMessageService cardActionMessageService;

    @Resource
    CardActionMessageDaoService cardActionMessageDaoService;

    @Resource
    UserDaoService userDaoService;
	
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
		return userDaoService.findDistinctUserType();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
    
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.ASC, sort = "etatFinal") Pageable pageable,
                       Model uiModel) {
        Page<CardActionMessage> cardactionmessages = cardActionMessageDaoService.findCardActionMessages(pageable);
        uiModel.addAttribute("cardactionmessages", cardactionmessages);
        uiModel.addAttribute("cardActionsMessagesConflictsList", cardActionMessageService.getCardActionMessagesAutoInConflict());
        uiModel.addAttribute("cardActionsMessagesUnreachableList", cardActionMessageService.getCardActionMessagesUnreachable());

        return "templates/admin/actionmessages/list";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid CardActionMessage cardActionMessage, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, cardActionMessage);
            return "templates/admin/actionmessages/update";
        }
        uiModel.asMap().clear();
        cardActionMessageService.persist(cardActionMessage);
        return "redirect:/admin/actionmessages/" + encodeUrlPathSegment(cardActionMessage.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid CardActionMessage cardActionMessage, BindingResult bindingResult, Model uiModel, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, cardActionMessage);
            return "templates/admin/actionmessages/update";
        }
        uiModel.asMap().clear();
        cardActionMessageService.merge(cardActionMessage);
        return "redirect:/admin/actionmessages/" + encodeUrlPathSegment(cardActionMessage.getId().toString(), request);
    }


	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        CardActionMessage cardActionMessage = cardActionMessageDaoService.findCardActionMessage(id);
        cardActionMessageService.remove(cardActionMessage);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/actionmessages";
    }
    
	

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new CardActionMessage());
        return "templates/admin/actionmessages/update";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, cardActionMessageDaoService.findCardActionMessage(id));
        uiModel.addAttribute("itemId", id);
        return "templates/admin/actionmessages/update";
    }

	void populateEditForm(Model uiModel, CardActionMessage cardActionMessage) {
        uiModel.addAttribute("cardActionMessage", cardActionMessage);
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
