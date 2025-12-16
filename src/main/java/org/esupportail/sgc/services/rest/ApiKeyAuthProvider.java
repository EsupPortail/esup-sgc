package org.esupportail.sgc.services.rest;


// ============================================================================
// API Key Provider
// ============================================================================

import org.springframework.http.HttpHeaders;

/**
 * Provider pour authentification par API Key
 */
public class ApiKeyAuthProvider implements RestAuthProvider {

    private final String headerName;
    private final String apiKey;

    public ApiKeyAuthProvider(String headerName, String apiKey) {
        this.headerName = headerName;
        this.apiKey = apiKey;
    }

    @Override
    public void configureHeaders(HttpHeaders headers) {
        headers.set(headerName, apiKey);
    }

    @Override
    public void renewToken() {
        // Pas de renouvellement pour API Key
    }

    @Override
    public boolean supportsRenewal() {
        return false;
    }
}
