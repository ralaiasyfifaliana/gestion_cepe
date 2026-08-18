package mg.cepe.ui;

import mg.cepe.model.Eleve;

/**
 * Callback déclenché depuis la liste des élèves lorsqu'on clique sur
 * « Gérer les notes » pour un élève sélectionné : bascule vers l'onglet
 * Notes et ouvre le formulaire de saisie pour cet élève.
 */
public interface EleveNotesListener {
    void gererNotes(Eleve eleve);
}
