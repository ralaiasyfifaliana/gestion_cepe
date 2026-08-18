package mg.cepe.ui;

import mg.cepe.model.*;
import mg.cepe.service.EcoleService;
import mg.cepe.service.EleveService;
import mg.cepe.service.MatiereService;
import mg.cepe.service.NoteService;
import mg.cepe.service.PdfService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liste automatiquement les élèves ayant une note pour chaque matière
 * existante (relevé complet) pour l'année scolaire de la session en cours.
 * Un simple clic sur le bouton PDF de la ligne génère le relevé de l'élève.
 */
public class RelevePanel extends JPanel implements Refreshable {

    private final EleveService eleveService = new EleveService();
    private final EcoleService ecoleService = new EcoleService();
    private final MatiereService matiereService = new MatiereService();
    private final NoteService noteService = new NoteService();
    private final PdfService pdfService = new PdfService();
    private final java.util.function.Supplier<String> anneeSupplier;

    private final JLabel labelInfo = Theme.mutedLabel(" ");
    private final ReleveTableModel tableModel = new ReleveTableModel();
    private final JTable table = new JTable(tableModel);

    public RelevePanel(java.util.function.Supplier<String> anneeSupplier) {
        this.anneeSupplier = anneeSupplier;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);

        add(construireEntete(), BorderLayout.NORTH);
        add(construireTable(), BorderLayout.CENTER);

        rafraichirListe();
    }

    private JPanel construireEntete() {
        JPanel panel = Theme.card("Relevés de notes disponibles");
        panel.setLayout(new BorderLayout());
        JLabel explication = Theme.mutedLabel(
                "Élèves ayant une note dans chaque matière existante pour l'année en cours. "
                + "Cliquez sur « PDF » pour générer directement le relevé.");
        panel.add(explication, BorderLayout.NORTH);
        panel.add(labelInfo, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construireTable() {
        Theme.styleTable(table);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(4).setCellRenderer(new BoutonPdfRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new BoutonPdfEditor());
        table.getColumnModel().getColumn(4).setMaxWidth(110);

        JPanel wrap = Theme.card("Élèves avec relevé complet");
        wrap.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void rafraichirListe() {
        try {
            int nbMatieres = matiereService.listerTout().size();
            List<EleveMoyenne> liste = noteService.listerElevesAvecReleveComplet(anneeSupplier.get(), nbMatieres);
            tableModel.setDonnees(liste);
            labelInfo.setText(liste.size() + " élève(s) ont un relevé complet (" + nbMatieres + " matière(s) au total).");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void genererPdfPour(EleveMoyenne em) {
        String annee = anneeSupplier.get();
        try {
            Eleve eleve = eleveService.rechercherParId(em.getNumEleve());
            if (eleve == null) {
                JOptionPane.showMessageDialog(this, "Élève introuvable.");
                return;
            }
            Ecole ecole = ecoleService.rechercherParId(eleve.getNumEcole());
            List<Note> notes = noteService.listerParEleveEtAnnee(eleve.getNumEleve(), annee);

            List<LigneReleve> lignes = new ArrayList<>();
            for (Note n : notes) {
                Matiere matiere = matiereService.rechercherParId(n.getNumMat());
                String designation = matiere != null ? matiere.getDesignMat() : n.getNumMat();
                int coef = matiere != null ? matiere.getCoef() : 1;
                lignes.add(new LigneReleve(designation, coef, n.getNote()));
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Releve_" + eleve.getNom() + "_" + eleve.getPrenom() + ".pdf"));
            int choix = chooser.showSaveDialog(this);
            if (choix != JFileChooser.APPROVE_OPTION) return;

            String chemin = chooser.getSelectedFile().getAbsolutePath();
            if (!chemin.toLowerCase().endsWith(".pdf")) chemin += ".pdf";

            pdfService.genererReleve(eleve, ecole, annee, lignes, chemin);
            JOptionPane.showMessageDialog(this, "Relevé de notes généré avec succès :\n" + chemin);
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(chemin));
            } catch (Exception ignorable) {
                // Ouverture automatique non disponible : pas bloquant.
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération du PDF : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        rafraichirListe();
    }

    // ---------------------------------------------------------------
    // Modèle de table + bouton PDF par ligne
    // ---------------------------------------------------------------
    private class ReleveTableModel extends AbstractTableModel {
        private final String[] colonnes = {"N° Élève", "Nom", "Prénom", "Moyenne / 20", "Relevé"};
        private List<EleveMoyenne> donnees = new ArrayList<>();

        void setDonnees(List<EleveMoyenne> liste) {
            this.donnees = liste;
            fireTableDataChanged();
        }

        EleveMoyenne getLigne(int row) { return donnees.get(row); }

        @Override public int getRowCount() { return donnees.size(); }
        @Override public int getColumnCount() { return colonnes.length; }
        @Override public String getColumnName(int col) { return colonnes[col]; }
        @Override public boolean isCellEditable(int row, int col) { return col == 4; }

        @Override
        public Object getValueAt(int row, int col) {
            EleveMoyenne em = donnees.get(row);
            switch (col) {
                case 0: return em.getNumEleve();
                case 1: return em.getNom();
                case 2: return em.getPrenom();
                case 3: return String.format("%.2f", em.getMoyenne());
                case 4: return "📄 PDF";
                default: return "";
            }
        }
    }

    private static class BoutonPdfRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        BoutonPdfRenderer() {
            setOpaque(true);
            setFont(Theme.FONT_NORMAL);
            setBackground(Theme.PRIMARY);
            setForeground(Color.WHITE);
            setBorderPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            setText("📄 PDF");
            return this;
        }
    }

    private class BoutonPdfEditor extends javax.swing.DefaultCellEditor {
        private final JButton bouton = new JButton("📄 PDF");
        private int ligneCourante;

        BoutonPdfEditor() {
            super(new JCheckBox());
            bouton.setOpaque(true);
            bouton.setFont(Theme.FONT_NORMAL);
            bouton.setBackground(Theme.PRIMARY);
            bouton.setForeground(Color.WHITE);
            bouton.setBorderPainted(false);
            bouton.addActionListener(e -> {
                fireEditingStopped();
                EleveMoyenne em = tableModel.getLigne(ligneCourante);
                SwingUtilities.invokeLater(() -> genererPdfPour(em));
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                       int row, int column) {
            this.ligneCourante = row;
            return bouton;
        }

        @Override
        public Object getCellEditorValue() { return "📄 PDF"; }
    }
}
