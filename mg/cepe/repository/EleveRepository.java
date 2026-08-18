package mg.cepe.repository;

import mg.cepe.data.DBConnection;
import mg.cepe.interfaces.IEleveRepository;
import mg.cepe.model.Eleve;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleveRepository implements IEleveRepository {

    @Override
    public boolean create(Eleve eleve) throws SQLException {
        String sql = "INSERT INTO eleve (numeleve, numecole, nom, prenom) VALUES (?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, eleve.getNumEleve());
            ps.setString(2, eleve.getNumEcole());
            ps.setString(3, eleve.getNom());
            ps.setString(4, eleve.getPrenom());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Eleve findById(String numEleve) throws SQLException {
        String sql = "SELECT numeleve, numecole, nom, prenom FROM eleve WHERE numeleve = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEleve);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Eleve> findAll() throws SQLException {
        String sql = "SELECT numeleve, numecole, nom, prenom FROM eleve ORDER BY nom, prenom";
        List<Eleve> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public boolean update(Eleve eleve) throws SQLException {
        String sql = "UPDATE eleve SET numecole = ?, nom = ?, prenom = ? WHERE numeleve = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, eleve.getNumEcole());
            ps.setString(2, eleve.getNom());
            ps.setString(3, eleve.getPrenom());
            ps.setString(4, eleve.getNumEleve());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String numEleve) throws SQLException {
        String sql = "DELETE FROM eleve WHERE numeleve = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEleve);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Eleve> searchByNomOuPrenom(String motCle) throws SQLException {
        String sql = "SELECT numeleve, numecole, nom, prenom FROM eleve " +
                "WHERE nom ILIKE ? OR prenom ILIKE ? ORDER BY nom, prenom";
        List<Eleve> liste = new ArrayList<>();
        String motif = "%" + motCle + "%";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, motif);
            ps.setString(2, motif);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    private Eleve mapRow(ResultSet rs) throws SQLException {
        return new Eleve(rs.getString("numeleve"), rs.getString("numecole"),
                rs.getString("nom"), rs.getString("prenom"));
    }
}
