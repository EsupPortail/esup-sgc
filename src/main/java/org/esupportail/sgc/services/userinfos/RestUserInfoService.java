package org.esupportail.sgc.services.userinfos;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.services.rest.RequireRestAuth;
import org.esupportail.sgc.services.rest.RestAuthProvider;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class RestUserInfoService implements ExtUserInfoService {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(RestUserInfoService.class);

    RestTemplate restTemplate = new RestTemplate();

    String url;

    RestAuthProvider authProvider;


    Map<String, String> sgcParam2jsonPath;

    Long order = Long.valueOf(0);

    String eppnFilter = ".*";

    String beanName;

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getEppnFilter() {
        return eppnFilter;
    }

    public void setEppnFilter(String eppnFilter) {
        this.eppnFilter = eppnFilter;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setAuthProvider(RestAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSgcParam2jsonPath(Map<String, String> sgcParam2jsonPath) {
        this.sgcParam2jsonPath = sgcParam2jsonPath;
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    @RequireRestAuth
    public Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {
        Map<String, String> userInfos = new HashMap<>();

        String urlWithParams = buildUrl(user, request, userInfosInComputing);
        log.trace("RestUserInfoService URL for user {}: {}", user.getEppn(), urlWithParams);

        if(urlWithParams != null) {
            HttpHeaders headers = new HttpHeaders();
            authProvider.configureHeaders(headers);
            HttpEntity entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(urlWithParams, org.springframework.http.HttpMethod.GET, entity, String.class);
            log.trace("RestUserInfoService response for user {}: {}", user.getEppn(), response.getBody());

            DocumentContext jsonContext = JsonPath.parse(response.getBody());

            for (Map.Entry<String, String> mapping : sgcParam2jsonPath.entrySet()) {
                try {
                    Object value = jsonContext.read(mapping.getValue());
                    userInfos.put(mapping.getKey(), String.valueOf(value));
                } catch (PathNotFoundException e) {
                    log.trace("JSON path not found for user {}: {}", user.getEppn(), mapping.getValue());
                }
            }
        }
        log.trace("RestUserInfoService found for user {}: {}", user.getEppn(), userInfos);
        return userInfos;
    }

    String buildUrl(User user, HttpServletRequest request, Map<String, String> userInfosInComputing) {
        String urlWithParams = url.replace("{eppn}", user.getEppn());
        for (String key : userInfosInComputing.keySet()) {
            if (urlWithParams.contains("{" + key + "}")) {
                if(userInfosInComputing.get(key) == null) {
                    log.info("User info {} is null for user {} - abort", key, user.getEppn());
                    return null;
                }
                urlWithParams = urlWithParams.replace("{" + key + "}", userInfosInComputing.get(key));
            }
        }
        return urlWithParams;
    }
}

