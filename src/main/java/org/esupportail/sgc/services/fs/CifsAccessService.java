package org.esupportail.sgc.services.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class CifsAccessService implements AccessService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected String uri;
	
	protected String domain;
	
	protected String username;
	
	protected String password;
	
	private NtlmPasswordAuthentication userAuthenticator;

	protected SmbFile root;

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	protected void open() throws MalformedURLException {

		if(root == null) {
			userAuthenticator = new NtlmPasswordAuthentication(this.domain, this.username, this.password) ;
			root = new SmbFile(this.uri, userAuthenticator);
		}

	}


	@Override
	public boolean putFile(String dir, String filename, InputStream inputStream, boolean override) {

		boolean success = false;
		SmbFile newFile = null;
		
		try {
			this.open();

			SmbFile folder = root;
			if (dir != null && dir.length() != 0) {
				folder = new SmbFile(this.uri + dir, userAuthenticator);
			}

			newFile = new SmbFile(folder.getCanonicalPath() + filename, this.userAuthenticator);
			if(override && newFile.exists()) {
				newFile.delete();
			}
			if(!newFile.exists()) {
				newFile.createNewFile();
	
				OutputStream outstr = newFile.getOutputStream();
	
				FileCopyUtils.copy(inputStream, outstr);
	
				success = true;
			} else {
				log.warn("Fichier " + filename + " existe déjà et pas d'override de demandé");
			}
			
		} catch (SmbException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		} catch (IOException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		}

		return success;

	}


}
