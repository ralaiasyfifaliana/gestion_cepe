package mg.cepe.interfaces;

import mg.cepe.model.EleveMoyenne;
import mg.cepe.model.Note;
import java.sql.SQLException;
import java.util.List;

public interface INoteRepository {
    boolean create(Note note) throws SQLException;
    Note findById(String anneeScolaire, String numEleve, String numMat) throws SQLException;
    List<Note> findAll() throws SQLException;
    List<Note> findByEleveAndAnnee(String numEleve, String anneeScolaire) throws SQLException;
    boolean update(Note note) throws SQLException;
    boolean delete(String anneeScolaire, String numEleve, String numMat) throws SQLException;
    List<EleveMoyenne> calculerMoyennesParAnnee(String anneeScolaire) throws SQLException;
}
