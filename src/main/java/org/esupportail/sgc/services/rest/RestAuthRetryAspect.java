package org.esupportail.sgc.services.rest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Aspect AOP pour intercepter les erreurs 401 et renouveler automatiquement le token
 */
@Aspect
@Component
public class RestAuthRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RestAuthRetryAspect.class);

    @Around("@annotation(RequireRestAuth) && target(service)")
    public Object handleAuthenticationRetry(ProceedingJoinPoint joinPoint, Object service) throws Throwable {

        RestAuthProvider authProvider = getAuthProvider(service);

        if (authProvider == null || !authProvider.supportsRenewal()) {
            // Pas de renouvellement supporté, exécution normale
            return joinPoint.proceed();
        }

        try {
            // Première tentative
            return joinPoint.proceed();

        } catch (HttpClientErrorException e) {

            // Vérifier si c'est une erreur 401
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("Received 401 Unauthorized, attempting token renewal");

                authProvider.renewToken();

                // Réessayer l'appel avec le nouveau token
                try {
                    log.info("Retrying request with renewed token");
                    return joinPoint.proceed();

                } catch (HttpClientErrorException retryException) {
                    if (retryException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        log.error("Still receiving 401 after token renewal, giving up");
                        throw new RuntimeException("Authentication failed even after token renewal", retryException);
                    }
                    throw retryException;
                }

            } else {
                // Autre erreur HTTP, on la propage
                throw e;
            }
        }
    }

    /**
     * Extrait le RestAuthProvider du service via réflexion
     */
    private RestAuthProvider getAuthProvider(Object service) {
        try {
            java.lang.reflect.Field field = service.getClass().getDeclaredField("authProvider");
            field.setAccessible(true);
            return (RestAuthProvider) field.get(service);
        } catch (Exception e) {
            log.debug("Could not access authProvider field", e);
            return null;
        }
    }
}