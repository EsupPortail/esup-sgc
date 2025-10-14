package org.esupportail.sgc.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter;

/*
 Allow to handle error pages defined in web.xml
 */
@Controller
public class ErrorPageController {

    @RequestMapping("/uncaughtException")
    public String handleUncaughtException(HttpServletRequest request, Model model) {
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("requestUri", requestUri);

        if (throwable != null) {
            model.addAttribute("exception", throwable);
            model.addAttribute("exception_stacktrace", getStackTrace(throwable));
            model.addAttribute("exception_message", throwable.getMessage());
        }

        return "templates/uncaughtException";
    }

    @RequestMapping(value="/resourceNotFound", produces = "text/html")
    public String handle404(HttpServletRequest request, Model model, HttpServletResponse response) {
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        model.addAttribute("requestUri", requestUri);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/html");
        return "templates/resourceNotFound";
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
