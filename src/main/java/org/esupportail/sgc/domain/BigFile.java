package org.esupportail.sgc.domain;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Basic;
import javax.persistence.FetchType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.exceptions.SgcRuntimeException;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.classpath.operations.jsr303.RooUploadedFile;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class BigFile {
	
	private final static Logger log = LoggerFactory.getLogger(BigFile.class);
	
	//@Lob
	//@Type(type="org.hibernate.type.PrimitiveByteArrayBlobType")
	@RooUploadedFile(contentType = "application/zip")
	@Basic(fetch = FetchType.LAZY)
	private Blob binaryFile;
	
	private String md5;
	
    public void setBinaryFileStream(InputStream inputStream, long length) {		
        if (this.entityManager == null) this.entityManager = entityManager();
        byte[] buffer = new byte[(int)length];
        try {
			IOUtils.readFully(inputStream, buffer);
	        this.setBinaryFile(buffer);
		} catch (IOException e) {
			throw new SgcRuntimeException("Error saving binary file", e);
		}
    }
    
    public void updateMd5() {
    	try {
			this.md5 = DigestUtils.md5Hex(binaryFile.getBinaryStream());
		} catch (IOException | SQLException e) {
			log.error("updateMd5 error on this file", e);
		}
    	
	}

	public void setBinaryFile(byte[] bytes) {		
        if (this.entityManager == null) this.entityManager = entityManager();
        Session session = (Session) this.entityManager.getDelegate(); 
        LobHelper helper = session.getLobHelper(); 
        this.binaryFile = helper.createBlob(bytes); 
        this.updateMd5();
    }
    
    public byte[] getBinaryFileasBytes() throws IOException, SQLException {		
        Blob blob = this.getBinaryFile();
        if(blob==null) return null;
        return IOUtils.toByteArray(blob.getBinaryStream());
    }
    
}
