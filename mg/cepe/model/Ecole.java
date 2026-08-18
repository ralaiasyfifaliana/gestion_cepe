package mg.cepe.model;

import java.util.Objects;

public class Ecole {
    private String numEcole;
    private String design;
    private String adresse;

    public Ecole() { }

    public Ecole(String numEcole, String design, String adresse) {
        this.numEcole = numEcole;
        this.design = design;
        this.adresse = adresse;
    }

    public String getNumEcole() { return numEcole; }
    public void setNumEcole(String numEcole) { this.numEcole = numEcole; }
    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    @Override
    public String toString() { return numEcole + " - " + design; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ecole)) return false;
        Ecole ecole = (Ecole) o;
        return Objects.equals(numEcole, ecole.numEcole);
    }

    @Override
    public int hashCode() { return Objects.hash(numEcole); }
}
