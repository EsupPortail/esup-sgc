package org.esupportail.sgc.web.admin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.esupportail.sgc.domain.ExportBean;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ExportService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

@RequestMapping("/admin/import")
@Controller
public class ImportExportController {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ExportService exportService;
	
	@Resource
	AppliConfigService appliConfigService;
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "import";
	}   
	
	@Resource
	ImportExportService importExportService;
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index(Model uiModel) {
		uiModel.addAttribute("isInWorking", importExportService.isInWorking());
		return "admin/import";
	}
	
	
	@RequestMapping(value = "/importCsvFile", method = RequestMethod.POST, produces = "text/html")
	public String importCsvFile(MultipartFile file, @RequestParam(defaultValue="False") Boolean inverseCsn) throws IOException, ParseException {
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("CrousSmartCardController retrieving file " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			importExportService.consumeCsv(stream, inverseCsn);
		}
		return "redirect:/admin/import";
	}

	@RequestMapping(value = "/exportCsvFile/{stats}", method = RequestMethod.GET)
	public void getCsv(@PathVariable("stats") String stats, HttpServletRequest request, HttpServletResponse response, Locale locale) throws UnsupportedEncodingException, IOException {
		
		response.setContentType("text/csv");
		String reportName = "editable.csv";
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
		response.setHeader("Content-disposition", "attachment;filename=" + reportName);
		
		final String[] header = exportService.getHeader(stats);
		
		Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
		
		ICsvBeanWriter beanWriter =  new CsvBeanWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

		beanWriter.writeHeader(header);
		
		try{
			List<ExportBean>  objs = exportService.getBean(stats, locale);
			for(ExportBean item : objs) {
				beanWriter.write(item, header);
			}
			beanWriter.flush();
			
			writer.close();
			
		}catch(Exception e){
			log.error("interruption de l'export !",e);
		} finally {
            if( beanWriter != null ) {
                beanWriter.close();
            }
		}
			
	}
}
