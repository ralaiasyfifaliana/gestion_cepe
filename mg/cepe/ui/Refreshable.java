package mg.cepe.ui;

/**
 * Implémentée par tous les panneaux qui doivent se rafraîchir automatiquement
 * (rechargement discret des données depuis la base, sans popups d'erreur
 * intempestifs) : appelée périodiquement par {@link MainFrame} ainsi qu'à
 * chaque changement d'onglet / de section du menu.
 */
public interface Refreshable {
    void refresh();
}
