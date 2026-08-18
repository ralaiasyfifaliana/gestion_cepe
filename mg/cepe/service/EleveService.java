package mg.cepe.service;

import mg.cepe.model.Eleve;
import mg.cepe.repository.EleveRepository;

import java.sql.SQLException;
import java.util.List;

public class EleveService {

    private final EleveRepository eleveRepository = new EleveRepository();

    public boolean ajouter(Eleve eleve) throws SQLException {
        if (eleve.getNumEleve() == null || eleve.getNumEleve().isBlank()) {
            throw new IllegalArgumentException("Le numéro d'élève est obligatoire.");
        }
        if (eleve.getNom() == null || eleve.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'élève est obligatoire.");
        }
        return eleveRepository.create(eleve);
    }

    public boolean modifier(Eleve eleve) throws SQLException { return eleveRepository.update(eleve); }
    public boolean supprimer(String numEleve) throws SQLException { return eleveRepository.delete(numEleve); }
    public Eleve rechercherParId(String numEleve) throws SQLException { return eleveRepository.findById(numEleve); }
    public List<Eleve> listerTout() throws SQLException { return eleveRepository.findAll(); }
    public List<Eleve> rechercherParNomOuPrenom(String motCle) throws SQLException {
        return eleveRepository.searchByNomOuPrenom(motCle);
    }
}
