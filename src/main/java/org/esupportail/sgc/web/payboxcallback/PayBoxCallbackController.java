package org.esupportail.sgc.web.payboxcallback;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.paybox.PayBoxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/payboxcallback")
@Controller
public class PayBoxCallbackController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	PayBoxService payBoxService;
	
	@Resource
	LogService logService;
	
    @ResponseBody
    @RequestMapping
    public ResponseEntity<java.lang.String> index(@RequestParam String montant, @RequestParam String reference, @RequestParam(required = false) String auto, @RequestParam String erreur, @RequestParam String idtrans, @RequestParam String signature, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String queryString = request.getQueryString();
        String eppn = payBoxService.getEppn(reference);
        if (payBoxService.payboxCallback(montant, reference, auto, erreur, idtrans, signature, queryString, ip)) {
        	logService.log(null, ACTION.PAYMENT, RETCODE.SUCCESS, "", eppn, null);
        } else {
        	logService.log(null, ACTION.PAYMENT, RETCODE.FAILED, "", eppn, null);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    
}
