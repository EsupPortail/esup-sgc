package org.esupportail.sgc.web.wsrest;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public class AbstractRestController {
	
	public final Logger log = LoggerFactory.getLogger(getClass());
	
	  /**
     * Catch all for any other exceptions...
     */
    @ExceptionHandler()
    @ResponseBody
    public ResponseEntity<?> handleAnyException(Exception e) {
        return errorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle failures commonly thrown from code
     */
    @ExceptionHandler({ InvocationTargetException.class, IllegalArgumentException.class, ClassCastException.class,
            ConversionFailedException.class })
    @ResponseBody
    public ResponseEntity handleMiscFailures(Exception e) {
        return errorResponse(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * Send a 409 Conflict in case of concurrent modification
     */
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class,
            DataIntegrityViolationException.class })
    @ResponseBody
    public ResponseEntity handleConflict(Exception ex) {
        return errorResponse(ex, HttpStatus.CONFLICT);
    }

    protected ResponseEntity<String> errorResponse(Exception e, HttpStatus status) {
        if (e != null && e.getMessage() != null) {
            log.error("error caught: " + e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), new HttpHeaders(), status);
        } else {
            log.error("unknown error caught in RESTController, {}", e);
            return new ResponseEntity<String>("Erreur interne", new HttpHeaders(), status);
        }
    }
    
}
