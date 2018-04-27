package org.esupportail.sgc.exceptions;

public class CrousAccountForbiddenException extends SgcRuntimeException {

	private static final long serialVersionUID = 1L;

	public CrousAccountForbiddenException(String message, Exception e) {
		super(message, e);
	}

}
