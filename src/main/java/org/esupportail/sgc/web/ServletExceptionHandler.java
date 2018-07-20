package org.esupportail.sgc.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletExceptionHandler extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        
	    	final Throwable e = (Throwable) request.getAttribute("javax.servlet.error.exception");
	        log.error("Caught unhandled exception: " + e, e);

	        RequestDispatcher rd = request.getRequestDispatcher("/uncaughtException");
	       	request.setAttribute("exception", e);
	        rd.forward(request, response);
	    }
}
