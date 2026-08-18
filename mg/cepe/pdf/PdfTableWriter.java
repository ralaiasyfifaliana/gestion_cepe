package mg.cepe.pdf;

import java.util.List;

/**
 * Aide à dessiner un tableau (en-têtes + lignes) dans un {@link PdfDocument},
 * avec gestion automatique du passage à la page suivante et ré-affichage
 * de l'en-tête sur chaque nouvelle page.
 */
public class PdfTableWriter {

    private static final float MARGIN_LEFT = 45f;
    private static final float MARGIN_RIGHT = 45f;
    private static final float MARGIN_BOTTOM = 55f;
    private static final float ROW_HEIGHT = 20f;
    private static final float HEADER_FONT_SIZE = 10.5f;
    private static final float CELL_FONT_SIZE = 10.5f;

    private final PdfDocument doc;
    private final String[] headers;
    private final float[] colWidths; // en pourcentage (somme = 1.0)
    private final String reportTitle;
    private final String subTitle;

    private float y;
    private float tableWidth;

    public PdfTableWriter(PdfDocument doc, String reportTitle, String subTitle,
                           String[] headers, float[] colWidths) {
        this.doc = doc;
        this.reportTitle = reportTitle;
        this.subTitle = subTitle;
        this.headers = headers;
        this.colWidths = colWidths;
        this.tableWidth = doc.pageWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        drawPageHeader(true);
    }

    private void drawPageHeader(boolean firstPage) {
        float top = doc.pageHeight() - 55f;
        if (firstPage) {
            doc.textCentered(0, doc.pageWidth(), top, 17, true, reportTitle);
            if (subTitle != null && !subTitle.isBlank()) {
                doc.textCentered(0, doc.pageWidth(), top - 20, 11, false, subTitle);
                top -= 20;
            }
            y = top - 30;
        } else {
            doc.textCentered(0, doc.pageWidth(), top, 12, true, reportTitle + " (suite)");
            y = top - 30;
        }
        drawTableHeaderRow();
    }

    private void drawTableHeaderRow() {
        float x = MARGIN_LEFT;
        doc.setFillColor(0.16f, 0.29f, 0.58f);
        doc.rect(MARGIN_LEFT, y - ROW_HEIGHT + 5, tableWidth, ROW_HEIGHT, true);
        doc.setFillColor(1f, 1f, 1f);
        for (int i = 0; i < headers.length; i++) {
            float w = tableWidth * colWidths[i];
            doc.text(x + 6, y - ROW_HEIGHT + 11, HEADER_FONT_SIZE, true, headers[i]);
            x += w;
        }
        doc.setFillColor(0f, 0f, 0f);
        y -= ROW_HEIGHT;
    }

    /** Ajoute une ligne de données ; passe automatiquement à la page suivante si nécessaire. */
    public void addRow(String... values) {
        if (y - ROW_HEIGHT < MARGIN_BOTTOM) {
            doc.newPage();
            drawPageHeader(false);
        }
        boolean strip = ((int) ((doc.pageHeight() - y) / ROW_HEIGHT)) % 2 == 0;
        if (strip) {
            doc.setFillColor(0.94f, 0.96f, 0.99f);
            doc.rect(MARGIN_LEFT, y - ROW_HEIGHT + 5, tableWidth, ROW_HEIGHT, true);
            doc.setFillColor(0f, 0f, 0f);
        }
        float x = MARGIN_LEFT;
        for (int i = 0; i < values.length && i < colWidths.length; i++) {
            float w = tableWidth * colWidths[i];
            doc.text(x + 6, y - ROW_HEIGHT + 11, CELL_FONT_SIZE, false, values[i] == null ? "" : values[i]);
            x += w;
        }
        doc.setStrokeColor(0.85f, 0.85f, 0.85f);
        doc.setLineWidth(0.5f);
        doc.line(MARGIN_LEFT, y - ROW_HEIGHT + 5, MARGIN_LEFT + tableWidth, y - ROW_HEIGHT + 5);
        y -= ROW_HEIGHT;
    }

    public void addRows(List<String[]> rows) {
        for (String[] row : rows) addRow(row);
    }

    /** Ajoute une ligne de texte libre sous le tableau (ex : mention finale, total, date). */
    public void footerLine(String text, boolean bold, float size) {
        if (y - ROW_HEIGHT < MARGIN_BOTTOM) {
            doc.newPage();
            drawPageHeader(false);
        }
        y -= 8;
        doc.text(MARGIN_LEFT, y, size, bold, text);
        y -= (size + 6);
    }

    public float currentY() { return y; }
}
