package org.esupportail.sgc.domain;

import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.web.multipart.MultipartFile;

@RooJavaBean
@RooToString(excludeFields = { "bigFile", "file" })
@RooJpaActiveRecord
public class PhotoFile {

    private String filename;
    
    @Transient
    private String imageData;

    @Transient
    private MultipartFile file;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date sendTime;

    private Long fileSize;

    private String contentType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private BigFile bigFile = new BigFile();

    @Transient
    public String getFileSizeFormatted() {
        return readableFileSize(fileSize.longValue());
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}

