package org.esupportail.sgc.web.admin;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.esupportail.sgc.dao.UserDaoService;
import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.crous.*;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

@RequestMapping("/admin/crouserrorlogs")
@Controller
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
	UserInfoService userInfoService;

    @Resource
    UserDaoService userDaoService;

    @Resource
    CrousErrorLogDaoService crousErrorLogDaoService;
	
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

    /*
        Empty value strings are trimmed to null.
        Usefull for jpa repositories to avoid empty string queries.
    */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
	
	@RequestMapping(method = RequestMethod.DELETE, produces = "text/html")
    public String purgeAllLogs() {
		List<CrousErrorLog> logs = crousErrorLogDaoService.findAllCrousErrorLogs();
		for(CrousErrorLog log : logs) {
            crousErrorLogDaoService.remove(log);
		}		
        return "redirect:/admin/crouserrorlogs";
    }
	
    @RequestMapping(produces = "text/html")
    public String list(CrousErrorLog searchCrousErrorLog,
                       @PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "date") Pageable pageable,
                       Model uiModel) {

        Page<CrousErrorLog> crousErrorLogs = crousErrorLogDaoService.findCrousErrorLogs(searchCrousErrorLog, pageable);
        uiModel.addAttribute("crouserrorlogs", crousErrorLogs);
        uiModel.addAttribute("searchCrousErrorLog", searchCrousErrorLog);

		List<String> crousMessages = crousErrorLogDaoService.getCrousErrorLogMessages();
		uiModel.addAttribute("crousMessages", crousMessages);

        List<String> codes = crousErrorLogDaoService.getCrousErrorLogCodes();
        uiModel.addAttribute("codes", codes);

        return "templates/admin/crouserrorlogs/list";
    }
    
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	CrousErrorLog crousErrorLog = crousErrorLogDaoService.findCrousErrorLog(id);
        uiModel.addAttribute("crouserrorlog", crousErrorLogDaoService.findCrousErrorLog(id));
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

        return "templates/admin/crouserrorlogs/show";
    }

	protected RightHolder computeEsupSgcRightHolderWithSynchronizedInfos(String eppn) {
		User user = userDaoService.findUser(eppn);
        User dummyUser = new User();
		dummyUser.setEppn(user.getEppn());
		dummyUser.setCrous(user.getCrous());
        userInfoService.setAdditionalsInfo(dummyUser, null);
        RightHolder esupSgcRightHolder =  apiCrousService.computeEsupSgcRightHolder(dummyUser, true);
		return esupSgcRightHolder;
	}

    
    @RequestMapping(value = "/{id}/patchIdentifier", produces = "text/html", method = RequestMethod.POST)
    public String  patchIdentifier(@PathVariable("id") Long id, @Valid PatchIdentifier patchIdentifier, BindingResult bindingResult, Model uiModel) {
    	CrousErrorLog crousErrorLog = crousErrorLogDaoService.findCrousErrorLog(id);
    	crousService.patchIdentifier(patchIdentifier, EsupSgcOperation.PATCH);
    	logService.log(crousErrorLog.getCardId(), ACTION.CROUS_PATCH_IDENTIFIER, RETCODE.SUCCESS, "PatchIdentifier CROUS : " + patchIdentifier, crousErrorLog.getUserEppn(), null);
        return "redirect:/admin/crouserrorlogs/" + id;
    }
    

    @Transactional
    @RequestMapping(value = "/{id}/desactivateCrous", produces = "text/html", method = RequestMethod.POST)
    public String  desactivateCrous(@PathVariable("id") Long id, Model uiModel) {
    	CrousErrorLog crousErrorLog = crousErrorLogDaoService.findCrousErrorLog(id);
    	User user = crousErrorLog.getUserAccount();
    	user.setCrous(false);
    	userDaoService.merge(user);
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
			List<CrousErrorLog> logs = crousErrorLogDaoService.findAllCrousErrorLogs("date", "desc");
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
