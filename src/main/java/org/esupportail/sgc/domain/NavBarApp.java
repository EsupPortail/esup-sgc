package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;

@Entity
public class NavBarApp {
	
	public enum VisibleRole {
        CONSULT,
        MANAGER,
        UPDATER,
        VERSO,
        LIVREUR
	}

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

    private String title;

    private String url;

    private String icon;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column
    @CollectionTable(
            name = "nav_bar_app_visible4role",   // nom de la table jointe
            joinColumns = @JoinColumn(name = "nav_bar_app")  // nom exact de la colonne FK dans la table
    )
    private Collection<VisibleRole> visible4role;

    private Integer index;


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

	public String getTitle() {
        return this.title;
    }

	public void setTitle(String title) {
        this.title = title;
    }

	public String getUrl() {
        return this.url;
    }

	public void setUrl(String url) {
        this.url = url;
    }

	public String getIcon() {
        return this.icon;
    }

	public void setIcon(String icon) {
        this.icon = icon;
    }

	public Collection<VisibleRole> getVisible4role() {
        return this.visible4role;
    }

	public void setVisible4role(Collection<VisibleRole> visible4role) {
        this.visible4role = visible4role;
    }

	public Integer getIndex() {
        return this.index;
    }

	public void setIndex(Integer index) {
        this.index = index;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
