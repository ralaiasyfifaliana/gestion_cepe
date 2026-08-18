package mg.cepe;

import mg.cepe.data.DBConnection;
import mg.cepe.ui.MainFrame;
import mg.cepe.ui.Theme;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Point d'entrée de l'application "Gestion d'une session CEPE".
 * Se lance directement avec la commande java (voir README.md / run.sh),
 * sans Maven.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorable) {
            // Non bloquant : on garde le look and feel par défaut.
        }
        UIManager.put("ToolTip.background", Theme.CARD);

        try {
            DBConnection.testConnection();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Impossible de se connecter à la base de données PostgreSQL 'CEPE'.\n" +
                    "Vérifiez que PostgreSQL est démarré et que la base CEPE existe déjà\n" +
                    "(sql/create_cepe_db.sql), avec l'utilisateur postgres / mot de passe mdp.\n\n" +
                    "Détail : " + ex.getMessage(),
                    "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeConnection));
    }
}
