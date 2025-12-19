package org.esupportail.sgc.web;

import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.math.raw.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/*
    Global controller advice to
    * handle uncaught exceptions
    * and init binder settings (disable request headers injection in model attributes).
 */
@ControllerAdvice
public class EsupSgcControllerAdvice implements Serializable {
	
	@Serial
    private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    ErrorPageController errorPageController;

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletResponse response) {
        log.error("Erreur non gérée", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                .getRequest();
        if(request != null) {
            String requestUri = request.getRequestURI();
            String statusCode = Integer.toString(response.getStatus());
            model.addAttribute("requestUri", requestUri);
            model.addAttribute("statusCode", statusCode);
        }
        model.addAttribute("exception", ex);
        model.addAttribute("exception_stacktrace", getStackTrace(ex));
        return "templates/uncaughtException";
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @InitBinder
    public void initBinder(ExtendedServletRequestDataBinder binder) {
        // disable binding of all fields from request headers
        // without this, headers form shibboleth authentication can be bound to model attributes
        binder.setHeaderPredicate(header -> false);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleResourceNotFound(NoResourceFoundException ex,
                                         HttpServletRequest request,
                                         Model model,
                                         HttpServletResponse response
                                         ) throws NoResourceFoundException {
        String uri = request.getRequestURI();

        // Si c'est une ressource versionnée (avec hash)
        if (uri.matches(".*/[^/]+-[a-f0-9]{32}\\.(css|js)$")) {
            // Log en WARNING au lieu d'ERROR
            log.warn("Ressource versionnée non trouvée (probablement cache navigateur): {}", uri);
            return errorPageController.handle404(request, model, response);
        }

        throw ex;
    }

}
