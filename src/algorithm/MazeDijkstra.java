package algorithm;

import model.MazeModel;
import model.MazeStep;
import model.MazeStep.CellState;

import java.util.*;

public class MazeDijkstra {

    public static List<MazeStep> compute(MazeModel maze) {
        List<MazeStep> steps = new ArrayList<>();
        int n = maze.size();
        int start = maze.getStartCell();
        int end   = maze.getEndCell();

        CellState[] vis  = new CellState[n];
        int[]       from = new int[n];
        int[]       dist = new int[n];
        Arrays.fill(vis, CellState.UNVISITED);
        Arrays.fill(from, -1);
        Arrays.fill(dist, Integer.MAX_VALUE);
        
        vis[start] = CellState.START;
        vis[end]   = CellState.END;
        dist[start] = 0;

        java.util.Random rnd = new java.util.Random(maze.getStartCell());
        int[] cellWeights = new int[n];
        for (int i = 0; i < n; i++) cellWeights[i] = rnd.nextInt(9) + 1;

        Set<Integer> visited = new HashSet<>();
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(i -> dist[i]));
        pq.add(start);

        steps.add(snap(vis, "Dijkstra — départ depuis S.", pq.size(), 0));

        outer:
        while (!pq.isEmpty()) {
            int cur = pq.poll();
            if (visited.contains(cur)) continue;
            visited.add(cur);

            if (cur != start && cur != end) vis[cur] = CellState.CURRENT;
            steps.add(snap(vis, "Cellule courante examinée (coût: " + dist[cur] + ").", pq.size(), 0));

            if (cur == end) {
                int len = reconstruct(vis, from, start, end);
                steps.add(snap(vis, "Arrivée atteinte ! Coût du chemin : " + dist[cur] + ".", pq.size(), len));
                break outer;
            }

            for (int nb : maze.neighbors(cur)) {
                if (!visited.contains(nb)) {
                    int tentDist = dist[cur] + cellWeights[nb];
                    if (tentDist < dist[nb]) {
                        dist[nb] = tentDist;
                        from[nb] = cur;
                        if (nb != end) vis[nb] = CellState.OPEN;
                        pq.add(nb);
                    }
                }
            }
            if (cur != start && cur != end) vis[cur] = CellState.CLOSED;
            steps.add(snap(vis, "Voisins mis à jour.", pq.size(), 0));
        }

        return steps;
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
