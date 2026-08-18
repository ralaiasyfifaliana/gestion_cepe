package mg.cepe.service;

import mg.cepe.model.Ecole;
import mg.cepe.model.Eleve;
import mg.cepe.model.EleveMoyenne;
import mg.cepe.model.LigneReleve;
import mg.cepe.pdf.PdfDocument;
import mg.cepe.pdf.PdfTableWriter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Génération de documents PDF (moteur maison, sans bibliothèque externe) :
 * - relevé de notes d'un élève
 * - listes d'élèves (réussite après délibération, échecs, admis en 6e,
 *   classement par ordre de mérite)
 */
public class PdfService {

    // ---------------------------------------------------------------
    // Relevé de notes individuel
    // ---------------------------------------------------------------
    public void genererReleve(Eleve eleve, Ecole ecole, String anneeScolaire,
                               List<LigneReleve> lignes, String cheminSortie) throws IOException {
        PdfDocument doc = new PdfDocument();

        float top = doc.pageHeight() - 55f;
        float infoY = top - 77;
        doc.textCentered(0, doc.pageWidth(), top, 18, true, "RELEVÉ DE NOTES - CEPE");
        doc.textCentered(0, doc.pageWidth(), top - 22, 11, false, "Année scolaire : " + anneeScolaire);
        doc.text(50, infoY + 18, 11, true, "Nom : ");
        doc.text(90, infoY + 18, 11, false, eleve.getNom());
        doc.text(300, infoY + 18, 11, true, "Prénom(s) : ");
        doc.text(370, infoY + 18, 11, false, eleve.getPrenom());
        doc.text(50, infoY, 11, true, "N° Élève : ");
        doc.text(110, infoY, 11, false, eleve.getNumEleve());
        if (ecole != null) {
            doc.text(300, infoY, 11, true, "École : ");
            doc.text(345, infoY, 11, false, ecole.getDesign());
        }

        int totalCoef = 0;
        int totalPondere = 0;
        for (LigneReleve l : lignes) {
            totalCoef += l.getCoef();
            totalPondere += l.getNotePonderee();
        }
        double moyenne = totalCoef == 0 ? 0.0 : (double) totalPondere / (double) totalCoef;

        PdfTableWriterAtY writer = new PdfTableWriterAtY(doc,
                new String[]{"Matière", "Coefficient", "Note / 20", "Note "},
                new float[]{0.46f, 0.18f, 0.18f, 0.18f}, infoY - 35);
        for (LigneReleve l : lignes) {
            writer.addRow(l.getDesignMat(), String.valueOf(l.getCoef()),
                    String.valueOf(l.getNote()), String.valueOf(l.getNotePonderee()));
        }
        writer.footerLine(String.format(Locale.US, "Moyenne générale : %.2f / 20", moyenne), true, 13);

        String mention;
        double m2 = Math.round(moyenne * 100.0) / 100.0;
        if (m2 < NoteService.SEUIL_REUSSITE) {
            mention = "NON ADMIS(E) AU CEPE";
        } else if (m2 == NoteService.SEUIL_REUSSITE) {
            mention = "ADMIS(E) AU CEPE (après délibération)";
        } else if (m2 > NoteService.SEUIL_ADMISSION_SIXIEME) {
            mention = "ADMIS(E) AU CEPE -- ADMIS(E) EN CLASSE DE 6e";
        } else {
            mention = "ADMIS(E) AU CEPE";
        }
        writer.footerLine(mention, true, 13);
        writer.footerLine("", false, 9);
        
        doc.save(cheminSortie);
    }

    // ---------------------------------------------------------------
    // Listes d'élèves (réussite, échecs, admis 6e, classement)
    // ---------------------------------------------------------------
    public void genererListeEleves(String titre, String anneeScolaire, List<EleveMoyenne> liste,
                                    boolean avecRang, String cheminSortie) throws IOException {
        PdfDocument doc = new PdfDocument();
        String[] headers = avecRang
                ? new String[]{"Rang", "N° Élève", "Nom", "Prénom", "Moyenne / 20"}
                : new String[]{"N° Élève", "Nom", "Prénom", "Moyenne / 20"};
        float[] widths = avecRang
                ? new float[]{0.12f, 0.20f, 0.28f, 0.24f, 0.16f}
                : new float[]{0.22f, 0.32f, 0.28f, 0.18f};

        PdfTableWriter table = new PdfTableWriter(doc, titre, "Année scolaire : " + anneeScolaire, headers, widths);
        for (EleveMoyenne em : liste) {
            if (avecRang) {
                table.addRow(String.valueOf(em.getRang()), em.getNumEleve(), em.getNom(), em.getPrenom(),
                        String.format(Locale.US, "%.2f", em.getMoyenne()));
            } else {
                table.addRow(em.getNumEleve(), em.getNom(), em.getPrenom(),
                        String.format(Locale.US, "%.2f", em.getMoyenne()));
            }
        }
        table.footerLine("Total : " + liste.size() + " élève(s)", true, 11);
        table.footerLine("", false, 9);

        doc.save(cheminSortie);
    }

    /**
     * Petite variante de {@link PdfTableWriter} qui démarre le tableau à une
     * ordonnée Y donnée (au lieu de redessiner un titre en haut de page),
     * utilisée pour le relevé individuel où l'en-tête (nom, école...) est
     * déjà positionné manuellement.
     */
    private static class PdfTableWriterAtY {
        private static final float MARGIN_LEFT = 50f;
        private static final float MARGIN_RIGHT = 45f;
        private static final float MARGIN_BOTTOM = 55f;
        private static final float ROW_HEIGHT = 20f;

        private final PdfDocument doc;
        private final String[] headers;
        private final float[] colWidths;
        private final float tableWidth;
        private float y;

        PdfTableWriterAtY(PdfDocument doc, String[] headers, float[] colWidths, float startY) {
            this.doc = doc;
            this.headers = headers;
            this.colWidths = colWidths;
            this.tableWidth = doc.pageWidth() - MARGIN_LEFT - MARGIN_RIGHT;
            this.y = startY;
            drawHeaderRow();
        }

        private void drawHeaderRow() {
            float x = MARGIN_LEFT;
            doc.setFillColor(0.16f, 0.29f, 0.58f);
            doc.rect(MARGIN_LEFT, y - ROW_HEIGHT + 5, tableWidth, ROW_HEIGHT, true);
            doc.setFillColor(1f, 1f, 1f);
            for (int i = 0; i < headers.length; i++) {
                float w = tableWidth * colWidths[i];
                doc.text(x + 6, y - ROW_HEIGHT + 11, 10.5f, true, headers[i]);
                x += w;
            }
            doc.setFillColor(0f, 0f, 0f);
            y -= ROW_HEIGHT;
        }

        void addRow(String... values) {
            if (y - ROW_HEIGHT < MARGIN_BOTTOM) {
                doc.newPage();
                y = doc.pageHeight() - 55f;
                drawHeaderRow();
            }
            float x = MARGIN_LEFT;
            for (int i = 0; i < values.length && i < colWidths.length; i++) {
                float w = tableWidth * colWidths[i];
                doc.text(x + 6, y - ROW_HEIGHT + 11, 10.5f, false, values[i]);
                x += w;
            }
            doc.setStrokeColor(0.85f, 0.85f, 0.85f);
            doc.setLineWidth(0.5f);
            doc.line(MARGIN_LEFT, y - ROW_HEIGHT + 5, MARGIN_LEFT + tableWidth, y - ROW_HEIGHT + 5);
            y -= ROW_HEIGHT;
        }

        void footerLine(String text, boolean bold, float size) {
            if (y - ROW_HEIGHT < MARGIN_BOTTOM) {
                doc.newPage();
                y = doc.pageHeight() - 55f;
            }
            y -= 8;
            doc.text(MARGIN_LEFT, y, size, bold, text);
            y -= (size + 6);
        }
    }
}
