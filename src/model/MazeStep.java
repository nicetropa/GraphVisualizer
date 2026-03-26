package model;

import java.util.*;

/**
 * Snapshot d'un état du labyrinthe pendant l'exécution d'un algorithme.
 */
public class MazeStep {

    public enum CellState {
        UNVISITED, OPEN, CURRENT, CLOSED, ROUTE, START, END
    }

    public final CellState[] cellStates;
    public final String message;
    public final int queueSize;   // taille de la file (BFS) ou pile (DFS), -1 si N/A
    public final int routeLength; // longueur du chemin final, 0 si pas encore trouvé

    public MazeStep(CellState[] cellStates, String message, int queueSize, int routeLength) {
        this.cellStates  = cellStates.clone();
        this.message     = message;
        this.queueSize   = queueSize;
        this.routeLength = routeLength;
    }
}
