package algorithm;

import model.MazeModel;
import model.MazeStep;
import model.MazeStep.CellState;

import java.util.*;

public class MazeDFS {

    public static List<MazeStep> compute(MazeModel maze) {
        List<MazeStep> steps = new ArrayList<>();
        int n = maze.size();
        int start = maze.getStartCell();
        int end   = maze.getEndCell();

        CellState[] vis  = new CellState[n];
        int[]       from = new int[n];
        Arrays.fill(vis, CellState.UNVISITED);
        Arrays.fill(from, -1);
        vis[start] = CellState.START;
        vis[end]   = CellState.END;

        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);

        steps.add(snap(vis, "DFS — départ depuis S.", stack.size(), 0));

        outer:
        while (!stack.isEmpty()) {
            int cur = stack.pop();
            if (visited.contains(cur)) continue;
            visited.add(cur);

            if (cur != start && cur != end) vis[cur] = CellState.CURRENT;
            steps.add(snap(vis, "Cellule dépilée.", stack.size(), 0));

            if (cur == end) {
                int len = reconstruct(vis, from, start, end);
                steps.add(snap(vis, "Arrivée atteinte ! Chemin de " + len + " cases.", stack.size(), len));
                break outer;
            }

            if (cur != start && cur != end) vis[cur] = CellState.CLOSED;

            for (int nb : maze.neighbors(cur)) {
                if (!visited.contains(nb)) {
                    from[nb] = cur;
                    if (nb != end) vis[nb] = CellState.OPEN;
                    stack.push(nb);
                }
            }
            steps.add(snap(vis, "Voisins empilés.", stack.size(), 0));
        }

        return steps;
    }

    private static int reconstruct(CellState[] vis, int[] from, int start, int end) {
        int cur = end, len = 0, guard = 0;
        int maxLen = vis.length;
        while (cur != start && cur != -1 && guard++ < maxLen) {
            if (vis[cur] != CellState.END) vis[cur] = CellState.ROUTE;
            cur = from[cur];
            len++;
        }
        return len;
    }

    private static MazeStep snap(CellState[] vis, String msg, int stackSize, int routeLen) {
        return new MazeStep(vis, msg, stackSize, routeLen);
    }
}
