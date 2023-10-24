package org.esupportail.sgc.web.user;

import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.Card.MotifDisable;
import org.esupportail.sgc.domain.PayBoxForm;
import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.esupportail.sgc.domain.PhotoFile;
import org.esupportail.sgc.domain.TemplateCard;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.security.ShibAuthenticatedUserDetailsService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.CardEtatService;
import org.esupportail.sgc.services.CardService;
import org.esupportail.sgc.services.EsupSgcBmpAsBase64Service;
import org.esupportail.sgc.services.ExternalCardService;
import org.esupportail.sgc.services.LogService;
import org.esupportail.sgc.services.LogService.ACTION;
import org.esupportail.sgc.services.LogService.RETCODE;
import org.esupportail.sgc.services.TemplateCardService;
import org.esupportail.sgc.services.UserService;
import org.esupportail.sgc.services.crous.CrousService;
import org.esupportail.sgc.services.esc.ApiEscrService;
import org.esupportail.sgc.services.ie.ImportExportCardService;
import org.esupportail.sgc.services.paybox.PayBoxService;
import org.esupportail.sgc.services.userinfos.ExtUserInfoService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.esupportail.sgc.tools.PrettyStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
		return "user/shib";
	}
}

