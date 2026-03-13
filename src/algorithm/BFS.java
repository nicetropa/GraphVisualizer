package algorithm;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;

import java.util.*;

public class BFS {

    public static List<AlgoStep> compute(Graph g, String start, String end) {
        List<AlgoStep> steps = new ArrayList<>();
        Map<String, NodeState> ns = initStates(g, start, end);
        Map<String, EdgeState> es = new HashMap<>();
        Map<String, String> par  = new HashMap<>();
        Set<String> visited      = new HashSet<>();
        Deque<String> queue      = new ArrayDeque<>();

        queue.add(start);
        ns.put(start, NodeState.QUEUED);
        steps.add(snap(ns, es, "Départ : " + start + " ajouté à la file.", queue, null));

        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (visited.contains(cur)) continue;

            ns.put(cur, NodeState.CURRENT);
            steps.add(snap(ns, es, "Visite de " + cur + ".", queue, null));

            if (cur.equals(end)) {
                steps.add(snap(ns, es, "Arrivée " + end + " atteinte !", queue, par));
                break;
            }

            visited.add(cur);

            for (Graph.Edge e : g.getNeighborEdges(cur)) {
                String nb = g.neighborOf(e, cur);
                if (!visited.contains(nb) && !queue.contains(nb)) {
                    par.put(nb, cur);
                    ns.put(nb, NodeState.QUEUED);
                    queue.add(nb);
                    es.put(e.key(), EdgeState.ACTIVE);
                    steps.add(snap(ns, es, nb + " découvert depuis " + cur + ".", queue, null));
                    es.put(e.key(), EdgeState.PATH);
                }
            }

            ns.put(cur, NodeState.VISITED);
            steps.add(snap(ns, es, cur + " marqué visité.", queue, null));
        }

        return steps;
    }

    private static Map<String, NodeState> initStates(Graph g, String start, String end) {
        Map<String, NodeState> ns = new LinkedHashMap<>();
        for (String n : g.getNodes()) ns.put(n, NodeState.UNVISITED);
        ns.put(start, NodeState.START);
        if (!end.equals(start)) ns.put(end, NodeState.END);
        return ns;
    }

    private static AlgoStep snap(Map<String, NodeState> ns, Map<String, EdgeState> es,
                                  String msg, Deque<String> queue, Map<String, String> prev) {
        return new AlgoStep(ns, es, msg, new ArrayList<>(queue), null, prev);
    }
}
