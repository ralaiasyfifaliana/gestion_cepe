package mg.cepe.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Générateur PDF minimaliste, écrit à la main, sans aucune bibliothèque externe
 * (pas de Maven, pas de jar tiers pour la partie PDF).
 * Supporte : texte (police Helvetica normale/grasse), lignes, rectangles
 * (remplis ou non), plusieurs pages, encodage WinAnsi (accents français).
 *
 * Usage typique :
 *   PdfDocument doc = new PdfDocument();
 *   doc.text(50, 800, 16, true, "Titre");
 *   doc.line(50, 790, 545, 790);
 *   doc.save("sortie.pdf");
 */
public class PdfDocument {

    public static final float PAGE_WIDTH = 595f;   // A4 portrait, en points
    public static final float PAGE_HEIGHT = 842f;

    private final List<StringBuilder> pages = new ArrayList<>();
    private StringBuilder current;

    public PdfDocument() {
        newPage();
    }

    public void newPage() {
        current = new StringBuilder();
        pages.add(current);
    }

    public float pageWidth() { return PAGE_WIDTH; }
    public float pageHeight() { return PAGE_HEIGHT; }

    public void setFillGray(float g) {
        current.append(fmt(g)).append(" g\n");
    }

    public void setStrokeGray(float g) {
        current.append(fmt(g)).append(" G\n");
    }

    public void setFillColor(float r, float g, float b) {
        current.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" rg\n");
    }

    public void setStrokeColor(float r, float g, float b) {
        current.append(fmt(r)).append(" ").append(fmt(g)).append(" ").append(fmt(b)).append(" RG\n");
    }

    public void setLineWidth(float w) {
        current.append(fmt(w)).append(" w\n");
    }

    public void line(float x1, float y1, float x2, float y2) {
        current.append(fmt(x1)).append(" ").append(fmt(y1)).append(" m ")
                .append(fmt(x2)).append(" ").append(fmt(y2)).append(" l S\n");
    }

    public void rect(float x, float y, float w, float h, boolean fill) {
        current.append(fmt(x)).append(" ").append(fmt(y)).append(" ")
                .append(fmt(w)).append(" ").append(fmt(h)).append(" re ")
                .append(fill ? "f" : "S").append("\n");
    }

    /** Affiche du texte, coin bas-gauche à (x,y), police Helvetica normale ou grasse. */
    public void text(float x, float y, float size, boolean bold, String s) {
        current.append("BT /").append(bold ? "FB" : "F1").append(" ").append(fmt(size)).append(" Tf ")
                .append(fmt(x)).append(" ").append(fmt(y)).append(" Td (")
                .append(escape(s)).append(") Tj ET\n");
    }

    /** Texte centré horizontalement dans [x, x+w]. */
    public void textCentered(float x, float w, float y, float size, boolean bold, String s) {
        float textWidth = approxWidth(s, size, bold);
        float startX = x + Math.max(0, (w - textWidth) / 2f);
        text(startX, y, size, bold, s);
    }

    /** Estimation grossière de la largeur du texte pour du Helvetica (suffisant pour centrer). */
    public float approxWidth(String s, float size, boolean bold) {
        float factor = bold ? 0.60f : 0.52f;
        return s.length() * size * factor;
    }

    private static String escape(String s) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '(' || c == ')') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    private static String fmt(float f) {
        if (f == Math.floor(f)) return String.valueOf((long) f);
        return String.format(Locale.US, "%.2f", f);
    }

    /** Écrit le document PDF final sur disque. */
    public void save(String path) throws IOException {
        int nPages = pages.size();
        // Numérotation des objets :
        // 1 = Catalog, 2 = Pages, 3..(2+nPages) = Page N, (3+nPages)..(2+2*nPages) = Content stream N
        // (3+2*nPages) = Font F1, (4+2*nPages) = Font FB
        int catalogNum = 1;
        int pagesNum = 2;
        int firstPageNum = 3;
        int firstContentNum = firstPageNum + nPages;
        int fontF1Num = firstContentNum + nPages;
        int fontFBNum = fontF1Num + 1;
        int totalObjects = fontFBNum;

        List<byte[]> objects = new ArrayList<>();
        objects.add(null); // index 0 inutilisé

        // 1: Catalog
        objects.add(obj(catalogNum, "<< /Type /Catalog /Pages " + pagesNum + " 0 R >>"));

        // 2: Pages
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < nPages; i++) {
            kids.append(firstPageNum + i).append(" 0 R ");
        }
        objects.add(obj(pagesNum, "<< /Type /Pages /Kids [ " + kids + "] /Count " + nPages + " >>"));

        // Pages + Content streams
        for (int i = 0; i < nPages; i++) {
            int pageNum = firstPageNum + i;
            int contentNum = firstContentNum + i;
            String pageDict = "<< /Type /Page /Parent " + pagesNum + " 0 R "
                    + "/MediaBox [0 0 " + fmt(PAGE_WIDTH) + " " + fmt(PAGE_HEIGHT) + "] "
                    + "/Resources << /Font << /F1 " + fontF1Num + " 0 R /FB " + fontFBNum + " 0 R >> >> "
                    + "/Contents " + contentNum + " 0 R >>";
            objects.add(obj(pageNum, pageDict));

            byte[] streamBytes = pages.get(i).toString().getBytes(StandardCharsets.ISO_8859_1);
            ByteArrayOutputStream cs = new ByteArrayOutputStream();
            cs.write(("" + contentNum + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            cs.write(streamBytes);
            cs.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            objects.add(cs.toByteArray());
        }

        // Fonts
        objects.add(obj(fontF1Num,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objects.add(obj(fontFBNum,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));

        // Assemblage final avec table xref
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

        int[] offsets = new int[totalObjects + 1];
        for (int i = 1; i <= totalObjects; i++) {
            offsets[i] = out.size();
            out.write(objects.get(i));
        }

        int xrefStart = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(totalObjects + 1).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int i = 1; i <= totalObjects; i++) {
            xref.append(String.format(Locale.US, "%010d 00000 n \n", offsets[i]));
        }
        xref.append("trailer\n<< /Size ").append(totalObjects + 1)
                .append(" /Root ").append(catalogNum).append(" 0 R >>\n")
                .append("startxref\n").append(xrefStart).append("\n%%EOF");

        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));

        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(out.toByteArray());
        }
    }

    private static byte[] obj(int num, String dict) {
        String s = num + " 0 obj\n" + dict + "\nendobj\n";
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }
}
