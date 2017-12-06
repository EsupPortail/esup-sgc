package org.esupportail.sgc.services.fs;

import java.io.InputStream;

public interface AccessService {

	public abstract boolean putFile(String dir, String filename, InputStream inputStream, boolean override);

}