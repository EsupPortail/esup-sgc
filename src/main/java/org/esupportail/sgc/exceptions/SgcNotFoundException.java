package org.esupportail.sgc.exceptions;

public class SgcNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SgcNotFoundException(String message, Exception e) {
		super(message, e);
	}

}
