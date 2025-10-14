package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

@Entity
public class BigFile {

    private final static Logger log = LoggerFactory.getLogger(BigFile.class);

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

    //@Lob
    //@Type(type="org.hibernate.type.PrimitiveByteArrayBlobType")
    @Basic(fetch = FetchType.LAZY)
    private Blob binaryFile;

    private String md5;

    public void updateMd5() {
        try {
            this.md5 = DigestUtils.md5Hex(binaryFile.getBinaryStream());
        } catch (IOException | SQLException e) {
            log.error("updateMd5 error on this file", e);
        }
    }

    public byte[] getBinaryFileasBytes() throws IOException, SQLException {
        Blob blob = this.getBinaryFile();
        if(blob==null) return null;
        return IOUtils.toByteArray(blob.getBinaryStream());
    }

    public Blob getBinaryFile() {
        return this.binaryFile;
    }

    public void setBinaryFile(Blob binaryFile) {
        this.binaryFile = binaryFile;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
