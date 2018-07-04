package org.esupportail.sgc.domain;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Basic;
import javax.persistence.FetchType;

import org.apache.commons.io.IOUtils;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.classpath.operations.jsr303.RooUploadedFile;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class BigFile {
	
	//@Lob
	//@Type(type="org.hibernate.type.PrimitiveByteArrayBlobType")
	@RooUploadedFile(contentType = "application/zip")
	@Basic(fetch = FetchType.LAZY)
	private Blob binaryFile;
	
	// Here we have to use directly Hibernate and no "just" JPA, because we want to use stream to read and write big files
	// @see http://stackoverflow.com/questions/10042766/jpa-analog-of-lobcreator-from-hibernate
    public void setBinaryFileStream(InputStream inputStream, long length) {		
        if (this.entityManager == null) this.entityManager = entityManager();
        Session session = (Session) this.entityManager.getDelegate(); 
        LobHelper helper = session.getLobHelper(); 
        this.binaryFile = helper.createBlob(inputStream, length); 
    }
    
    public void setBinaryFile(byte[] bytes) {		
        if (this.entityManager == null) this.entityManager = entityManager();
        Session session = (Session) this.entityManager.getDelegate(); 
        LobHelper helper = session.getLobHelper(); 
        this.binaryFile = helper.createBlob(bytes); 
    }
    
    public byte[] getBinaryFileasBytes() throws IOException, SQLException {		
        Blob blob = this.getBinaryFile();
        return IOUtils.toByteArray(blob.getBinaryStream());
    }
    
}
