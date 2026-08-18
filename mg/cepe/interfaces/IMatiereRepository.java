package mg.cepe.interfaces;

import mg.cepe.model.Matiere;
import java.sql.SQLException;
import java.util.List;

public interface IMatiereRepository {
    boolean create(Matiere matiere) throws SQLException;
    Matiere findById(String numMat) throws SQLException;
    List<Matiere> findAll() throws SQLException;
    boolean update(Matiere matiere) throws SQLException;
    boolean delete(String numMat) throws SQLException;
}
