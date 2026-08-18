package mg.cepe.interfaces;

import mg.cepe.model.Eleve;
import java.sql.SQLException;
import java.util.List;

public interface IEleveRepository {
    boolean create(Eleve eleve) throws SQLException;
    Eleve findById(String numEleve) throws SQLException;
    List<Eleve> findAll() throws SQLException;
    boolean update(Eleve eleve) throws SQLException;
    boolean delete(String numEleve) throws SQLException;
    List<Eleve> searchByNomOuPrenom(String motCle) throws SQLException;
}
