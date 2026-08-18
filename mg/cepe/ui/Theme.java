package mg.cepe.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Palette de couleurs et fabriques de composants pour une interface
 * Swing moderne et cohérente (sans dépendance à une bibliothèque de
 * Look&Feel externe).
 */
public final class Theme {

    public static final Color BG            = new Color(0xF3, 0xF5, 0xF9);
    public static final Color SIDEBAR       = new Color(0x1E, 0x27, 0x3B);
    public static final Color SIDEBAR_HOVER = new Color(0x2A, 0x35, 0x4F);
    public static final Color SIDEBAR_SEL   = new Color(0x2F, 0x6F, 0xED);
    public static final Color PRIMARY       = new Color(0x2F, 0x6F, 0xED);
    public static final Color PRIMARY_DARK  = new Color(0x1F, 0x54, 0xC4);
    public static final Color SUCCESS       = new Color(0x1E, 0xA0, 0x67);
    public static final Color DANGER        = new Color(0xE0, 0x3E, 0x3E);
    public static final Color WARNING       = new Color(0xE0, 0x9A, 0x1E);
    public static final Color CARD          = Color.WHITE;
    public static final Color BORDER        = new Color(0xE2, 0xE5, 0xEC);
    public static final Color TEXT          = new Color(0x1F, 0x29, 0x37);
    public static final Color TEXT_MUTED    = new Color(0x6B, 0x72, 0x80);
    public static final Color TABLE_STRIPE  = new Color(0xF3, 0xF6, 0xFC);
    public static final Color TABLE_HEADER  = new Color(0x22, 0x2C, 0x44);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_NORMAL  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_NAV     = new Font("Segoe UI", Font.PLAIN, 14);

    private Theme() { }

    public static JButton primaryButton(String text) {
        JButton b = baseButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        return b;
    }

    public static JButton successButton(String text) {
        JButton b = baseButton(text);
        b.setBackground(SUCCESS);
        b.setForeground(Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = baseButton(text);
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        return b;
    }

    public static JButton warningButton(String text) {
        JButton b = baseButton(text);
        b.setBackground(WARNING);
        b.setForeground(Color.WHITE);
        return b;
    }

    public static JButton neutralButton(String text) {
        JButton b = baseButton(text);
        b.setBackground(new Color(0xE9, 0xEC, 0xF3));
        b.setForeground(TEXT);
        return b;
    }

    private static JButton baseButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_NORMAL);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    public static JTextField textField(int cols) {
        JTextField f = new JTextField(cols);
        styleField(f);
        return f;
    }

    public static void styleField(JComponent f) {
        f.setFont(FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
    }

    public static JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        if (title != null) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(BORDER, 1, true), title,
                            0, 0, FONT_SECTION, TEXT),
                    new EmptyBorder(6, 10, 10, 10)));
        }
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_NORMAL);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(0xDD, 0xE9, 0xFD));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SECTION.deriveFont(12f));
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    private static class StripedRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_STRIPE);
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SECTION);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }
}
