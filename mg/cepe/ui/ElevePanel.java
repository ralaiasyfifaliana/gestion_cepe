package mg.cepe.ui;

import mg.cepe.model.Ecole;
import mg.cepe.model.Eleve;
import mg.cepe.service.EcoleService;
import mg.cepe.service.EleveService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestion des élèves : création, listage, modification, suppression, recherche,
 * et accès à la gestion des notes de l'élève sélectionné.
 * Formulaire à gauche, liste à droite.
 */
public class ElevePanel extends JPanel implements Refreshable {

    private final EleveService eleveService = new EleveService();
    private final EcoleService ecoleService = new EcoleService();

    private final JTextField champNumEleve = Theme.textField(14);
    private final JComboBox<Ecole> comboEcole = new JComboBox<>();
    private final JTextField champNom = Theme.textField(14);
    private final JTextField champPrenom = Theme.textField(14);
    private final JTextField champRecherche = Theme.textField(14);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"N° Élève", "N° École", "Nom", "Prénom"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private boolean modeRecherche = false;
    private EleveNotesListener notesListener;
    private final JButton btnGererNotes = Theme.primaryButton("📝 Gérer les notes de l'élève sélectionné");

    public ElevePanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(construireFormulaire(), BorderLayout.WEST);
        add(construireTable(), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            remplirFormulaireDepuisSelection();
            btnGererNotes.setEnabled(table.getSelectedRow() >= 0);
        });
        btnGererNotes.setEnabled(false);
        Theme.styleField(comboEcole);

        chargerEcolesDansCombo();
        rafraichirTable();
    }

    public void setNotesListener(EleveNotesListener listener) {
        this.notesListener = listener;
    }

    private JPanel construireFormulaire() {
        JPanel outer = Theme.card("Fiche élève");
        outer.setLayout(new BorderLayout());
        outer.setPreferredSize(new Dimension(320, 0));

        JPanel champs = new JPanel();
        champs.setOpaque(false);
        champs.setLayout(new BoxLayout(champs, BoxLayout.Y_AXIS));

        champs.add(champLabel("N° Élève"));
        champs.add(pleineLargeur(champNumEleve));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("École"));
        champs.add(pleineLargeur(comboEcole));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Nom"));
        champs.add(pleineLargeur(champNom));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Prénom"));
        champs.add(pleineLargeur(champPrenom));
        champs.add(Box.createVerticalStrut(16));

        JButton btnAjouter = Theme.successButton("Ajouter");
        JButton btnModifier = Theme.primaryButton("Modifier");
        JButton btnSupprimer = Theme.dangerButton("Supprimer");
        JButton btnEffacer = Theme.neutralButton("Effacer le formulaire");

        for (JButton b : new JButton[]{btnAjouter, btnModifier, btnSupprimer, btnEffacer}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            champs.add(b);
            champs.add(Box.createVerticalStrut(8));
        }

        champs.add(Box.createVerticalStrut(8));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        champs.add(sep);
        champs.add(Box.createVerticalStrut(10));

        btnGererNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGererNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        champs.add(btnGererNotes);

        btnAjouter.addActionListener(e -> ajouter());
        btnModifier.addActionListener(e -> modifier());
        btnSupprimer.addActionListener(e -> supprimer());
        btnEffacer.addActionListener(e -> effacerFormulaire());
        btnGererNotes.addActionListener(e -> ouvrirGestionNotes());

        JPanel recherche = new JPanel();
        recherche.setOpaque(false);
        recherche.setLayout(new BoxLayout(recherche, BoxLayout.Y_AXIS));
        recherche.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        recherche.add(champLabel("Recherche (nom ou prénom)"));
        recherche.add(pleineLargeur(champRecherche));
        recherche.add(Box.createVerticalStrut(8));
        JButton btnRechercher = Theme.neutralButton("Rechercher");
        JButton btnToutAfficher = Theme.neutralButton("Tout afficher");
        btnRechercher.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRechercher.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnToutAfficher.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnToutAfficher.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnRechercher.addActionListener(e -> rechercher());
        btnToutAfficher.addActionListener(e -> { modeRecherche = false; rafraichirTable(); });
        recherche.add(btnRechercher);
        recherche.add(Box.createVerticalStrut(6));
        recherche.add(btnToutAfficher);
        champs.add(recherche);

        outer.add(champs, BorderLayout.NORTH);
        return outer;
    }

    private JLabel champLabel(String text) {
        JLabel l = Theme.mutedLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JComponent pleineLargeur(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return c;
    }

    private JPanel construireTable() {
        Theme.styleTable(table);
        JPanel wrap = Theme.card("Liste des élèves");
        wrap.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void chargerEcolesDansCombo() {
        try {
            Ecole selection = (Ecole) comboEcole.getSelectedItem();
            comboEcole.removeAllItems();
            for (Ecole e : ecoleService.listerTout()) comboEcole.addItem(e);
            if (selection != null) comboEcole.setSelectedItem(selection);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void ajouter() {
        try {
            Ecole ecole = (Ecole) comboEcole.getSelectedItem();
            if (ecole == null) {
                JOptionPane.showMessageDialog(this, "Veuillez d'abord créer une école.");
                return;
            }
            Eleve eleve = new Eleve(champNumEleve.getText().trim(), ecole.getNumEcole(),
                    champNom.getText().trim(), champPrenom.getText().trim());
            eleveService.ajouter(eleve);
            rafraichirTable();
            effacerFormulaire();
            JOptionPane.showMessageDialog(this, "Élève ajouté avec succès.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void modifier() {
        try {
            if (champNumEleve.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un élève à modifier.");
                return;
            }
            Ecole ecole = (Ecole) comboEcole.getSelectedItem();
            if (ecole == null) return;
            Eleve eleve = new Eleve(champNumEleve.getText().trim(), ecole.getNumEcole(),
                    champNom.getText().trim(), champPrenom.getText().trim());
            boolean ok = eleveService.modifier(eleve);
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "Élève modifié avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucun élève trouvé avec ce numéro.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void supprimer() {
        if (champNumEleve.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un élève à supprimer.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de l'élève " + champNumEleve.getText() + " ?\n"
                + "Toutes ses notes seront également supprimées.",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = eleveService.supprimer(champNumEleve.getText().trim());
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "Élève (et ses notes) supprimé avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucun élève trouvé avec ce numéro.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void rechercher() {
        modeRecherche = true;
        try {
            List<Eleve> resultats = eleveService.rechercherParNomOuPrenom(champRecherche.getText().trim());
            remplirTable(resultats);
            if (resultats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun élève trouvé pour ce mot-clé.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void rafraichirTable() {
        try {
            if (modeRecherche && !champRecherche.getText().isBlank()) {
                remplirTable(eleveService.rechercherParNomOuPrenom(champRecherche.getText().trim()));
            } else {
                remplirTable(eleveService.listerTout());
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void remplirTable(List<Eleve> liste) {
        int selectedRow = table.getSelectedRow();
        String selectedId = selectedRow >= 0 ? String.valueOf(tableModel.getValueAt(selectedRow, 0)) : null;
        tableModel.setRowCount(0);
        for (Eleve e : liste) {
            tableModel.addRow(new Object[]{e.getNumEleve(), e.getNumEcole(), e.getNom(), e.getPrenom()});
        }
        if (selectedId != null) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (String.valueOf(tableModel.getValueAt(i, 0)).equals(selectedId)) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void remplirFormulaireDepuisSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        champNumEleve.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        String numEcole = String.valueOf(tableModel.getValueAt(row, 1));
        for (int i = 0; i < comboEcole.getItemCount(); i++) {
            if (comboEcole.getItemAt(i).getNumEcole().equals(numEcole)) {
                comboEcole.setSelectedIndex(i);
                break;
            }
        }
        champNom.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        champPrenom.setText(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void ouvrirGestionNotes() {
        int row = table.getSelectedRow();
        if (row < 0 || notesListener == null) return;
        String numEleve = String.valueOf(tableModel.getValueAt(row, 0));
        try {
            Eleve eleve = eleveService.rechercherParId(numEleve);
            if (eleve != null) notesListener.gererNotes(eleve);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void effacerFormulaire() {
        champNumEleve.setText("");
        champNom.setText("");
        champPrenom.setText("");
        table.clearSelection();
        btnGererNotes.setEnabled(false);
    }

    private void afficherErreur(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        chargerEcolesDansCombo();
        rafraichirTable();
    }
}
