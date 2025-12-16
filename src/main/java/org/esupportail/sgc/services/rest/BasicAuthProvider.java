package org.esupportail.sgc.services.rest;


import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Provider pour authentification Basic (username:password)
 */
public class BasicAuthProvider implements RestAuthProvider {

    private final String username;
    private final String password;

    public BasicAuthProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void configureHeaders(HttpHeaders headers) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        headers.set("Authorization", authHeader);
    }

    @Override
    public void renewToken() {
        // Pas de renouvellement pour Basic Auth
    }

    @Override
    public boolean supportsRenewal() {
        return false;
    }
}