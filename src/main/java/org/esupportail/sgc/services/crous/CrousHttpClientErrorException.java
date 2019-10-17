package org.esupportail.sgc.services.crous;

import org.esupportail.sgc.services.crous.CrousErrorLog.CrousOperation;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.springframework.web.client.HttpClientErrorException;

public class CrousHttpClientErrorException extends Exception {
	
	private static final long serialVersionUID = 1L;

	HttpClientErrorException httpClientErrorException;
	
	String eppn;
	
	String csn;

	CrousOperation crousOperation;
	
	EsupSgcOperation esupSgcOperation;
	
	String crousUrl;
	
	Boolean blocking = false;

	public CrousHttpClientErrorException(HttpClientErrorException httpClientErrorException, String eppn, String csn,
			CrousOperation crousOperation, EsupSgcOperation esupSgcOperation, String crousUrl) {
		super(httpClientErrorException);
		this.httpClientErrorException = httpClientErrorException;
		this.eppn = eppn;
		this.csn = csn;
		this.crousOperation = crousOperation;
		this.esupSgcOperation = esupSgcOperation;
		this.crousUrl = crousUrl;
	}

	public String getEppn() {
		return eppn;
	}

	public String getCsn() {
		return csn;
	}

	public String getErrorBodyAsJson() {
		return httpClientErrorException.getResponseBodyAsString();
	}

	public CrousOperation getCrousOperation() {
		return crousOperation;
	}

	public EsupSgcOperation getEsupSgcOperation() {
		return esupSgcOperation;
	}

	public String getCrousUrl() {
		return crousUrl;
	}

	public Boolean getBlocking() {
		return blocking;
	}

	public void setBlocking(Boolean blocking) {
		this.blocking = blocking;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public void setEsupSgcOperation(EsupSgcOperation esupSgcOperation) {
		this.esupSgcOperation = esupSgcOperation;
	}

	@Override
	public String toString() {
		return "CrousHttpClientErrorException [eppn=" + eppn + ", csn=" + csn + ", errorBodyAsJson=" + httpClientErrorException.getResponseBodyAsString()
				+ "]";
	}

	public Object getStatusCode() {
		return httpClientErrorException.getStatusCode();
	}
	
}
