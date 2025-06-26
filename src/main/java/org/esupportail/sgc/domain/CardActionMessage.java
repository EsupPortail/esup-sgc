package org.esupportail.sgc.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.sgc.domain.Card.Etat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CardActionMessage {

    private static final Logger log = LoggerFactory.getLogger(CardActionMessage.class);

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

    @Column
    @Enumerated(EnumType.STRING)
    Etat etatInitial;

    @Column
    @Enumerated(EnumType.STRING)
    Etat etatFinal;

    @Column(columnDefinition="TEXT")
    private String message;

    @Column
    private boolean auto;

    @Column
    private boolean defaut;

    @Column(columnDefinition="TEXT")
    private String mailTo;

    @Column
    @ElementCollection(targetClass=String.class)
    @CollectionTable(
            name = "card_action_message_user_types",   // nom de la table jointe
            joinColumns = @JoinColumn(name = "card_action_message")  // nom exact de la colonne FK dans la table
    )
    private Set<String> userTypes = new HashSet<String>();

    @Column(name = "date_delay4prevent_caduc")
    Integer dateDelay4PreventCaduc = null;

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

    public Etat getEtatInitial() {
        return this.etatInitial;
    }

    public void setEtatInitial(Etat etatInitial) {
        this.etatInitial = etatInitial;
    }

    public Etat getEtatFinal() {
        return this.etatFinal;
    }

    public void setEtatFinal(Etat etatFinal) {
        this.etatFinal = etatFinal;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAuto() {
        return this.auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isDefaut() {
        return this.defaut;
    }

    public void setDefaut(boolean defaut) {
        this.defaut = defaut;
    }

    public String getMailTo() {
        return this.mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public Set<String> getUserTypes() {
        return this.userTypes;
    }

    public void setUserTypes(Set<String> userTypes) {
        this.userTypes = userTypes;
    }

    public Integer getDateDelay4PreventCaduc() {
        return this.dateDelay4PreventCaduc;
    }

    public void setDateDelay4PreventCaduc(Integer dateDelay4PreventCaduc) {
        this.dateDelay4PreventCaduc = dateDelay4PreventCaduc;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("message").toString();
    }

}
