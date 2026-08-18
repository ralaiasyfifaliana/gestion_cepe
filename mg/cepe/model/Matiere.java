package mg.cepe.model;

import java.util.Objects;

public class Matiere {
    private String numMat;
    private String designMat;
    private int coef;

    public Matiere() { }

    public Matiere(String numMat, String designMat, int coef) {
        this.numMat = numMat;
        this.designMat = designMat;
        this.coef = coef;
    }

    public String getNumMat() { return numMat; }
    public void setNumMat(String numMat) { this.numMat = numMat; }
    public String getDesignMat() { return designMat; }
    public void setDesignMat(String designMat) { this.designMat = designMat; }
    public int getCoef() { return coef; }
    public void setCoef(int coef) { this.coef = coef; }

    @Override
    public String toString() { return numMat + " - " + designMat + " (coef " + coef + ")"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matiere)) return false;
        Matiere matiere = (Matiere) o;
        return Objects.equals(numMat, matiere.numMat);
    }

    @Override
    public int hashCode() { return Objects.hash(numMat); }
}
