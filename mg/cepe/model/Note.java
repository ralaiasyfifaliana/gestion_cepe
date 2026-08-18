package mg.cepe.model;

import java.util.Objects;

public class Note {
    private String anneeScolaire;
    private String numEleve;
    private String numMat;
    private double note;

    public Note() { }

    public Note(String anneeScolaire, String numEleve, String numMat, double note) {
        this.anneeScolaire = anneeScolaire;
        this.numEleve = numEleve;
        this.numMat = numMat;
        this.note = note;
    }

    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    public String getNumEleve() { return numEleve; }
    public void setNumEleve(String numEleve) { this.numEleve = numEleve; }
    public String getNumMat() { return numMat; }
    public void setNumMat(String numMat) { this.numMat = numMat; }
    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note n = (Note) o;
        return Objects.equals(anneeScolaire, n.anneeScolaire)
                && Objects.equals(numEleve, n.numEleve)
                && Objects.equals(numMat, n.numMat);
    }

    @Override
    public int hashCode() { return Objects.hash(anneeScolaire, numEleve, numMat); }
}
