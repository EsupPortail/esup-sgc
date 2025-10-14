package org.esupportail.sgc.services.crous;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/*
 * Aspect to handle CROUS API authentication.
 * If a method annotated with @RequireCrousAuth throws a 401 error,
 * the aspect will call ApiCrousService.authenticate() and retry the method.
 */
@Aspect
@Component
public class CrousAuthAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    private final ApiCrousService apiCrousService;

    public CrousAuthAspect(ApiCrousService apiCrousService) {
        this.apiCrousService = apiCrousService;
    }

    @Around("@annotation(RequireCrousAuth)")
    public Object handleAuth(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (HttpClientErrorException|CrousHttpClientErrorException e) {
            HttpClientErrorException he = null;
            if(e instanceof CrousHttpClientErrorException) {
                he = ((CrousHttpClientErrorException) e).getHttpClientErrorException();
            } else if(e instanceof HttpClientErrorException) {
                he = (HttpClientErrorException) e;
            }
            if (he.getStatusCode() == HttpStatus.UNAUTHORIZED || he.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.info("Auth Token of Crous API should be renew, we call an authentication");
                apiCrousService.authenticate();
                return joinPoint.proceed();
            } else {
                throw e;
            }
        }
    }
}
