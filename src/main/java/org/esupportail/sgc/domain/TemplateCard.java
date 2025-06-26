package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "template_card")
public class TemplateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
    @Column(name = "id")
    private Long id;

    @Column
    @NotEmpty
    private String key;

    @Column
    private String name = "";
    
    @Column
    private int numVersion = 0;

    @Column(columnDefinition = "TEXT")
    private String cssStyle;
    
    @Column(columnDefinition = "TEXT")
    private String cssMobileStyle;

    @Column(columnDefinition = "TEXT")
    private String cssBackStyle;

    @Column
    private Boolean backSupported = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String modificateur;
    
    @Column
    private Boolean codeBarres= false;

    @Column
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dateModification;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "photo_file_logo", nullable = false)
    private PhotoFile photoFileLogo = new PhotoFile();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "photo_file_masque", nullable = false)
    private PhotoFile photoFileMasque = new PhotoFile();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "photo_file_qr_code", nullable = false)
    private PhotoFile photoFileQrCode = new PhotoFile();

    @Transient
    private MultipartFile masque;

    @Transient
    private MultipartFile logo;

    @Transient
    private MultipartFile qrCode;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public String getKey() {
        return this.key;
    }

	public void setKey(String key) {
        this.key = key;
    }

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public int getNumVersion() {
        return this.numVersion;
    }

	public void setNumVersion(int numVersion) {
        this.numVersion = numVersion;
    }

	public String getCssStyle() {
        return this.cssStyle;
    }

	public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }

	public String getCssMobileStyle() {
        return this.cssMobileStyle;
    }

	public void setCssMobileStyle(String cssMobileStyle) {
        this.cssMobileStyle = cssMobileStyle;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public String getModificateur() {
        return this.modificateur;
    }

	public void setModificateur(String modificateur) {
        this.modificateur = modificateur;
    }

	public Boolean getCodeBarres() {
        return this.codeBarres;
    }

	public void setCodeBarres(Boolean codeBarres) {
        this.codeBarres = codeBarres;
    }

	public LocalDateTime getDateModification() {
        return this.dateModification;
    }

	public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

	public PhotoFile getPhotoFileLogo() {
        return this.photoFileLogo;
    }

	public void setPhotoFileLogo(PhotoFile photoFileLogo) {
        this.photoFileLogo = photoFileLogo;
    }

	public PhotoFile getPhotoFileMasque() {
        return this.photoFileMasque;
    }

	public void setPhotoFileMasque(PhotoFile photoFileMasque) {
        this.photoFileMasque = photoFileMasque;
    }

	public PhotoFile getPhotoFileQrCode() {
        return this.photoFileQrCode;
    }

	public void setPhotoFileQrCode(PhotoFile photoFileQrCode) {
        this.photoFileQrCode = photoFileQrCode;
    }

	public MultipartFile getMasque() {
        return this.masque;
    }

	public void setMasque(MultipartFile masque) {
        this.masque = masque;
    }

	public MultipartFile getLogo() {
        return this.logo;
    }

	public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }

	public MultipartFile getQrCode() {
        return this.qrCode;
    }

	public void setQrCode(MultipartFile qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isCodeBarres() {
        return this.codeBarres!=null && this.codeBarres;
    }

    public String getCssBackStyle() {
        return cssBackStyle;
    }

    public void setCssBackStyle(String cssBackStyle) {
        this.cssBackStyle = cssBackStyle;
    }

    public Boolean getBackSupported() {
        return backSupported;
    }

    public void setBackSupported(Boolean backSupported) {
        this.backSupported = backSupported;
    }

    public String toString() {
        return String.format("%s / V%d", this.getName(), this.getNumVersion());
    }

}
