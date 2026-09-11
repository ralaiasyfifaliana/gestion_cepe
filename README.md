# Gestion d'une session CEPE

Application desktop Java (Swing) connectée à PostgreSQL pour la gestion
d'une session d'examen CEPE : écoles, élèves, matières, notes, délibération
et relevés de notes en PDF.

**Aucun Maven requis.** Le projet se compile et se lance directement avec
`javac` / `java`. La seule dépendance externe est le pilote JDBC PostgreSQL
(fourni dans `lib/postgresql.jar`) — la génération des PDF utilise un moteur
maison sans aucune bibliothèque tierce.

## 1. Architecture (POO en couches)

```
gestion-cepe/
├── mg/cepe/
│   ├── Main.java           -> point d'entrée
│   ├── data/                -> connexion JDBC (DBConnection)
│   ├── model/                -> Ecole, Eleve, Matiere, Note, EleveMoyenne, LigneReleve
│   ├── interfaces/            -> contrats CRUD par modèle
│   ├── repository/            -> implémentation SQL des interfaces (JDBC)
│   ├── service/                -> logique métier (délibération, PDF...)
│   ├── pdf/                    -> moteur PDF maison (sans dépendance externe)
│   └── ui/                     -> interface graphique Swing moderne
├── lib/postgresql.jar       -> pilote JDBC PostgreSQL (seule dépendance)
├── sql/create_cepe_db.sql   -> script de création de la base
├── build.sh / build.bat     -> compilation (javac direct)
└── run.sh   / run.bat       -> lancement (java direct)
```

Aucun dossier `src/` ni `target/` : le code source est directement sous
`mg/cepe/...`, et la compilation produit un dossier `classes/`

## 2. Prérequis

- JDK 17 ou plus récent
- PostgreSQL démarré, avec la base **CEPE** déjà créée (`sql/create_cepe_db.sql`)
- Identifiants utilisés par l'application (dans `mg/cepe/data/DBConnection.java`) :
  - URL : `jdbc:postgresql://localhost:5432/CEPE`
  - Utilisateur : `postgres`

## 3. Compilation

**Linux / macOS :**
```bash
cd gestion-cepe
./build.sh
```

**Windows :**
```bat
cd gestion-cepe
build.bat
```

Ceci exécute simplement :
```bash
javac -encoding UTF-8 -cp "lib/*" -d classes $(find mg -name "*.java")
```
Les classes compilées sont placées dans `classes/` (jamais de `target/`).

## 4. Exécution

**Linux / macOS :**
```bash
./run.sh
```

**Windows :**
```bat
run.bat
```

Ceci lance directement (recompile automatiquement si nécessaire) :
```bash
java -cp "classes:lib/*" mg.cepe.Main
```
(sous Windows, remplacer `:` par `;` dans le classpath — déjà géré par `run.bat`)

## 5. Fonctionnalités couvertes

- [x] Navigation en haut de fenêtre (onglets à gauche, "Session CEPE — année"
      à droite ; l'année est modifiable directement dans la barre du haut et
      s'applique à tous les onglets Notes / Résultat / Relevé)
- [x] Écoles / Élèves / Matières : CRUD avec **formulaire à gauche, liste à
      droite**
- [x] Recherche d'un élève par nom ou prénom (`LIKE %mot%`)
- [x] **Saisie des notes par élève** : depuis l'onglet Élèves, on sélectionne
      un élève puis on clique sur « Gérer les notes » — le formulaire de
      l'onglet Notes s'ouvre avec **toutes les matières déjà listées** (pas
      besoin de choisir la matière à chaque note). Le bouton Enregistrer
      n'est actif que lorsque **toutes** les matières ont une note valide
      (0 à 20). Le formulaire reste modifiable après enregistrement.
- [x] **L'onglet Notes est verrouillé** tant qu'aucun élève n'a été choisi
      depuis l'onglet Élèves.
- [x] **Synchronisation automatique** : toutes les listes (élèves, écoles,
      matières, résultats, relevés) se rafraîchissent seules (toutes les
      5 secondes + à chaque changement d'onglet ou d'année de session) —
      plus besoin de cliquer sur "Rafraîchir" pour voir les changements
      faits ailleurs.
- [x] Onglet **Résultat** (anciennement Délibération) :
      - Échecs : moyenne < 9,75/20
      - Admis après délibération : moyenne **exactement égale** à 9,75/20
      - Admis (réussite directe) : moyenne > 9,75/20
      - Admis en classe de 6e : moyenne > 12/20
      - Classement par ordre de mérite : **exclut les élèves en situation
        d'échec**
      - Export PDF de chacune de ces listes
- [x] Onglet **Relevé (PDF)** : liste automatiquement les élèves ayant une
      note pour **chaque matière existante** (relevé complet) pour l'année
      de la session en cours ; un simple clic sur le bouton « 📄 PDF » de la
      ligne génère directement le relevé de l'élève.
- [x] **Suppression en cascade** : supprimer un élève supprime toutes ses
      notes ; supprimer une matière supprime toutes les notes qui y sont
      rattachées (géré nativement par PostgreSQL via `ON DELETE CASCADE`
      dans `sql/create_cepe_db.sql`).
- [x] Interface graphique moderne (thème de couleurs cohérent, tableaux
      zébrés, boutons colorés par action)

## 6. Moteur PDF maison

Pour éviter toute dépendance Maven/tierce, la génération de PDF est réalisée
par les classes `mg.cepe.pdf.PdfDocument` et `mg.cepe.pdf.PdfTableWriter`,
qui écrivent directement la syntaxe PDF (texte, tableaux, pagination
automatique, encodage des accents français). Aucune bibliothèque externe
(type OpenPDF/iText) n'est nécessaire.
