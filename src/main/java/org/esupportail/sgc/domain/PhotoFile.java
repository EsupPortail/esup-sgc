package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class PhotoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    private String filename;

    @Transient
    private String imageData;

    @Transient
    private MultipartFile file;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime sendTime;

    private Long fileSize;

    private String contentType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "big_file", nullable = false)
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

	public String getFilename() {
        return this.filename;
    }

	public void setFilename(String filename) {
        this.filename = filename;
    }

	public String getImageData() {
        return this.imageData;
    }

	public void setImageData(String imageData) {
        this.imageData = imageData;
    }

	public MultipartFile getFile() {
        return this.file;
    }

	public void setFile(MultipartFile file) {
        this.file = file;
    }

	public LocalDateTime getSendTime() {
        return this.sendTime;
    }

	public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

	public Long getFileSize() {
        return this.fileSize;
    }

	public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

	public String getContentType() {
        return this.contentType;
    }

	public void setContentType(String contentType) {
        this.contentType = contentType;
    }

	public BigFile getBigFile() {
        return this.bigFile;
    }

	public void setBigFile(BigFile bigFile) {
        this.bigFile = bigFile;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("bigFile", "file").toString();
    }
}

