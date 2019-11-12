package org.esupportail.sgc.web.admin;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.ApiCrousService;
import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.crous.PatchIdentifier;
import org.esupportail.sgc.services.crous.RightHolder;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

@RequestMapping("/admin/crouserrorlogs")
@Controller
@RooWebScaffold(path = "admin/crouserrorlogs", formBackingObject = CrousErrorLog.class, create=false, delete=false, update=false)
public class CrousErrorLogController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public final static String[] CSV_FIELDS = new String[] {"code", "message", "field", "crousOperation", "esupSgcOperation", "date", "blocking", "eppn", "ine", "name", "mail", "csn", "crousUrl"};
	
	public final static String[] FIELD_MAPPING = new String[] {"code", "message", "field", "crousOperation", "esupSgcOperation", "date", "blocking", "userAccount.eppn", "userAccount.supannCodeINE", "userAccount.name", "userAccount.email", "card.csn", "crousUrl"};
	
	public final static CellProcessor[] CSV_PROCESSORS = new CellProcessor[] {new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""), 
																			 new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""), 
																			 new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""), 
																			 new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo("")};

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
	
	@ModelAttribute("searchCrousErrorLog")
	public CrousErrorLog getDefaultCrousErrorLog() {
		CrousErrorLog searchCrousErrorLog =  new CrousErrorLog();
		return searchCrousErrorLog;
	}
	
	@ModelAttribute("esupSgcOperations")
	public EsupSgcOperation[] getEsupSgcOperations() {
		return CrousErrorLog.EsupSgcOperation.values();
	}
	
	
	@RequestMapping(method = RequestMethod.DELETE, produces = "text/html")
    public String purgeAllLogs() {
		List<CrousErrorLog> logs = CrousErrorLog.findAllCrousErrorLogs();
		for(CrousErrorLog log : logs) {
			log.remove();
		}		
        return "redirect:/admin/crouserrorlogs";
    }
	
    @RequestMapping(produces = "text/html")
    public String list(CrousErrorLog searchCrousErrorLog, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, 
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
        uiModel.addAttribute("crouserrorlogs", CrousErrorLog.findCrousErrorLogs(searchCrousErrorLog, firstResult, sizeNo, sortFieldName, sortOrder));
        float nrOfPages = (float) CrousErrorLog.countCrousErrorLogs(searchCrousErrorLog) / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        uiModel.addAttribute("searchCrousErrorLog", searchCrousErrorLog);
        return "admin/crouserrorlogs/list";
    }
    
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	CrousErrorLog crousErrorLog = CrousErrorLog.findCrousErrorLog(id);
        uiModel.addAttribute("crouserrorlog", CrousErrorLog.findCrousErrorLog(id));
        uiModel.addAttribute("itemId", id);
        RightHolder esupSgcRightHolder = computeEsupSgcRightHolderWithSynchronizedInfos(crousErrorLog.getUserEppn());
        uiModel.addAttribute("esupSgcRightHolder", esupSgcRightHolder);
        try {
        	RightHolder crousEppnRightHolder =  crousService.getRightHolder(crousErrorLog.getUserEppn(), crousErrorLog.getUserEppn());
            uiModel.addAttribute("crousEppnRightHolder", crousEppnRightHolder);
        } catch(SgcRuntimeException ex) {
			uiModel.addAttribute("crousEppnRightHolderException", ex.getMessage());
        }
        try {
        	RightHolder crousEmailRightHolder =  crousService.getRightHolder(crousErrorLog.getUserEmail(), crousErrorLog.getUserEppn());
        	uiModel.addAttribute("crousEmailRightHolder", crousEmailRightHolder);
        } catch(SgcRuntimeException ex) {
			uiModel.addAttribute("crousEmailRightHolderException", ex.getMessage());
        }	
        if(crousErrorLog.getUserAccount().getSupannCodeINE() != null && !crousErrorLog.getUserAccount().getSupannCodeINE().isEmpty()) {
	        try {
	        	RightHolder crousIneRightHolder =  crousService.getRightHolder(crousErrorLog.getUserAccount().getSupannCodeINE(), crousErrorLog.getUserAccount().getEppn());
	        	uiModel.addAttribute("crousIneRightHolder", crousIneRightHolder);
	        } catch(SgcRuntimeException ex) {
				uiModel.addAttribute("crousIneRightHolderException", ex.getMessage());
	        }
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
    	crousService.patchIdentifier(patchIdentifier, EsupSgcOperation.PATCH);
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
    
    
    @RequestMapping(method = RequestMethod.GET, params="csv")
    public void exportCsv2OutputStream(HttpServletResponse response) {	
   
		response.setContentType("text/csv");
		String reportName = "esup-sgc-crous-errors.csv";
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
		response.setHeader("Content-disposition", "attachment;filename=" + reportName);
		
		CsvDozerBeanWriter beanWriter = null;
		Writer writer = null;
		try{
			OutputStream outputStream = response.getOutputStream();
			writer = new OutputStreamWriter(outputStream, "UTF8");
			beanWriter =  new CsvDozerBeanWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			beanWriter.writeHeader(CSV_FIELDS);

            beanWriter.configureBeanMapping(CrousErrorLog.class, FIELD_MAPPING);
			List<CrousErrorLog> logs = CrousErrorLog.findAllCrousErrorLogs("date", "desc");
			for(CrousErrorLog log : logs) {
				beanWriter.write(log, CSV_PROCESSORS);
			}		
		} catch(Exception e){
			log.warn("Interruption de l'export", e);
		} finally {
			if(beanWriter!=null) {
				try {
					beanWriter.close();
				} catch (IOException e) {
					log.warn("IOException ...", e);
				}
			}
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					log.warn("IOException ...", e);
				}
			}
		}
	}
    
}
