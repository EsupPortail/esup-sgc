package org.esupportail.sgc.tools;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyStore;

public class RestTemplateClientCertBuilder implements FactoryBean<RestTemplate> {

    String password;
    
    String certFile;
	
    public void setPassword(String password) {
		this.password = password;
	}

	public void setCertFile(String certFile) {
		this.certFile = certFile;
	}

	@Override
	public RestTemplate getObject() throws Exception {


        File fCertFile = ResourceUtils.getFile(certFile);

        KeyStore clientStore = KeyStore.getInstance("PKCS12");
                   clientStore.load(FileUtils.openInputStream(fCertFile), password.toCharArray());

        SSLContext sslContext = new SSLContextBuilder()
                .loadKeyMaterial(clientStore, password.toCharArray())
                .build();

        SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslConFactory)
                .build();

        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(cm)
                .build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
	}

	@Override
	public Class<?> getObjectType() {
		return RestTemplate.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
    
}
