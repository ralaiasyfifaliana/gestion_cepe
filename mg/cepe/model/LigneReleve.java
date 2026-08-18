package mg.cepe.model;

public class LigneReleve {
    private String designMat;
    private int coef;
    private double note;

    public LigneReleve(String designMat, int coef, double note) {
        this.designMat = designMat;
        this.coef = coef;
        this.note = note;
    }

    public String getDesignMat() { return designMat; }
    public int getCoef() { return coef; }
    public double getNote() { return note; }
    public double getNotePonderee() { return note * coef; }
}
