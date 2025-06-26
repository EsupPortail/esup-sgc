package org.esupportail.sgc.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"id", "eppn"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EscPerson {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(
        name = "my_seq",
        sequenceName = "hibernate_sequence",
        allocationSize = 1
)
	@Column(name = "id")
	private Long id;

	@Column(unique=true)
	String eppn;

	// Pas de contrainte d'unicité ici pour gérer le cas d'usage d'un esup-sgc multi-établissements
	// où on retrouverait le même étudiant (même ine) inscrit dans plusieurs établissements (eppn différents)
	@Column
	String identifier;

	String fullName;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="escr_person_id")
	List<EscPersonOrganisationUpdateView> personOrganisationUpdateViews = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<EscPersonOrganisationUpdateView> getPersonOrganisationUpdateViews() {
		return personOrganisationUpdateViews;
	}

	public void updateWith(EscPerson escPersonGoal) {
		this.setEppn(escPersonGoal.getEppn());
		this.setIdentifier(escPersonGoal.getIdentifier());
		this.setFullName(escPersonGoal.getFullName());
		EscPersonOrganisationUpdateView escPersonOrganisationUpdateViewGoal = escPersonGoal.getPersonOrganisationUpdateViews().get(0);
		if(escPersonOrganisationUpdateViewGoal != null) {
			for(EscPersonOrganisationUpdateView escPersonOrganisationUpdateView : this.getPersonOrganisationUpdateViews()) {
				if(escPersonOrganisationUpdateView.getOrganisationIdentifier().equals(escPersonOrganisationUpdateViewGoal.getOrganisationIdentifier())) {
					escPersonOrganisationUpdateView.setAcademicLevel(escPersonOrganisationUpdateViewGoal.getAcademicLevel());
					escPersonOrganisationUpdateView.setEmail(escPersonOrganisationUpdateViewGoal.getEmail());
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "EscPerson{" +
				"id=" + id +
				", eppn='" + eppn + '\'' +
				", identifier='" + identifier + '\'' +
				", personOrganisationUpdateViews=" + personOrganisationUpdateViews +
				'}';
	}
}
