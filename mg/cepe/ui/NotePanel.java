package mg.cepe.ui;

import mg.cepe.model.Eleve;
import mg.cepe.model.Matiere;
import mg.cepe.model.Note;
import mg.cepe.service.EleveService;
import mg.cepe.service.MatiereService;
import mg.cepe.service.NoteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Onglet Notes : verrouillé tant qu'aucun élève n'a été choisi depuis
 * l'onglet Élèves (bouton « Gérer les notes »). Une fois ouvert pour un
 * élève, affiche un formulaire listant toutes les matières existantes
 * (pas besoin de les sélectionner une à une) avec un champ de note chacune.
 * L'enregistrement n'est possible que lorsque toutes les matières ont une
 * note valide (0-20) saisie. Le formulaire reste modifiable après
 * enregistrement.
 */
public class NotePanel extends JPanel implements Refreshable {

    private final NoteService noteService = new NoteService();
    private final MatiereService matiereService = new MatiereService();
    private final EleveService eleveService = new EleveService();
    private final Supplier<String> anneeSupplier;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel();

    private static final String CARD_LOCKED = "locked";
    private static final String CARD_FORM = "form";

    private Eleve eleveCourant;
    private final JLabel labelEleve = new JLabel();
    private final JLabel labelAnnee = new JLabel();
    private final JPanel lignesMatieres = new JPanel();
    private final Map<String, JTextField> champsNotes = new LinkedHashMap<>();
    private final Map<String, Boolean> notesExistantes = new LinkedHashMap<>();
    private final JButton btnEnregistrer = Theme.successButton("💾 Enregistrer");
    private final JButton btnAnnuler = Theme.neutralButton("Annuler");
    private final JLabel labelStatutForm = Theme.mutedLabel(" ");

    public NotePanel(Supplier<String> anneeSupplier) {
        this.anneeSupplier = anneeSupplier;
        setLayout(new BorderLayout());
        setOpaque(false);

        cards.setLayout(cardLayout);
        cards.setOpaque(false);
        cards.add(construireEcranVerrouille(), CARD_LOCKED);
        cards.add(construireEcranFormulaire(), CARD_FORM);
        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, CARD_LOCKED);
    }

    private JPanel construireEcranVerrouille() {
        JPanel panel = Theme.card(null);
        panel.setLayout(new GridBagLayout());
        JLabel msg = new JLabel("<html><div style='text-align:center;'>"
                + "🔒 &nbsp;<b>Onglet verrouillé</b><br><br>"
                + "Rendez-vous dans l'onglet <b>Élèves</b>, sélectionnez un élève,<br>"
                + "puis cliquez sur « Gérer les notes de l'élève sélectionné »."
                + "</div></html>");
        msg.setFont(Theme.FONT_NORMAL);
        msg.setForeground(Theme.TEXT_MUTED);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(msg);
        return panel;
    }

    private JPanel construireEcranFormulaire() {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setOpaque(false);

        JPanel header = Theme.card(null);
        header.setLayout(new GridLayout(2, 1, 0, 4));
        labelEleve.setFont(Theme.FONT_TITLE.deriveFont(17f));
        labelEleve.setForeground(Theme.TEXT);
        labelAnnee.setFont(Theme.FONT_NORMAL);
        labelAnnee.setForeground(Theme.TEXT_MUTED);
        header.add(labelEleve);
        header.add(labelAnnee);

        lignesMatieres.setLayout(new BoxLayout(lignesMatieres, BoxLayout.Y_AXIS));
        lignesMatieres.setOpaque(false);
        JScrollPane scroll = new JScrollPane(lignesMatieres);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel tableCard = Theme.card("Notes par matière (0 à 20)");
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        footer.add(labelStatutForm, BorderLayout.WEST);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boutons.setOpaque(false);
        btnAnnuler.addActionListener(e -> { if (eleveCourant != null) ouvrirPourEleve(eleveCourant); });
        btnEnregistrer.addActionListener(e -> enregistrer());
        boutons.add(btnAnnuler);
        boutons.add(btnEnregistrer);
        footer.add(boutons, BorderLayout.EAST);

        outer.add(header, BorderLayout.NORTH);
        outer.add(tableCard, BorderLayout.CENTER);
        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    /** Ouvre le formulaire de saisie des notes pour l'élève donné (appelé depuis ElevePanel). */
    public void ouvrirPourEleve(Eleve eleve) {
        this.eleveCourant = eleve;
        String annee = anneeSupplier.get();
        labelEleve.setText(eleve.getNom() + " " + eleve.getPrenom() + "  (N° " + eleve.getNumEleve() + ")");
        labelAnnee.setText("Année scolaire : " + annee);

        lignesMatieres.removeAll();
        champsNotes.clear();
        notesExistantes.clear();

        try {
            List<Matiere> matieres = matiereService.listerTout();
            List<Note> notesExistantesEleve = noteService.listerParEleveEtAnnee(eleve.getNumEleve(), annee);
            Map<String, Double> valeursExistantes = new LinkedHashMap<>();
            for (Note n : notesExistantesEleve) valeursExistantes.put(n.getNumMat(), n.getNote());

            if (matieres.isEmpty()) {
                JLabel vide = Theme.mutedLabel("Aucune matière n'a encore été créée. Rendez-vous dans l'onglet Matières.");
                lignesMatieres.add(vide);
            }

            for (Matiere m : matieres) {
                lignesMatieres.add(construireLigneMatiere(m, valeursExistantes.get(m.getNumMat())));
                lignesMatieres.add(Box.createVerticalStrut(6));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        lignesMatieres.revalidate();
        lignesMatieres.repaint();
        majEtatBoutonEnregistrer();
        cardLayout.show(cards, CARD_FORM);
    }

    private JPanel construireLigneMatiere(Matiere matiere, Double valeurExistante) {
        JPanel ligne = new JPanel(new BorderLayout(10, 0));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ligne.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nomMatiere = new JLabel(matiere.getDesignMat() + "  (coef. " + matiere.getCoef() + ")");
        nomMatiere.setFont(Theme.FONT_NORMAL);
        ligne.add(nomMatiere, BorderLayout.CENTER);

        JTextField champ = new JTextField(4);
        Theme.styleField(champ);
        champ.setHorizontalAlignment(JTextField.CENTER);
        if (valeurExistante != null) {
            champ.setText(String.valueOf(valeurExistante));
        }
        champ.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { majEtatBoutonEnregistrer(); }
            @Override public void removeUpdate(DocumentEvent e) { majEtatBoutonEnregistrer(); }
            @Override public void changedUpdate(DocumentEvent e) { majEtatBoutonEnregistrer(); }
        });

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        droite.setOpaque(false);
        droite.add(champ);
        droite.add(new JLabel(" / 20"));
        ligne.add(droite, BorderLayout.EAST);

        champsNotes.put(matiere.getNumMat(), champ);
        notesExistantes.put(matiere.getNumMat(), valeurExistante != null);
        return ligne;
    }

    private void majEtatBoutonEnregistrer() {
        boolean complet = !champsNotes.isEmpty();
        for (JTextField champ : champsNotes.values()) {
            Double v = parseNote(champ.getText());
            if (v == null) { complet = false; break; }
        }
        btnEnregistrer.setEnabled(complet);
        labelStatutForm.setText(complet
                ? "Toutes les matières ont une note. Vous pouvez enregistrer."
                : "Renseignez une note (0 à 20) pour chaque matière avant d'enregistrer.");
    }

    private Double parseNote(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty()) return null;
        try {
            double v = Double.parseDouble(text);
            if (v < 0 || v > 20) return null;
            return v;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void enregistrer() {
        if (eleveCourant == null) return;
        String annee = anneeSupplier.get();
        try {
            for (Map.Entry<String, JTextField> entry : champsNotes.entrySet()) {
                String numMat = entry.getKey();
                Double valeur = parseNote(entry.getValue().getText());
                if (valeur == null) return; // sécurité, ne devrait pas arriver (bouton désactivé sinon)
                Note note = new Note(annee, eleveCourant.getNumEleve(), numMat, valeur);
                boolean existait = Boolean.TRUE.equals(notesExistantes.get(numMat));
                if (existait) {
                    noteService.modifier(note);
                } else {
                    noteService.ajouter(note);
                }
            }
            JOptionPane.showMessageDialog(this, "Notes enregistrées avec succès pour "
                    + eleveCourant.getNom() + " " + eleveCourant.getPrenom() + ".");
            ouvrirPourEleve(eleveCourant); // recharge (les notes sont maintenant modifiables)
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        // On ne recharge PAS automatiquement le formulaire de saisie ici :
        // cela écraserait une saisie en cours. On vérifie juste, silencieusement,
        // que l'élève courant existe toujours (sinon on reverrouille l'onglet).
        // Les données (matières, notes déjà enregistrées) sont chargées à chaque
        // ouverture explicite via ouvrirPourEleve(...).
        if (eleveCourant != null) {
            try {
                Eleve toujoursLa = eleveService.rechercherParId(eleveCourant.getNumEleve());
                if (toujoursLa == null) {
                    eleveCourant = null;
                    cardLayout.show(cards, CARD_LOCKED);
                }
            } catch (SQLException ignorable) {
                // Rafraîchissement silencieux : on ignore les erreurs transitoires.
            }
        }
    }
}
