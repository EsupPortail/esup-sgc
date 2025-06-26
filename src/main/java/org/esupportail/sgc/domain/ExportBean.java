package org.esupportail.sgc.domain;

public class ExportBean {

    String editable;
    String nom;
    String prenom;
    String email;
    String adresse;
    String nombre;

    String type = "0";
    String contractuel = "0";
    String etudiant = "0";
    String formationContinue = "0";
    String apprentissage = "0";
    String heberge = "0";
    String passager = "0";
    String personnel = "0";
    String personnelOuvrier = "0";
    String stagiaire = "0";
    int total = 0;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getContractuel() {
        return contractuel;
    }
    public void setContractuel(String contractuel) {
        this.contractuel = contractuel;
    }
    public String getEtudiant() {
        return etudiant;
    }
    public void setEtudiant(String etudiant) {
        this.etudiant = etudiant;
    }
    public String getFormationContinue() {
        return formationContinue;
    }
    public void setFormationContinue(String formationContinue) {
        this.formationContinue = formationContinue;
    }
    public String getApprentissage() {
        return apprentissage;
    }
    public void setApprentissage(String apprentissage) {
        this.apprentissage = apprentissage;
    }
    public String getHeberge() {
        return heberge;
    }
    public void setHeberge(String heberge) {
        this.heberge = heberge;
    }
    public String getPassager() {
        return passager;
    }
    public void setPassager(String passager) {
        this.passager = passager;
    }
    public String getPersonnel() {
        return personnel;
    }
    public void setPersonnel(String personnel) {
        this.personnel = personnel;
    }
    public String getPersonnelOuvrier() {
        return personnelOuvrier;
    }
    public void setPersonnelOuvrier(String personnelOuvrier) {
        this.personnelOuvrier = personnelOuvrier;
    }
    public String getStagiaire() {
        return stagiaire;
    }
    public void setStagiaire(String stagiaire) {
        this.stagiaire = stagiaire;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public String getAdresse() {
        return adresse;
    }
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getEditable() {
        return editable;
    }
    public void setEditable(String editable) {
        this.editable = editable;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}
