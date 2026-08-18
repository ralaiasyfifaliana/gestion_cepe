package mg.cepe.interfaces;

import mg.cepe.model.Ecole;
import java.sql.SQLException;
import java.util.List;

public interface IEcoleRepository {
    boolean create(Ecole ecole) throws SQLException;
    Ecole findById(String numEcole) throws SQLException;
    List<Ecole> findAll() throws SQLException;
    boolean update(Ecole ecole) throws SQLException;
    boolean delete(String numEcole) throws SQLException;
}
