package org.esupportail.sgc.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Display HTTP headers in HTML page.
 */
@RequestMapping("/user/shib")
@Controller
public class InfoHeadersShibController {

	public final Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping
	public String index(Model uiModel, HttpServletRequest request) {

		SortedMap<String, String> httpHeaders = new TreeMap<>();
		Enumeration<String> headerEnum = request.getHeaderNames();
		while(headerEnum.hasMoreElements()) {
			String headerName = headerEnum.nextElement();
			httpHeaders.put(headerName, request.getHeader(headerName));
		}
		uiModel.addAttribute("httpHeader", httpHeaders);
		return "templates/user/shib";
	}
}

