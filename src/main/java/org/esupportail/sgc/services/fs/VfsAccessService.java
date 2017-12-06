package org.esupportail.sgc.services.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.FileCopyUtils;

public class VfsAccessService implements DisposableBean, AccessService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected String uri;

	protected FileSystemManager fsManager;

	protected FileObject root;

	public void setUri(String uri) {
		this.uri = uri;
	}

	protected void open() throws FileSystemException {

		if(root == null) {
			FileSystemOptions fsOptions = new FileSystemOptions();

			fsManager = VFS.getManager();
			root = fsManager.resolveFile(uri, fsOptions);
		}

	}

	protected void close() {
		FileSystem fs = null;
		if(this.root != null) {
			fs = this.root.getFileSystem();
			this.fsManager.closeFileSystem(fs);
			this.root = null;
		}
	}

	public void destroy() throws Exception {
		this.close();
	}


	@Override
	public boolean putFile(String dir, String filename, InputStream inputStream, boolean override) {

		boolean success = false;
		FileObject newFile = null;

		try {
			this.open();
			FileObject folder = root;
			if(dir != null) {
				folder = root.resolveFile(dir);
			}
			newFile = folder.resolveFile(filename);
			if(override && newFile.exists()) {
				newFile.delete();
			}
			if(!newFile.exists()) {
				newFile.createFile();
				OutputStream outstr = newFile.getContent().getOutputStream();
				FileCopyUtils.copy(inputStream, outstr);
				success = true;
			} else {
				log.warn("Fichier " + filename + " existe déjà et pas d'override de demandé");
			}
		} catch (FileSystemException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		} catch (IOException e) {
			log.warn("can't upload file : " + e.getMessage(), e);
		}

		return success;

	}


}
