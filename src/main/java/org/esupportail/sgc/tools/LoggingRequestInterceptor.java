package org.esupportail.sgc.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    final static Logger log = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        //traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        log.trace("===========================request begin================================================");
        log.trace("URI         : {}", request.getURI());
        log.trace("Method      : {}", request.getMethod());
        log.trace("Headers     : {}", request.getHeaders() );
        log.trace("Request body: {}", new String(body, "UTF-8"));
        log.trace("==========================request end================================================");
    }
    
    /**
     * Don't use it : it uses the response.getBody() stream and jackson can't read anymore :-( 
     */
    private void traceResponse(ClientHttpResponse response) throws IOException {
        log.trace("===========================response begin================================================");
        log.trace("StatusCode      : {}", response.getStatusCode());
        log.trace("Headers     : {}", response.getHeaders() );
        CloseShieldInputStream csis = new CloseShieldInputStream(response.getBody());
	    StringWriter writer = new StringWriter();
	    String encoding = StandardCharsets.UTF_8.name();
	    IOUtils.copy(csis, writer, encoding);
        log.trace("Response body: {}", writer.toString());
        log.trace("==========================response end================================================");
    }  	
    
}

