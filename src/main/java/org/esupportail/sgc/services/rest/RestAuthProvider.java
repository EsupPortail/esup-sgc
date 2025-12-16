package org.esupportail.sgc.services.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

/**
 * Interface pour les providers d'authentification REST
 */
public interface RestAuthProvider {

    /**
     * Configure les headers HTTP avec l'authentification
     * @param headers Headers HTTP à enrichir
     */
    void configureHeaders(HttpHeaders headers);

    /**
     * Renouvelle le token d'authentification
     * Appelé automatiquement en cas de 401
     */
    void renewToken();

    /**
     * Indique si ce provider supporte le renouvellement automatique
     */
    default boolean supportsRenewal() {
        return false;
    }
}