package mg.cepe.ui;

import mg.cepe.model.Ecole;
import mg.cepe.service.EcoleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestion des écoles (CRUD). Formulaire à gauche, liste à droite.
 * Se rafraîchit automatiquement (voir MainFrame).
 */
public class EcolePanel extends JPanel implements Refreshable {

    private final EcoleService ecoleService = new EcoleService();

    private final JTextField champNumEcole = Theme.textField(14);
    private final JTextField champDesign = Theme.textField(14);
    private final JTextField champAdresse = Theme.textField(14);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"N° École", "Désignation", "Adresse"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
    private final JTable table = new JTable(tableModel);

    public EcolePanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(construireFormulaire(), BorderLayout.WEST);
        add(construireTable(), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> remplirFormulaireDepuisSelection());
        rafraichirTable();
    }

    private JPanel construireFormulaire() {
        JPanel outer = Theme.card("Fiche école");
        outer.setLayout(new BorderLayout());
        outer.setPreferredSize(new Dimension(300, 0));

        JPanel champs = new JPanel();
        champs.setOpaque(false);
        champs.setLayout(new BoxLayout(champs, BoxLayout.Y_AXIS));

        champs.add(champLabel("N° École"));
        champs.add(pleineLargeur(champNumEcole));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Désignation"));
        champs.add(pleineLargeur(champDesign));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Adresse"));
        champs.add(pleineLargeur(champAdresse));
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

        btnAjouter.addActionListener(e -> ajouter());
        btnModifier.addActionListener(e -> modifier());
        btnSupprimer.addActionListener(e -> supprimer());
        btnEffacer.addActionListener(e -> effacerFormulaire());

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
        JPanel wrap = Theme.card("Liste des écoles");
        wrap.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void ajouter() {
        try {
            Ecole ecole = new Ecole(champNumEcole.getText().trim(),
                    champDesign.getText().trim(), champAdresse.getText().trim());
            ecoleService.ajouter(ecole);
            rafraichirTable();
            effacerFormulaire();
            JOptionPane.showMessageDialog(this, "École ajoutée avec succès.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void modifier() {
        try {
            if (champNumEcole.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une école à modifier.");
                return;
            }
            Ecole ecole = new Ecole(champNumEcole.getText().trim(),
                    champDesign.getText().trim(), champAdresse.getText().trim());
            boolean ok = ecoleService.modifier(ecole);
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "École modifiée avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune école trouvée avec ce numéro.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void supprimer() {
        if (champNumEcole.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une école à supprimer.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de l'école " + champNumEcole.getText() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = ecoleService.supprimer(champNumEcole.getText().trim());
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "École supprimée avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune école trouvée avec ce numéro.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de supprimer : des élèves sont rattachés à cette école. "
                    + "Supprimez ou déplacez ces élèves d'abord.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rafraichirTable() {
        try {
            int selectedRow = table.getSelectedRow();
            String selectedId = selectedRow >= 0 ? String.valueOf(tableModel.getValueAt(selectedRow, 0)) : null;
            List<Ecole> ecoles = ecoleService.listerTout();
            tableModel.setRowCount(0);
            for (Ecole e : ecoles) {
                tableModel.addRow(new Object[]{e.getNumEcole(), e.getDesign(), e.getAdresse()});
            }
            if (selectedId != null) reselectionner(selectedId);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void reselectionner(String numEcole) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (String.valueOf(tableModel.getValueAt(i, 0)).equals(numEcole)) {
                table.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void remplirFormulaireDepuisSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        champNumEcole.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        champDesign.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        champAdresse.setText(String.valueOf(tableModel.getValueAt(row, 2)));
    }

    private void effacerFormulaire() {
        champNumEcole.setText("");
        champDesign.setText("");
        champAdresse.setText("");
        table.clearSelection();
    }

    private void afficherErreur(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        rafraichirTable();
    }
}
