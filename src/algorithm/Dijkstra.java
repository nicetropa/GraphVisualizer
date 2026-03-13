package algorithm;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;

import java.util.*;

public class Dijkstra {

    public static List<AlgoStep> compute(Graph g, String start, String end) {
        List<AlgoStep> steps = new ArrayList<>();
        Map<String, NodeState> ns = initStates(g, start, end);
        Map<String, EdgeState> es = new HashMap<>();
        Map<String, Integer>  dist = new LinkedHashMap<>();
        Map<String, String>   par  = new HashMap<>();
        Set<String> done = new HashSet<>();

        for (String n : g.getNodes()) dist.put(n, Integer.MAX_VALUE);
        dist.put(start, 0);
        ns.put(start, NodeState.QUEUED);

        steps.add(snap(ns, es, "Init : dist[" + start + "]=0, tous les autres=∞.", dist, null));

        while (done.size() < g.getNodes().size()) {
            String u = minNode(dist, done);
            if (u == null || dist.get(u) == Integer.MAX_VALUE) break;

            done.add(u);
            ns.put(u, NodeState.CURRENT);
            steps.add(snap(ns, es, "Nœud minimum non visité : " + u + " (dist=" + dist.get(u) + ").", dist, null));

            if (u.equals(end)) {
                ns.put(u, NodeState.VISITED);
                steps.add(snap(ns, es, "Arrivée " + end + " atteinte ! Chemin optimal trouvé.", dist, par));
                break;
            }

            for (Graph.Edge e : g.getNeighborEdges(u)) {
                String nb = g.neighborOf(e, u);
                if (done.contains(nb)) continue;

                int alt = dist.get(u) + e.weight;
                es.put(e.key(), EdgeState.ACTIVE);
                String distNbStr = dist.get(nb) == Integer.MAX_VALUE ? "∞" : String.valueOf(dist.get(nb));
                steps.add(snap(ns, es,
                    "Test " + u + "→" + nb + " : " + dist.get(u) + "+" + e.weight + "=" + alt + " vs " + distNbStr + ".",
                    dist, null));

                if (alt < dist.get(nb)) {
                    dist.put(nb, alt);
                    par.put(nb, u);
                    ns.put(nb, NodeState.QUEUED);
                    steps.add(snap(ns, es, "Mise à jour dist[" + nb + "]=" + alt + ".", dist, null));
                }
                es.put(e.key(), par.containsKey(nb) && par.get(nb).equals(u) ? EdgeState.PATH : EdgeState.NORMAL);
            }

            ns.put(u, NodeState.VISITED);
            steps.add(snap(ns, es, u + " finalisé (dist=" + dist.get(u) + ").", dist, null));
        }

        return steps;
    }

    private static String minNode(Map<String, Integer> dist, Set<String> done) {
        String best = null;
        for (Map.Entry<String, Integer> entry : dist.entrySet()) {
            if (!done.contains(entry.getKey())) {
                if (best == null || entry.getValue() < dist.get(best)) best = entry.getKey();
            }
        }
        return best;
    }

    private static Map<String, NodeState> initStates(Graph g, String start, String end) {
        Map<String, NodeState> ns = new LinkedHashMap<>();
        for (String n : g.getNodes()) ns.put(n, NodeState.UNVISITED);
        ns.put(start, NodeState.START);
        if (!end.equals(start)) ns.put(end, NodeState.END);
        return ns;
    }

    private static AlgoStep snap(Map<String, NodeState> ns, Map<String, EdgeState> es,
                                  String msg, Map<String, Integer> dist, Map<String, String> prev) {
        return new AlgoStep(ns, es, msg, null, dist, prev);
    }
}
