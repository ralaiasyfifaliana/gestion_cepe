package mg.cepe.repository;

import mg.cepe.data.DBConnection;
import mg.cepe.interfaces.IEcoleRepository;
import mg.cepe.model.Ecole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EcoleRepository implements IEcoleRepository {

    @Override
    public boolean create(Ecole ecole) throws SQLException {
        String sql = "INSERT INTO ecole (numecole, design, adresse) VALUES (?, ?, ?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ecole.getNumEcole());
            ps.setString(2, ecole.getDesign());
            ps.setString(3, ecole.getAdresse());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Ecole findById(String numEcole) throws SQLException {
        String sql = "SELECT numecole, design, adresse FROM ecole WHERE numecole = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEcole);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Ecole> findAll() throws SQLException {
        String sql = "SELECT numecole, design, adresse FROM ecole ORDER BY numecole";
        List<Ecole> liste = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public boolean update(Ecole ecole) throws SQLException {
        String sql = "UPDATE ecole SET design = ?, adresse = ? WHERE numecole = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ecole.getDesign());
            ps.setString(2, ecole.getAdresse());
            ps.setString(3, ecole.getNumEcole());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String numEcole) throws SQLException {
        String sql = "DELETE FROM ecole WHERE numecole = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEcole);
            return ps.executeUpdate() > 0;
        }
    }

    private Ecole mapRow(ResultSet rs) throws SQLException {
        return new Ecole(rs.getString("numecole"), rs.getString("design"), rs.getString("adresse"));
    }
}
