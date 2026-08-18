package mg.cepe.service;

import mg.cepe.model.Matiere;
import mg.cepe.repository.MatiereRepository;

import java.sql.SQLException;
import java.util.List;

public class MatiereService {

    private final MatiereRepository matiereRepository = new MatiereRepository();

    public boolean ajouter(Matiere matiere) throws SQLException {
        if (matiere.getNumMat() == null || matiere.getNumMat().isBlank()) {
            throw new IllegalArgumentException("Le numéro de matière est obligatoire.");
        }
        if (matiere.getCoef() <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");
        }
        return matiereRepository.create(matiere);
    }

    public boolean modifier(Matiere matiere) throws SQLException { return matiereRepository.update(matiere); }
    public boolean supprimer(String numMat) throws SQLException { return matiereRepository.delete(numMat); }
    public Matiere rechercherParId(String numMat) throws SQLException { return matiereRepository.findById(numMat); }
    public List<Matiere> listerTout() throws SQLException { return matiereRepository.findAll(); }
}
