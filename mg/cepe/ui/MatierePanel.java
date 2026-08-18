package mg.cepe.ui;

import mg.cepe.model.Matiere;
import mg.cepe.service.MatiereService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestion des matières (CRUD). Formulaire à gauche, liste à droite.
 */
public class MatierePanel extends JPanel implements Refreshable {

    private final MatiereService matiereService = new MatiereService();

    private final JTextField champNumMat = Theme.textField(14);
    private final JTextField champDesignMat = Theme.textField(14);
    private final JSpinner champCoef = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"N° Matière", "Désignation", "Coefficient"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
    private final JTable table = new JTable(tableModel);

    public MatierePanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(construireFormulaire(), BorderLayout.WEST);
        add(construireTable(), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> remplirFormulaireDepuisSelection());
        rafraichirTable();
    }

    private JPanel construireFormulaire() {
        JPanel outer = Theme.card("Fiche matière");
        outer.setLayout(new BorderLayout());
        outer.setPreferredSize(new Dimension(300, 0));

        JPanel champs = new JPanel();
        champs.setOpaque(false);
        champs.setLayout(new BoxLayout(champs, BoxLayout.Y_AXIS));

        Theme.styleField((JComponent) champCoef.getEditor());

        champs.add(champLabel("N° Matière"));
        champs.add(pleineLargeur(champNumMat));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Désignation"));
        champs.add(pleineLargeur(champDesignMat));
        champs.add(Box.createVerticalStrut(10));
        champs.add(champLabel("Coefficient"));
        champs.add(pleineLargeur(champCoef));
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
        JPanel wrap = Theme.card("Liste des matières");
        wrap.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void ajouter() {
        try {
            Matiere matiere = new Matiere(champNumMat.getText().trim(),
                    champDesignMat.getText().trim(), (Integer) champCoef.getValue());
            matiereService.ajouter(matiere);
            rafraichirTable();
            effacerFormulaire();
            JOptionPane.showMessageDialog(this, "Matière ajoutée avec succès.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void modifier() {
        try {
            if (champNumMat.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une matière à modifier.");
                return;
            }
            Matiere matiere = new Matiere(champNumMat.getText().trim(),
                    champDesignMat.getText().trim(), (Integer) champCoef.getValue());
            boolean ok = matiereService.modifier(matiere);
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "Matière modifiée avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune matière trouvée avec ce numéro.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void supprimer() {
        if (champNumMat.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une matière à supprimer.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirmer la suppression de la matière " + champNumMat.getText() + " ?\n"
                + "Toutes les notes rattachées à cette matière seront également supprimées.",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = matiereService.supprimer(champNumMat.getText().trim());
            if (ok) {
                rafraichirTable();
                effacerFormulaire();
                JOptionPane.showMessageDialog(this, "Matière (et ses notes) supprimée avec succès.");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune matière trouvée avec ce numéro.");
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void rafraichirTable() {
        try {
            int selectedRow = table.getSelectedRow();
            String selectedId = selectedRow >= 0 ? String.valueOf(tableModel.getValueAt(selectedRow, 0)) : null;
            List<Matiere> matieres = matiereService.listerTout();
            tableModel.setRowCount(0);
            for (Matiere m : matieres) {
                tableModel.addRow(new Object[]{m.getNumMat(), m.getDesignMat(), m.getCoef()});
            }
            if (selectedId != null) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (String.valueOf(tableModel.getValueAt(i, 0)).equals(selectedId)) {
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            afficherErreur(ex);
        }
    }

    private void remplirFormulaireDepuisSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        champNumMat.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        champDesignMat.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        champCoef.setValue(Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 2))));
    }

    private void effacerFormulaire() {
        champNumMat.setText("");
        champDesignMat.setText("");
        champCoef.setValue(1);
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
