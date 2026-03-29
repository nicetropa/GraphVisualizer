package algorithm;

import model.MazeModel;
import model.MazeStep;
import model.MazeStep.CellState;

import java.util.*;

public class MazeAStar {

    public static List<MazeStep> compute(MazeModel maze) {
        List<MazeStep> steps = new ArrayList<>();
        int n = maze.size();
        int start = maze.getStartCell();
        int end   = maze.getEndCell();

        CellState[] vis  = new CellState[n];
        int[]       from = new int[n];
        int[]       gScore = new int[n];
        int[]       fScore = new int[n];
        
        Arrays.fill(vis, CellState.UNVISITED);
        Arrays.fill(from, -1);
        Arrays.fill(gScore, Integer.MAX_VALUE);
        Arrays.fill(fScore, Integer.MAX_VALUE);
        
        vis[start] = CellState.START;
        vis[end]   = CellState.END;
        gScore[start] = 0;
        fScore[start] = h(maze, start, end);

        java.util.Random rnd = new java.util.Random(maze.getStartCell());
        int[] cellWeights = new int[n];
        for (int i = 0; i < n; i++) cellWeights[i] = rnd.nextInt(9) + 1;

        Set<Integer> closed = new HashSet<>();
        PriorityQueue<Integer> open = new PriorityQueue<>(Comparator.comparingInt(i -> fScore[i]));
        open.add(start);

        steps.add(snap(vis, "A* — départ depuis S. f=" + fScore[start], open.size(), 0));

        outer:
        while (!open.isEmpty()) {
            int cur = open.poll();
            if (closed.contains(cur)) continue;
            closed.add(cur);

            if (cur != start && cur != end) vis[cur] = CellState.CURRENT;
            steps.add(snap(vis, "Cellule courante examinée (f: " + fScore[cur] + ").", open.size(), 0));

            if (cur == end) {
                int len = reconstruct(vis, from, start, end);
                steps.add(snap(vis, "Arrivée atteinte ! Coût final : " + gScore[cur] + ".", open.size(), len));
                break outer;
            }

            for (int nb : maze.neighbors(cur)) {
                if (closed.contains(nb)) continue;

                int tentG = gScore[cur] + cellWeights[nb];
                
                if (tentG < gScore[nb]) {
                    from[nb] = cur;
                    gScore[nb] = tentG;
                    fScore[nb] = tentG + h(maze, nb, end);
                    if (nb != end) vis[nb] = CellState.OPEN;
                    open.add(nb);
                }
            }
            if (cur != start && cur != end) vis[cur] = CellState.CLOSED;
            steps.add(snap(vis, "Voisins évalués.", open.size(), 0));
        }

        return steps;
    }

    private static int h(MazeModel maze, int cur, int end) {
        int r1 = maze.rowOf(cur), c1 = maze.colOf(cur);
        int r2 = maze.rowOf(end), c2 = maze.colOf(end);
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    private static int reconstruct(CellState[] vis, int[] from, int start, int end) {
        int cur = end, len = 0, maxLen = vis.length, guard = 0;
        while (cur != start && cur != -1 && guard++ < maxLen) {
            if (vis[cur] != CellState.END) vis[cur] = CellState.ROUTE;
            cur = from[cur];
            len++;
        }
        return len;
    }

    private static MazeStep snap(CellState[] vis, String msg, int qSize, int routeLen) {
        return new MazeStep(vis, msg, qSize, routeLen);
    }
}
