package mg.cepe.service;

import mg.cepe.model.EleveMoyenne;
import mg.cepe.model.Note;
import mg.cepe.repository.NoteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Règles métier de la session CEPE.
 *
 * Catégories, à partir de la moyenne pondérée d'un élève :
 * - Échec                     : moyenne &lt; 9,75/20
 * - Admis après délibération  : moyenne strictement égale à 9,75/20 (cas limite)
 * - Admis (réussite directe)  : moyenne &gt; 9,75/20
 * - Admis en classe de 6e     : moyenne &gt; 12/20 (sous-ensemble des admis)
 *
 * Le classement par ordre de mérite exclut les élèves en situation d'échec.
 */
public class NoteService {

    /** Seuil de réussite au CEPE. */
    public static final double SEUIL_REUSSITE = 9.75;

    /** Seuil d'admission en classe de 6e (moyenne strictement supérieure à 12). */
    public static final double SEUIL_ADMISSION_SIXIEME = 12.0;

    private final NoteRepository noteRepository = new NoteRepository();

    public boolean ajouter(Note note) throws SQLException {
        if (note.getNote() < 0.0 || note.getNote() > 20.0) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20.");
        }
        return noteRepository.create(note);
    }

    public boolean modifier(Note note) throws SQLException {
        if (note.getNote() < 0.0 || note.getNote() > 20.0) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20.");
        }
        return noteRepository.update(note);
    }

    public boolean supprimer(String anneeScolaire, String numEleve, String numMat) throws SQLException {
        return noteRepository.delete(anneeScolaire, numEleve, numMat);
    }

    public Note rechercherParId(String anneeScolaire, String numEleve, String numMat) throws SQLException {
        return noteRepository.findById(anneeScolaire, numEleve, numMat);
    }

    public List<Note> listerTout() throws SQLException { return noteRepository.findAll(); }

    public List<Note> listerParEleveEtAnnee(String numEleve, String anneeScolaire) throws SQLException {
        return noteRepository.findByEleveAndAnnee(numEleve, anneeScolaire);
    }

    public List<EleveMoyenne> calculerMoyennes(String anneeScolaire) throws SQLException {
        return noteRepository.calculerMoyennesParAnnee(anneeScolaire);
    }

    /** Arrondi à 2 décimales pour comparer proprement les moyennes (tolérance flottante). */
    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Échecs (moyenne &lt; 9,75/20). */
    public List<EleveMoyenne> listerEchecs(String anneeScolaire) throws SQLException {
        return calculerMoyennes(anneeScolaire).stream()
                .filter(em -> round2(em.getMoyenne()) < SEUIL_REUSSITE)
                .collect(Collectors.toList());
    }

    /** Admis après délibération : moyenne exactement égale à 9,75/20 (cas limite). */
    public List<EleveMoyenne> listerAdmisApresDeliberation(String anneeScolaire) throws SQLException {
        return calculerMoyennes(anneeScolaire).stream()
                .filter(em -> round2(em.getMoyenne()) == SEUIL_REUSSITE)
                .collect(Collectors.toList());
    }

    /** Admis (réussite directe) : moyenne strictement supérieure à 9,75/20. */
    public List<EleveMoyenne> listerAdmis(String anneeScolaire) throws SQLException {
        return calculerMoyennes(anneeScolaire).stream()
                .filter(em -> round2(em.getMoyenne()) > SEUIL_REUSSITE)
                .collect(Collectors.toList());
    }

    /** Admis en classe de 6e (moyenne &gt; 12/20). */
    public List<EleveMoyenne> listerAdmisEnSixieme(String anneeScolaire) throws SQLException {
        return calculerMoyennes(anneeScolaire).stream()
                .filter(em -> round2(em.getMoyenne()) > SEUIL_ADMISSION_SIXIEME)
                .collect(Collectors.toList());
    }

    /**
     * Classement par ordre de mérite (moyenne décroissante), en excluant les
     * élèves en situation d'échec (moyenne &lt; 9,75/20).
     */
    public List<EleveMoyenne> classerParOrdreDeMerite(String anneeScolaire) throws SQLException {
        List<EleveMoyenne> classement = calculerMoyennes(anneeScolaire).stream()
                .filter(em -> round2(em.getMoyenne()) >= SEUIL_REUSSITE)
                .collect(Collectors.toList());
        int rang = 1;
        for (EleveMoyenne em : classement) {
            em.setRang(rang++);
        }
        return classement;
    }

    /**
     * Liste des élèves ayant une note pour chaque matière existante
     * (relevé complet) pour l'année scolaire donnée.
     */
    public List<EleveMoyenne> listerElevesAvecReleveComplet(String anneeScolaire, int nbMatieres) throws SQLException {
        if (nbMatieres <= 0) return List.of();
        return noteRepository.findElevesAvecNotesCompletes(anneeScolaire, nbMatieres);
    }
}
