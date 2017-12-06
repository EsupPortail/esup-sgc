package org.esupportail.sgc.exceptions;

public class CrousAccountLockException extends SgcRuntimeException {

	private static final long serialVersionUID = 1L;

	public CrousAccountLockException(String message, Exception e) {
		super(message, e);
	}

}
