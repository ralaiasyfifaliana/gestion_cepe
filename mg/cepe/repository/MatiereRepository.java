package mg.cepe.repository;

import mg.cepe.data.DBConnection;
import mg.cepe.interfaces.IMatiereRepository;
import mg.cepe.model.Matiere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereRepository implements IMatiereRepository {

    @Override
    public boolean create(Matiere matiere) throws SQLException {
        String sql = "INSERT INTO matiere (nummat, designmat, coef) VALUES (?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matiere.getNumMat());
            ps.setString(2, matiere.getDesignMat());
            ps.setInt(3, matiere.getCoef());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Matiere findById(String numMat) throws SQLException {
        String sql = "SELECT nummat, designmat, coef FROM matiere WHERE nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numMat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Matiere> findAll() throws SQLException {
        String sql = "SELECT nummat, designmat, coef FROM matiere ORDER BY designmat";
        List<Matiere> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public boolean update(Matiere matiere) throws SQLException {
        String sql = "UPDATE matiere SET designmat = ?, coef = ? WHERE nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matiere.getDesignMat());
            ps.setInt(2, matiere.getCoef());
            ps.setString(3, matiere.getNumMat());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String numMat) throws SQLException {
        String sql = "DELETE FROM matiere WHERE nummat = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numMat);
            return ps.executeUpdate() > 0;
        }
    }

    private Matiere mapRow(ResultSet rs) throws SQLException {
        return new Matiere(rs.getString("nummat"), rs.getString("designmat"), rs.getInt("coef"));
    }
}
