package mg.cepe.repository;

import mg.cepe.data.DBConnection;
import mg.cepe.interfaces.INoteRepository;
import mg.cepe.model.EleveMoyenne;
import mg.cepe.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteRepository implements INoteRepository {

    @Override
    public boolean create(Note note) throws SQLException {
        String sql = "INSERT INTO note (anneescolaire, numeleve, nummat, note) VALUES (?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, note.getAnneeScolaire());
            ps.setString(2, note.getNumEleve());
            ps.setString(3, note.getNumMat());
            ps.setDouble(4, note.getNote());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Note findById(String anneeScolaire, String numEleve, String numMat) throws SQLException {
        String sql = "SELECT anneescolaire, numeleve, nummat, note FROM note " +
                "WHERE anneescolaire = ? AND numeleve = ? AND nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            ps.setString(2, numEleve);
            ps.setString(3, numMat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Note> findAll() throws SQLException {
        String sql = "SELECT anneescolaire, numeleve, nummat, note FROM note ORDER BY anneescolaire DESC, numeleve";
        List<Note> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public List<Note> findByEleveAndAnnee(String numEleve, String anneeScolaire) throws SQLException {
        String sql = "SELECT anneescolaire, numeleve, nummat, note FROM note WHERE numeleve = ? AND anneescolaire = ?";
        List<Note> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEleve);
            ps.setString(2, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    @Override
    public boolean update(Note note) throws SQLException {
        String sql = "UPDATE note SET note = ? WHERE anneescolaire = ? AND numeleve = ? AND nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, note.getNote());
            ps.setString(2, note.getAnneeScolaire());
            ps.setString(3, note.getNumEleve());
            ps.setString(4, note.getNumMat());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String anneeScolaire, String numEleve, String numMat) throws SQLException {
        String sql = "DELETE FROM note WHERE anneescolaire = ? AND numeleve = ? AND nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            ps.setString(2, numEleve);
            ps.setString(3, numMat);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<EleveMoyenne> calculerMoyennesParAnnee(String anneeScolaire) throws SQLException {
        String sql =
                "SELECT e.numeleve, e.nom, e.prenom, " +
                "       SUM(n.note * m.coef)::float / SUM(m.coef)::float AS moyenne " +
                "FROM note n " +
                "JOIN eleve e   ON n.numeleve = e.numeleve " +
                "JOIN matiere m ON n.nummat   = m.nummat " +
                "WHERE n.anneescolaire = ? " +
                "GROUP BY e.numeleve, e.nom, e.prenom " +
                "ORDER BY moyenne DESC";

        List<EleveMoyenne> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new EleveMoyenne(
                            rs.getString("numeleve"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getDouble("moyenne")
                    ));
                }
            }
        }
        return liste;
    }

    /**
     * Élèves ayant une note pour chaque matière existante (relevé complet)
     * pour une année scolaire donnée, avec leur moyenne pondérée.
     */
    public List<EleveMoyenne> findElevesAvecNotesCompletes(String anneeScolaire, int nbMatieresAttendues) throws SQLException {
        String sql =
                "SELECT e.numeleve, e.nom, e.prenom, " +
                "       SUM(n.note * m.coef)::float / SUM(m.coef)::float AS moyenne, " +
                "       COUNT(DISTINCT n.nummat) AS nb_matieres " +
                "FROM note n " +
                "JOIN eleve e   ON n.numeleve = e.numeleve " +
                "JOIN matiere m ON n.nummat   = m.nummat " +
                "WHERE n.anneescolaire = ? " +
                "GROUP BY e.numeleve, e.nom, e.prenom " +
                "HAVING COUNT(DISTINCT n.nummat) = ? " +
                "ORDER BY e.nom, e.prenom";

        List<EleveMoyenne> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, anneeScolaire);
            ps.setInt(2, nbMatieresAttendues);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new EleveMoyenne(
                            rs.getString("numeleve"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getDouble("moyenne")
                    ));
                }
            }
        }
        return liste;
    }

    private Note mapRow(ResultSet rs) throws SQLException {
        return new Note(rs.getString("anneescolaire"), rs.getString("numeleve"),
                rs.getString("nummat"), rs.getDouble("note"));
    }
}
