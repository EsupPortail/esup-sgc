package org.esupportail.sgc.exceptions;

public class SgcRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SgcRuntimeException(String message, Exception e) {
		super(message, e);
	}

}
