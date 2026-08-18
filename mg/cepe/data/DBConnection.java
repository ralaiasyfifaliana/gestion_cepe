package mg.cepe.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/CEPE";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "mdp";

    private static Connection connection;

    private DBConnection() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC PostgreSQL introuvable dans le classpath (lib/postgresql.jar).", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    public static void testConnection() throws SQLException {
        Connection c = getConnection();
        if (c == null || c.isClosed()) {
            throw new SQLException("Impossible d'établir la connexion à la base CEPE.");
        }
    }
}
