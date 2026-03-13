# Visualiseur de graphe — BFS / DFS / Dijkstra / A*

Application Java Swing pour visualiser pas à pas les algorithmes de parcours de graphe.

## Prérequis

- JDK 11 ou supérieur  
  Vérifier avec : `java -version` et `javac -version`

## Lancement

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Windows
```
run.bat
```

### Manuellement
```bash
# Depuis le dossier GraphVisualizer/
mkdir out
javac -d out -sourcepath src $(find src -name "*.java")
java -cp out main.Main
```

## Structure du projet

```
GraphVisualizer/
├── src/
│   ├── main/
│   │   └── Main.java              # Point d'entrée
│   ├── model/
│   │   ├── Graph.java             # Modèle de graphe (noeuds + arêtes)
│   │   └── AlgoStep.java          # Un pas d'algorithme (snapshot)
│   ├── algorithm/
│   │   ├── BFS.java               # Parcours en largeur
│   │   ├── DFS.java               # Parcours en profondeur
│   │   └── Dijkstra.java          # Algorithme de Dijkstra
│   └── gui/
│       ├── MainFrame.java         # Fenêtre principale
│       ├── GraphCanvas.java       # Rendu du graphe (Swing + Graphics2D)
│       └── EdgePanel.java         # Tableau d'édition des arêtes
├── run.sh                         # Script Linux/macOS
├── run.bat                        # Script Windows
└── README.md
```

## Fonctionnalités

- **3 algorithmes** : BFS, DFS, Dijkstra
- **Sélection départ / arrivée** : menus déroulants
- **Nombre de sommets** : de 2 à 10, génération aléatoire
- **Édition des arêtes** :
  - Cliquer deux noeuds dans le graphe pour ajouter une arête
  - Modifier les poids dans le tableau (colonne Poids)
  - Supprimer une arête avec le bouton "Suppr"
  - Ajouter une arête via le formulaire en bas du panneau
- **Navigation pas à pas** : bouton "Étape suivante"
- **Affichage** :
  - Couleurs distinctes par état (non visité, en attente, actuel, visité, départ, arrivée)
  - Distances Dijkstra affichées sous chaque noeud
  - File/pile courante affichée en bas
  - Chemin final reconstruit avec coût total

## Légende des couleurs

| Couleur | État |
|---------|------|
| Bleu clair | Non visité |
| Orange clair | En attente (file/pile) |
| Rouge clair | Noeud actuel |
| Vert clair | Visité |
| Violet clair | Départ |
| Rose clair | Arrivée |
