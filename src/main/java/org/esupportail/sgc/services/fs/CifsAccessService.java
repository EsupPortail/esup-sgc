package org.esupportail.sgc.services.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class CifsAccessService implements AccessService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected String uri;
	
	protected String domain;
	
	protected String username;
	
	protected String password;
	
	protected CIFSContext cifsContext;
	
	protected NtlmPasswordAuthentication userAuthenticator;

	protected SmbFile root;
	
	/** CIFS properties */
    protected Properties jcifsConfigProperties = new Properties();

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

	public void setJcifsConfigProperties(Properties jcifsConfigProperties) {
		this.jcifsConfigProperties = jcifsConfigProperties;
	}

	protected void open() throws MalformedURLException, CIFSException {
		if(root == null) {
			cifsContext = new BaseContext(new PropertyConfiguration(jcifsConfigProperties));
			userAuthenticator = new NtlmPasswordAuthentication(cifsContext, this.domain, this.username, this.password) ;
			cifsContext = cifsContext.withCredentials(userAuthenticator);
			root = new SmbFile(this.uri, cifsContext);
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
				folder = new SmbFile(this.uri + dir, cifsContext);
			} else {
				dir = "";
			}

			newFile = new SmbFile(folder.getCanonicalPath() + filename, this.cifsContext);
			if(override && newFile.exists()) {
				newFile.delete();
			}
			if(!newFile.exists()) {
				newFile.createNewFile();
				OutputStream outstr = newFile.getOutputStream();
				FileCopyUtils.copy(inputStream, outstr);	
				success = true;
				log.info("Fichier " + filename + " envoyé dans " + this.uri + dir);
			} else {
				log.warn("Fichier " + filename + " existe déjà dans " + this.uri + dir + " et pas d'override de demandé");
			}
			
		} catch (SmbException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		} catch (IOException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		}

		return success;

	}


}
