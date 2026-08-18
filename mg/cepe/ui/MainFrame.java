package mg.cepe.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fenêtre principale : barre de navigation tout en haut (onglets à gauche,
 * titre de la session + année à droite) et zone de contenu en dessous.
 * Les pages se rafraîchissent automatiquement (minuteur périodique +
 * rafraîchissement à chaque changement de section ou d'année de session).
 */
public class MainFrame extends JFrame {

    /** Intervalle de rafraîchissement automatique des données affichées. */
    private static final int AUTO_REFRESH_MS = 30000;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String currentKey;
    private final JLabel statusLabel = new JLabel();
    private final JTextField anneeField = new JTextField("2022-2023", 9);

    private NotePanel notePanel;

    public MainFrame() {
        super("Gestion de la session CEPE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 700));
        setSize(1250, 780);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);

        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.setBackground(Theme.BG);
        add(content, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        Supplier<String> anneeSupplier = this::getAnneeScolaire;

        EcolePanel ecolePanel = new EcolePanel();
        ElevePanel elevePanel = new ElevePanel();
        MatierePanel matierePanel = new MatierePanel();
        notePanel = new NotePanel(anneeSupplier);
        ResultatPanel resultatPanel = new ResultatPanel(anneeSupplier);
        RelevePanel relevePanel = new RelevePanel(anneeSupplier);

        elevePanel.setNotesListener(eleve -> {
            showPage("notes");
            notePanel.ouvrirPourEleve(eleve);
        });

        registerPage("ecoles", ecolePanel);
        registerPage("eleves", elevePanel);
        registerPage("matieres", matierePanel);
        registerPage("notes", notePanel);
        registerPage("resultat", resultatPanel);
        registerPage("releve", relevePanel);

        showPage("ecoles");

        anneeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCurrentPage(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCurrentPage(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCurrentPage(); }
        });

        Timer autoRefresh = new Timer(AUTO_REFRESH_MS, e -> refreshCurrentPage());
        autoRefresh.start();
    }

    public String getAnneeScolaire() {
        return anneeField.getText().trim();
    }

    private void registerPage(String key, JComponent panel) {
        pages.put(key, panel);
        content.add(panel, key);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SIDEBAR);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        navRow.setOpaque(false);
        navRow.setBorder(new EmptyBorder(8, 12, 8, 0));
        navRow.add(navButton("ecoles", "🏫  Écoles"));
        navRow.add(navButton("eleves", "🧑‍🎓  Élèves"));
        navRow.add(navButton("matieres", "📘  Matières"));
        navRow.add(navButton("notes", "📝  Notes"));
        navRow.add(navButton("resultat", "🏆  Résultat"));
        navRow.add(navButton("releve", "📄  Relevé (PDF)"));
        bar.add(navRow, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(new EmptyBorder(8, 0, 8, 16));

        JLabel title = new JLabel("Session CEPE —");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        Theme.styleField(anneeField);
        anneeField.setHorizontalAlignment(JTextField.CENTER);
        anneeField.setFont(new Font("Segoe UI", Font.BOLD, 14));

        titleBlock.add(title);
        titleBlock.add(anneeField);
        bar.add(titleBlock, BorderLayout.EAST);

        return bar;
    }

    private JButton navButton(String key, String label) {
        JButton b = new JButton(label);
        b.setFont(Theme.FONT_NAV);
        b.setForeground(Color.WHITE);
        b.setBackground(Theme.SIDEBAR);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!key.equals(currentKey)) b.setBackground(Theme.SIDEBAR_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!key.equals(currentKey)) b.setBackground(Theme.SIDEBAR);
            }
        });
        b.addActionListener(e -> showPage(key));
        navButtons.put(key, b);
        return b;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(6, 20, 6, 20)));
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Prêt.");
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel autoLabel = new JLabel("● Rafraîchissement automatique actif");
        autoLabel.setFont(Theme.FONT_SMALL);
        autoLabel.setForeground(Theme.SUCCESS);
        bar.add(autoLabel, BorderLayout.EAST);
        return bar;
    }

    private void showPage(String key) {
        currentKey = key;
        cardLayout.show(content, key);
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            boolean selected = e.getKey().equals(key);
            e.getValue().setBackground(selected ? Theme.SIDEBAR_SEL : Theme.SIDEBAR);
        }
        refreshCurrentPage();
    }

    private void refreshCurrentPage() {
        JComponent page = pages.get(currentKey);
        if (page instanceof Refreshable) {
            ((Refreshable) page).refresh();
            statusLabel.setText("Dernière mise à jour : " + java.time.LocalTime.now().withNano(0).toString());
        }
    }
}
