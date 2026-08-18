package mg.cepe.service;

import mg.cepe.model.Ecole;
import mg.cepe.repository.EcoleRepository;

import java.sql.SQLException;
import java.util.List;

public class EcoleService {

    private final EcoleRepository ecoleRepository = new EcoleRepository();

    public boolean ajouter(Ecole ecole) throws SQLException {
        if (ecole.getNumEcole() == null || ecole.getNumEcole().isBlank()) {
            throw new IllegalArgumentException("Le numéro d'école est obligatoire.");
        }
        if (ecole.getDesign() == null || ecole.getDesign().isBlank()) {
            throw new IllegalArgumentException("La désignation de l'école est obligatoire.");
        }
        return ecoleRepository.create(ecole);
    }

    public boolean modifier(Ecole ecole) throws SQLException { return ecoleRepository.update(ecole); }
    public boolean supprimer(String numEcole) throws SQLException { return ecoleRepository.delete(numEcole); }
    public Ecole rechercherParId(String numEcole) throws SQLException { return ecoleRepository.findById(numEcole); }
    public List<Ecole> listerTout() throws SQLException { return ecoleRepository.findAll(); }
}
