package org.esupportail.sgc.services.userinfos;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.esupportail.sgc.services.rest.RequireRestAuth;
import org.esupportail.sgc.services.rest.RestAuthProvider;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestUserInfoService implements ExtUserInfoService {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(RestUserInfoService.class);

    RestTemplate restTemplate = new RestTemplate();

    String url;

    RestAuthProvider authProvider;

    Map<String, String> sgcParam2jsonPath;

    Long order = Long.valueOf(0);

    String eppnFilter = ".*";

    String spelFilter;

    String beanName;

    Map<String, List<String>> headers;

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

    public String getSpelFilter() {
        return spelFilter;
    }

    public void setSpelFilter(String spelFilter) {
        this.spelFilter = spelFilter;
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

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
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
            if(this.headers != null) {
                headers.addAll(CollectionUtils.toMultiValueMap(this.headers));
            }
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response;
            try {
                response = restTemplate.exchange(urlWithParams, HttpMethod.GET, entity, byte[].class);
            } catch(HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    // RequireRestAuth -> barer will be renewed and request retried
                    throw e;
                }
                throw  new SgcRuntimeException("Error when calling RestUserInfoService for user " + user.getEppn() + " at URL " + urlWithParams + " : " + e.getResponseBodyAsString(), e);
            }

            if(response.getHeaders().getContentType() != null && response.getHeaders().getContentType().toString().startsWith("image/")) {
                byte[] imageAsBytes = response.getBody();
                String imageAsBase64 = Base64.getEncoder().encodeToString(imageAsBytes);
                userInfos.put( sgcParam2jsonPath.entrySet().stream().findFirst().orElseThrow(() -> new SgcRuntimeException("No mapping defined for image content in RestUserInfoService " + beanName, null)).getKey(), imageAsBase64);
            } else {
                String json =  new String(response.getBody());
                log.trace("RestUserInfoService response for user {}: {}", user.getEppn(), json);
                DocumentContext jsonContext = JsonPath.parse(json);
                for (Map.Entry<String, String> mapping : sgcParam2jsonPath.entrySet()) {
                    try {
                        Object value = jsonContext.read(mapping.getValue());
                        if(value != null) {
                            if(value instanceof String) {
                                userInfos.put(mapping.getKey(), (String) value);
                            } else if(value instanceof List) {
                                List<String> valueList = (List<String>) value;
                                userInfos.put(mapping.getKey(), StringUtils.join(valueList, ","));
                            }
                        }
                    } catch (PathNotFoundException e) {
                        log.trace("JSON path not found for user {}: {}", user.getEppn(), mapping.getValue());
                    }
                }
            }
        }
        log.trace("RestUserInfoService found for user {}: {}", user.getEppn(), userInfos);
        return userInfos;
    }

    String buildUrl(User user, HttpServletRequest request, Map<String, String> userInfosInComputing) {
        String urlWithParams = url.replace("{eppn}", user.getEppn());
        if(userInfosInComputing != null) {
            for (String key : userInfosInComputing.keySet()) {
                if (urlWithParams.contains("{" + key + "}")) {
                    if (userInfosInComputing.get(key) == null) {
                        log.info("User info {} is null for user {} - abort", key, user.getEppn());
                        return null;
                    }
                    urlWithParams = urlWithParams.replace("{" + key + "}", userInfosInComputing.get(key));
                }
            }
        }
        return urlWithParams;
    }
}

