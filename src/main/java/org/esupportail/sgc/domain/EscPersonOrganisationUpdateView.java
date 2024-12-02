package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EscPersonOrganisationUpdateView {

    public enum AcademicLevelEnum {BACHELOR, MASTER, DOCTORATE}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    AcademicLevelEnum academicLevel;

    String email;

    String organisationIdentifier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AcademicLevelEnum getAcademicLevel() {
        return academicLevel;
    }

    public void setAcademicLevel(AcademicLevelEnum academicLevel) {
        this.academicLevel = academicLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public void setOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }

    @Override
    public String toString() {
        return "EscPersonOrganisationUpdateView{" +
                "id=" + id +
                ", academicLevel=" + academicLevel +
                ", email='" + email + '\'' +
                ", organisationIdentifier='" + organisationIdentifier + '\'' +
                '}';
    }

}
