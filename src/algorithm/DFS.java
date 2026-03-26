package algorithm;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;

import java.util.*;

public class DFS {

    public static List<AlgoStep> compute(Graph g, String start, String end) {
        List<AlgoStep> steps = new ArrayList<>();
        Map<String, NodeState> ns = initStates(g, start, end);
        Map<String, EdgeState> es = new HashMap<>();
        Map<String, String> par   = new HashMap<>();
        Set<String> visited       = new HashSet<>();

        dfs(g, start, end, ns, es, par, visited, steps);

        return steps;
    }

    private static boolean dfs(Graph g, String cur, String end,
                                Map<String, NodeState> ns,
                                Map<String, EdgeState> es,
                                Map<String, String> par,
                                Set<String> visited,
                                List<AlgoStep> steps) {

        visited.add(cur);
        ns.put(cur, NodeState.CURRENT);
        steps.add(snap(ns, es, "Visite " + cur + ".", null));

        if (cur.equals(end)) {
            steps.add(snap(ns, es, "Arrivée " + end + " atteinte !", par));
            return true;
        }

        ns.put(cur, NodeState.VISITED);

        
            for (Graph.Edge e : g.getNeighborEdges(cur)) {
                String nb = g.neighborOf(e, cur);
                if (!visited.contains(nb)) {
                    par.put(nb, cur);
                    ns.put(nb, NodeState.QUEUED);
                    es.put(e.key(), EdgeState.ACTIVE);
                    steps.add(snap(ns, es, "Explore " + cur + " → " + nb + ".", null));

                    Set<String> visitedSnapshot = new HashSet<>(visited);
                    Map<String, String> parSnapshot = new HashMap<>(par);

                    if (dfs(g, nb, end, ns, es, par, visited, steps)) {
                        es.put(e.key(), EdgeState.PATH);
                        return true;
                    }

                    visited.clear();
                    visited.addAll(visitedSnapshot);
                    par.clear();
                    par.putAll(parSnapshot);

                    es.put(e.key(), EdgeState.NORMAL);
                    ns.put(nb, NodeState.UNVISITED);
                    steps.add(snap(ns, es, "Backtrack depuis " + nb + " vers " + cur + ".", null));
        }
}

        return false;
    }

    private static Map<String, NodeState> initStates(Graph g, String start, String end) {
        Map<String, NodeState> ns = new LinkedHashMap<>();
        for (String n : g.getNodes()) ns.put(n, NodeState.UNVISITED);
        ns.put(start, NodeState.START);
        if (!end.equals(start)) ns.put(end, NodeState.END);
        return ns;
    }

    private static AlgoStep snap(Map<String, NodeState> ns, Map<String, EdgeState> es,
                                  String msg, Map<String, String> prev) {
        return new AlgoStep(ns, es, msg, null, null, prev);
    }
}