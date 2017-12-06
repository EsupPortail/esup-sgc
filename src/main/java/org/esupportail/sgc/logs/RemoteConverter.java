package org.esupportail.sgc.logs;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class RemoteConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
    	try{
	    	String remoteAddress = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
	    			.getRequest().getRemoteAddr();
	    	if (remoteAddress != null) {
	            return remoteAddress;
	    	}	
    	} catch(IllegalStateException e){
    		// we can't log here with logback logger : RemoteConverter is used in the logger
    		// but if IllegalStateException - that's because logback is just used outsite web request 
    	} catch(Exception e){
    		// we can't log here with logback logger : RemoteConverter is used in the logger
			e.printStackTrace();
    	}
    	return "NO_REMOTE_ADDRESS"; 
    }
}