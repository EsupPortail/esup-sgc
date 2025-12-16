package org.esupportail.sgc.services.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provider Bearer Token avec renouvellement automatique thread-safe
 */
public class BearerTokenAuthProvider implements RestAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenAuthProvider.class);

    RestTemplate restTemplate = new RestTemplate();

    String tokenEndpoint;

    Map<String, String> bodyCredentials;

    String tokenJsonPath = "$";

    // Token actuel (volatile pour visibilité entre threads)
    private volatile String currentToken;

    // Lock pour synchroniser le renouvellement
    private final ReentrantLock renewalLock = new ReentrantLock();

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public void setBodyCredentials(Map<String, String> bodyCredentials) {
        this.bodyCredentials = bodyCredentials;
    }

    public void setTokenJsonPath(String tokenJsonPath) {
        this.tokenJsonPath = tokenJsonPath;
    }

    @Override
    public void configureHeaders(HttpHeaders headers) {
        headers.setBearerAuth(currentToken);
    }

    @Override
    public void renewToken() {
        // Double-check locking pattern pour performance
        if (currentToken == null || needsRenewal()) {
            renewalLock.lock();
            try {
                // Re-vérifier après avoir acquis le lock (un autre thread a peut-être déjà renouvelé)
                if (currentToken == null || needsRenewal()) {
                    log.info("Renewing authentication token from {}", tokenEndpoint);
                    currentToken = fetchNewToken();
                    log.info("Token successfully renewed");
                }
            } finally {
                renewalLock.unlock();
            }
        }
    }

    @Override
    public boolean supportsRenewal() {
        return true;
    }

    /**
     * Détermine si le token a besoin d'être renouvelé
     * Dans cette implémentation, on renouvelle uniquement sur 401
     */
    private boolean needsRenewal() {
        // Retourne false car on renouvelle uniquement sur 401 via l'aspect
        return false;
    }

    /**
     * Récupère un nouveau token depuis l'endpoint OAuth2
     */
    String fetchNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : bodyCredentials.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenEndpoint,
                HttpMethod.POST,
                request,
                String.class
        );

        // Extraire le token de la réponse JSON
        DocumentContext jsonContext = JsonPath.parse(response.getBody());
        String token = jsonContext.read(tokenJsonPath);

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token not found in response at path: " + tokenJsonPath);
        }

        return token;

    }
}
