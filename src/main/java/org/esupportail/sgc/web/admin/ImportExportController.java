package org.esupportail.sgc.web.admin;
import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.ExportBean;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.ExportService;
import org.esupportail.sgc.services.ie.ImportExportService;
import org.esupportail.sgc.web.manager.CardSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  
	
	@Resource
	ImportExportService importExportService;
	
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public String index(Model uiModel) {
		uiModel.addAttribute("isInWorking", importExportService.isInWorking());
        uiModel.addAttribute("isInWorkingZip", importExportService.isInWorkingZip());
        return "templates/admin/import";
	}
	
	
	@RequestMapping(value = "/importCsvFile", method = RequestMethod.POST, produces = "text/html")
	public String importCsvFile(MultipartFile file, @RequestParam(defaultValue="False") Boolean inverseCsn) throws IOException, ParseException {
		if(file != null) {
			String filename = file.getOriginalFilename();
			log.info("importCsvFile retrieving file " + filename);
			InputStream stream = new  ByteArrayInputStream(file.getBytes());
			importExportService.consumeCsv(stream, inverseCsn);
		}
		return "redirect:/admin/import";
	}

    @RequestMapping(value = "/importZipFile", method = RequestMethod.POST, produces = "text/html")
    public String importZipFile(MultipartFile file) throws IOException, ParseException {
        if(file != null) {
            String filename = file.getOriginalFilename();
            log.info("importZipFile retrieving file " + filename);
            InputStream stream = file.getInputStream();
            importExportService.consumeZip(stream);
        }
        return "redirect:/admin/import";
    }

	@RequestMapping(value = "/exportCsvFile/{stats}", method = RequestMethod.GET)
	public void getCsv(@PathVariable("stats") String stats, HttpServletRequest request, HttpServletResponse response, Locale locale) throws IOException {
		
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
	
	@RequestMapping(value = "/tableStats", method = RequestMethod.GET)
	public void getCsvTableStats(HttpServletRequest request, HttpServletResponse response, Locale locale) throws IOException {
		
		response.setContentType("text/csv");
		String reportName = "stats.csv";
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
		response.setHeader("Content-disposition", "attachment;filename=" + reportName);
		
		final String[] header = exportService.getHeader("tableStats");
		
		Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
		
		ICsvBeanWriter beanWriter =  new CsvBeanWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

		beanWriter.writeHeader(header);
		
		try{
			List<ExportBean>  objs = exportService.getBeanTableStats(locale);
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

    @GetMapping(value = "/export.zip", produces = "application/zip")
    public void exportAll(HttpServletResponse response) {
        export(response, new CardSearchBean());
    }

    public void export(HttpServletResponse response, CardSearchBean searchBean)  {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"");
        try (OutputStream out = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            importExportService.exportToZip(searchBean, zos);
            zos.finish();
        } catch (IOException | SQLException e) {
            log.error("Error during export", e);
        }
    }

}
