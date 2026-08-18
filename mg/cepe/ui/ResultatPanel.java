package mg.cepe.ui;

import mg.cepe.model.EleveMoyenne;
import mg.cepe.service.NoteService;
import mg.cepe.service.PdfService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

/**
 * Résultats de la session CEPE (anciennement « Délibération ») :
 * - Échecs                    : moyenne &lt; 9,75/20
 * - Admis après délibération  : moyenne exactement égale à 9,75/20
 * - Admis (réussite directe)  : moyenne &gt; 9,75/20
 * - Admis en classe de 6e     : moyenne &gt; 12/20
 * - Classement par ordre de mérite : exclut les élèves en situation d'échec.
 * Chaque liste peut être exportée en PDF.
 */
public class ResultatPanel extends JPanel implements Refreshable {

    private final NoteService noteService = new NoteService();
    private final PdfService pdfService = new PdfService();
    private final Supplier<String> anneeSupplier;

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Rang", "N° Élève", "Nom", "Prénom", "Moyenne / 20"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel labelInfo = Theme.mutedLabel(" ");

    private List<EleveMoyenne> derniereListe = List.of();
    private boolean derniereAvecRang = false;
    private String dernierTitre = "Liste des élèves";
    private Runnable dernierRecalcul = null;

    public ResultatPanel(Supplier<String> anneeSupplier) {
        this.anneeSupplier = anneeSupplier;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);

        add(construireBarreOutils(), BorderLayout.NORTH);
        add(construireTable(), BorderLayout.CENTER);
    }

    private JPanel construireBarreOutils() {
        JPanel panel = Theme.card("Résultats de la session (seuil de réussite = "
                + NoteService.SEUIL_REUSSITE + "/20, admission en 6e > "
                + NoteService.SEUIL_ADMISSION_SIXIEME + "/20)");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ligneActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        ligneActions.setOpaque(false);

        JButton btnEchecs = Theme.dangerButton("Échecs");
        JButton btnApresDelib = Theme.warningButton("Admis après délibération (= 9,75)");
        JButton btnAdmis = Theme.successButton("Admis (réussite directe)");
        JButton btnAdmisSixieme = Theme.primaryButton("Admis en 6e (> 12)");
        JButton btnClassement = Theme.neutralButton("Classement par ordre de mérite");
        JButton btnExportPdf = Theme.primaryButton("📄 Exporter cette liste en PDF");

        btnEchecs.addActionListener(e -> afficherEchecs());
        btnApresDelib.addActionListener(e -> afficherAdmisApresDeliberation());
        btnAdmis.addActionListener(e -> afficherAdmis());
        btnAdmisSixieme.addActionListener(e -> afficherAdmisSixieme());
        btnClassement.addActionListener(e -> afficherClassement());
        btnExportPdf.addActionListener(e -> exporterPdf());

        ligneActions.add(btnEchecs);
        ligneActions.add(btnApresDelib);
        ligneActions.add(btnAdmis);
        ligneActions.add(btnAdmisSixieme);
        ligneActions.add(btnClassement);
        ligneActions.add(btnExportPdf);

        panel.add(ligneActions);
        panel.add(labelInfo);
        return panel;
    }

    private JPanel construireTable() {
        Theme.styleTable(table);
        JPanel wrap = Theme.card("Résultat");
        wrap.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void afficherEchecs() {
        dernierRecalcul = this::afficherEchecs;
        try {
            List<EleveMoyenne> liste = noteService.listerEchecs(anneeSupplier.get());
            majAffichage(liste, false, "Liste des échecs au CEPE",
                    liste.size() + " élève(s) en situation d'échec.");
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void afficherAdmisApresDeliberation() {
        dernierRecalcul = this::afficherAdmisApresDeliberation;
        try {
            List<EleveMoyenne> liste = noteService.listerAdmisApresDeliberation(anneeSupplier.get());
            majAffichage(liste, false, "Admis après délibération (moyenne = 9,75/20)",
                    liste.size() + " élève(s) admis après délibération.");
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void afficherAdmis() {
        dernierRecalcul = this::afficherAdmis;
        try {
            List<EleveMoyenne> liste = noteService.listerAdmis(anneeSupplier.get());
            majAffichage(liste, false, "Admis au CEPE (réussite directe)",
                    liste.size() + " élève(s) admis directement.");
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void afficherAdmisSixieme() {
        dernierRecalcul = this::afficherAdmisSixieme;
        try {
            List<EleveMoyenne> liste = noteService.listerAdmisEnSixieme(anneeSupplier.get());
            majAffichage(liste, false, "Admis en classe de 6e", liste.size() + " élève(s) admis en classe de 6e.");
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void afficherClassement() {
        dernierRecalcul = this::afficherClassement;
        try {
            List<EleveMoyenne> liste = noteService.classerParOrdreDeMerite(anneeSupplier.get());
            majAffichage(liste, true, "Classement par ordre de mérite",
                    "Classement par ordre de mérite, hors échecs (" + liste.size() + " élève(s)).");
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void majAffichage(List<EleveMoyenne> liste, boolean avecRang, String titre, String info) {
        this.derniereListe = liste;
        this.derniereAvecRang = avecRang;
        this.dernierTitre = titre;
        remplirTable(liste, avecRang);
        labelInfo.setText(info);
    }

    private void remplirTable(List<EleveMoyenne> liste, boolean avecRang) {
        tableModel.setRowCount(0);
        for (EleveMoyenne em : liste) {
            tableModel.addRow(new Object[]{
                    avecRang ? em.getRang() : "-",
                    em.getNumEleve(),
                    em.getNom(),
                    em.getPrenom(),
                    String.format("%.2f", em.getMoyenne())
            });
        }
    }

    private void exporterPdf() {
        if (derniereListe.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord afficher une liste (échecs, admis, admis en 6e ou classement).");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        String nomFichier = dernierTitre.replaceAll("[^A-Za-zÀ-ÿ0-9]+", "_") + "_" + anneeSupplier.get() + ".pdf";
        chooser.setSelectedFile(new File(nomFichier));
        int choix = chooser.showSaveDialog(this);
        if (choix != JFileChooser.APPROVE_OPTION) return;

        String chemin = chooser.getSelectedFile().getAbsolutePath();
        if (!chemin.toLowerCase().endsWith(".pdf")) chemin += ".pdf";

        try {
            pdfService.genererListeEleves(dernierTitre, anneeSupplier.get(), derniereListe, derniereAvecRang, chemin);
            JOptionPane.showMessageDialog(this, "Liste exportée en PDF avec succès :\n" + chemin);
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(chemin));
            } catch (Exception ignorable) {
                // Ouverture automatique non disponible : pas bloquant.
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération du PDF : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afficherErreur(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        if (dernierRecalcul != null) {
            dernierRecalcul.run();
        }
    }
}
