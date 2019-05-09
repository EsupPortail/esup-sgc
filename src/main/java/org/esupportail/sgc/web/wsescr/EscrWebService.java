package org.esupportail.sgc.web.wsescr;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.services.escstudentservice.EscRemoteStudent;
import org.esupportail.sgc.services.escstudentservice.EscRemoteStudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Implementation of remote institutionnal service like described in ESCR
 * https://router.europeanstudentcard.eu/remote-services
 * 
 * -> ESUP-SGC accepts externals European Students Cards via this web service which is called by ESCR
 * 
 */
@Transactional
@RequestMapping("/wsescr")
@Controller
public class EscrWebService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	protected EscRemoteStudentService escRemoteStudentService;

	@ResponseBody
	@RequestMapping(value="/activate", method = RequestMethod.POST)
	public String activate(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR activate : " + escRemoteStudent);
		escRemoteStudentService.activate(escRemoteStudent);
		return "OK";
	}
	
	@ResponseBody
	@RequestMapping(value="/deactivate", method = RequestMethod.POST)
	public String deactivate(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR deactivate : " + escRemoteStudent);
		escRemoteStudentService.deactivate(escRemoteStudent);
		return "OK";
	}
	
	@ResponseBody
	@RequestMapping(value="/addcard", method = RequestMethod.POST)
	public String addCard(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR addCard : " + escRemoteStudent);
		escRemoteStudentService.addCard(escRemoteStudent);
		return "OK";
	}
	
	@ResponseBody
	@RequestMapping(value="/updatestudent", method = RequestMethod.POST)
	public String updateStudent(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR updateStudent : " + escRemoteStudent);
		escRemoteStudentService.updateStudent(escRemoteStudent);
		return "OK";
	}
	
	@ResponseBody
	@RequestMapping(value="/deletecard", method = RequestMethod.POST)
	public String deleteCard(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR deleteCard : " + escRemoteStudent);
		escRemoteStudentService.deleteCard(escRemoteStudent);
		return "OK";
	}
	
	@ResponseBody
	@RequestMapping(value="/deletestudent", method = RequestMethod.POST)
	public String deleteStudent(@RequestBody EscRemoteStudent escRemoteStudent) {
		log.info("ESCR deleteStudent : " + escRemoteStudent);
		escRemoteStudentService.deleteStudent(escRemoteStudent);
		return "OK";
	}
	
}
