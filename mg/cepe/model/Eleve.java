package mg.cepe.model;

import java.util.Objects;

public class Eleve {
    private String numEleve;
    private String numEcole;
    private String nom;
    private String prenom;

    public Eleve() { }

    public Eleve(String numEleve, String numEcole, String nom, String prenom) {
        this.numEleve = numEleve;
        this.numEcole = numEcole;
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getNumEleve() { return numEleve; }
    public void setNumEleve(String numEleve) { this.numEleve = numEleve; }
    public String getNumEcole() { return numEcole; }
    public void setNumEcole(String numEcole) { this.numEcole = numEcole; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNomComplet() { return nom + " " + prenom; }

    @Override
    public String toString() { return numEleve + " - " + nom + " " + prenom; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Eleve)) return false;
        Eleve eleve = (Eleve) o;
        return Objects.equals(numEleve, eleve.numEleve);
    }

    @Override
    public int hashCode() { return Objects.hash(numEleve); }
}
