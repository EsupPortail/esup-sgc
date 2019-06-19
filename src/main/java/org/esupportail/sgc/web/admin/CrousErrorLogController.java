package org.esupportail.sgc.web.admin;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.CrousAccountForbiddenException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.ApiCrousService;
import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.PatchIdentifier;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/crouserrorlogs")
@Controller
@RooWebScaffold(path = "admin/crouserrorlogs", formBackingObject = CrousErrorLog.class, create=false, delete=false, update=false)
public class CrousErrorLogController {
	
	@Resource
	CrousService crousService;
	
	@Resource
	ApiCrousService apiCrousService;
	
	@Resource
	LogService logService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@Resource
	private UserInfoService userInfoService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "crousError";
	}   
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, 
    		@RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel, HttpServletRequest request) {
    	if(sortFieldName == null) {
    		sortFieldName = "date";
    		sortOrder = "desc";
    	}
    	if(size == null) {
    		Object sizeInSession = request.getSession().getAttribute("size_in_session");
    		size = sizeInSession != null ? (Integer)sizeInSession : 10;
    		page = 1;
    	}
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        uiModel.addAttribute("crouserrorlogs", CrousErrorLog.findCrousErrorLogEntries(firstResult, sizeNo, sortFieldName, sortOrder));
        float nrOfPages = (float) CrousErrorLog.countCrousErrorLogs() / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "admin/crouserrorlogs/list";
    }
    
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	CrousErrorLog crousErrorLog = CrousErrorLog.findCrousErrorLog(id);
        uiModel.addAttribute("crouserrorlog", CrousErrorLog.findCrousErrorLog(id));
        uiModel.addAttribute("itemId", id);
        try {
        	RightHolder crousEppnRightHolder =  crousService.getRightHolder(crousErrorLog.getUserEppn());
            uiModel.addAttribute("crousEppnRightHolder", crousEppnRightHolder);
        } catch(CrousAccountForbiddenException ex) {
			uiModel.addAttribute("crousEppnRightHolderException", ex.getMessage());
        }
        RightHolder esupSgcRightHolder = computeEsupSgcRightHolderWithSynchronizedInfos(crousErrorLog.getUserEppn());
        uiModel.addAttribute("esupSgcRightHolder", esupSgcRightHolder);
        try {
        	RightHolder crousEmailRightHolder =  crousService.getRightHolder(crousErrorLog.getUserEmail());
        	uiModel.addAttribute("crousEmailRightHolder", crousEmailRightHolder);
        } catch(CrousAccountForbiddenException ex) {
			uiModel.addAttribute("crousEmailRightHolderException", ex.getMessage());
        }	

        return "admin/crouserrorlogs/show";
    }

	protected RightHolder computeEsupSgcRightHolderWithSynchronizedInfos(String eppn) {
		User user = User.findUser(eppn);
        User dummyUser = new User();
		dummyUser.setEppn(user.getEppn());
		dummyUser.setCrous(user.getCrous());
        userInfoService.setAdditionalsInfo(dummyUser, null);
        RightHolder esupSgcRightHolder =  apiCrousService.computeEsupSgcRightHolder(dummyUser, true);
		return esupSgcRightHolder;
	}

    
    @RequestMapping(value = "/{id}/patchIdentifier", produces = "text/html", method = RequestMethod.POST)
    public String  patchIdentifier(@PathVariable("id") Long id, @Valid PatchIdentifier patchIdentifier, BindingResult bindingResult, Model uiModel) {
    	CrousErrorLog crousErrorLog = CrousErrorLog.findCrousErrorLog(id);
    	crousService.patchIdentifier(patchIdentifier);
    	logService.log(crousErrorLog.getCardId(), ACTION.CROUS_PATCH_IDENTIFIER, RETCODE.SUCCESS, "PatchIdentifier CROUS : " + patchIdentifier, crousErrorLog.getUserEppn(), null);
        return "redirect:/admin/crouserrorlogs/" + id;
    }
    

    @Transactional
    @RequestMapping(value = "/{id}/desactivateCrous", produces = "text/html", method = RequestMethod.POST)
    public String  desactivateCrous(@PathVariable("id") Long id, Model uiModel) {
    	CrousErrorLog crousErrorLog = CrousErrorLog.findCrousErrorLog(id);
    	User user = crousErrorLog.getUserAccount();
    	user.setCrous(false);
    	user.merge();
    	logService.log(crousErrorLog.getCardId(), ACTION.CROUS_DESACTIVATION, RETCODE.SUCCESS, "Désactivation CROUS suite à erreur API : " + crousErrorLog.toString(), user.getEppn(), null);
        return "redirect:/manager?eppn=" + user.getEppn();
    }
    
}
