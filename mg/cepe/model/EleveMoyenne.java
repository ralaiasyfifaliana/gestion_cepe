package mg.cepe.model;

/**
 * DTO représentant la moyenne pondérée d'un élève pour une année scolaire :
 * utilisé pour la délibération, réussite/échec, admission 6e, classement.
 */
public class EleveMoyenne {
    private String numEleve;
    private String nom;
    private String prenom;
    private double moyenne;
    private int rang;

    public EleveMoyenne() { }

    public EleveMoyenne(String numEleve, String nom, String prenom, double moyenne) {
        this.numEleve = numEleve;
        this.nom = nom;
        this.prenom = prenom;
        this.moyenne = moyenne;
    }

    public String getNumEleve() { return numEleve; }
    public void setNumEleve(String numEleve) { this.numEleve = numEleve; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public double getMoyenne() { return moyenne; }
    public void setMoyenne(double moyenne) { this.moyenne = moyenne; }
    public int getRang() { return rang; }
    public void setRang(int rang) { this.rang = rang; }
}
