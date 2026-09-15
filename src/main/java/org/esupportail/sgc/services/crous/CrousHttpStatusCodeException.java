package org.esupportail.sgc.services.crous;
import org.esupportail.sgc.services.crous.CrousErrorLog.CrousOperation;
import org.esupportail.sgc.services.crous.CrousErrorLog.EsupSgcOperation;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class CrousHttpStatusCodeException extends Exception {

	private static final long serialVersionUID = 1L;

	HttpStatusCodeException httpStatusCodeException;
	/**
	 * Message d'erreur "métier" utilisé lorsque cette exception n'est pas issue d'un appel HTTP
	 * vers l'API CROUS mais d'un contrôle/contrainte réalisé en amont par esup-sgc.
	 * Dans ce cas, httpStatusCodeException reste null.
	 */
	String errorMessage;

	String eppn;

	String csn;

	CrousOperation crousOperation;

	EsupSgcOperation esupSgcOperation;

	String crousUrl;

	Boolean blocking = false;

	public CrousHttpStatusCodeException(HttpStatusCodeException httpStatusCodeException, String eppn, String csn,
										CrousOperation crousOperation, EsupSgcOperation esupSgcOperation, String crousUrl) {
		super(httpStatusCodeException);
		this.httpStatusCodeException = httpStatusCodeException;
		this.eppn = eppn;
		this.csn = csn;
		this.crousOperation = crousOperation;
		this.esupSgcOperation = esupSgcOperation;
		this.crousUrl = crousUrl;
	}
	/**
	 * Constructeur pour une erreur "métier" levée par esup-sgc lui-même (contrôle/contrainte préalable),
	 * sans qu'il n'y ait eu d'appel HTTP vers l'API CROUS en échec.
	 */
	public CrousHttpStatusCodeException(String errorMessage, String eppn, String csn,
										CrousOperation crousOperation, EsupSgcOperation esupSgcOperation, String crousUrl) {
		super(errorMessage);
		this.errorMessage = errorMessage;
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
	/**
	 * Renvoie le corps d'erreur brut renvoyé par l'API CROUS, ou si cette exception a été levée
	 * suite à un contrôle interne (pas d'appel HTTP), un JSON généré au même format
	 * ({"errors":[{"code":..., "message":..., "field":null}]}) afin de rester compatible avec
	 * CrousLogService#logErrorCrous.
	 */
	public String getErrorBodyAsJson() {
		if (httpStatusCodeException != null) {
			return httpStatusCodeException.getResponseBodyAsString();
		}
		String message = errorMessage != null ? errorMessage : getMessage();
		if (message == null) {
			message = "";
		}
		String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
		return String.format("{\"errors\":[{\"code\":\"ESUP-SGC\",\"message\":\"%s\",\"field\":null}]}", escapedMessage);
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
		return "CrousHttpStatusCodeException [eppn=" + eppn + ", csn=" + csn + ", errorBodyAsJson=" + getErrorBodyAsJson()
				+ "]";
	}
	/**
	 * Renvoie null si l'exception a été levée suite à un contrôle interne (pas d'appel HTTP réel).
	 */
	public HttpStatusCode getStatusCode() {
		return httpStatusCodeException != null ? httpStatusCodeException.getStatusCode() : null;
	}
	/**
	 * Peut renvoyer null si l'exception a été levée suite à un contrôle interne (pas d'appel HTTP réel).
	 */
	public HttpStatusCodeException getHttpStatusCodeException() {
		return httpStatusCodeException;
	}
}
