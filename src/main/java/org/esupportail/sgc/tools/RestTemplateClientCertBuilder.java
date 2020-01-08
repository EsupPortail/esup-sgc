package org.esupportail.sgc.tools;

import java.io.File;
import java.security.KeyStore;

import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

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
		
		SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.useProtocol("TLS");
        sslContextBuilder.loadKeyMaterial(clientStore, password.toCharArray());
        sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(10000); // 10 seconds
        requestFactory.setReadTimeout(10000); // 10 seconds
        
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
