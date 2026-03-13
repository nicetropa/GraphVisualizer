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
        Map<String, String> par  = new HashMap<>();
        Set<String> visited      = new HashSet<>();
        Deque<String> stack      = new ArrayDeque<>();

        stack.push(start);
        ns.put(start, NodeState.QUEUED);
        steps.add(snap(ns, es, "Départ : " + start + " empilé.", stack, null));

        while (!stack.isEmpty()) {
            String cur = stack.pop();
            if (visited.contains(cur)) continue;

            ns.put(cur, NodeState.CURRENT);
            steps.add(snap(ns, es, "Dépile " + cur + ".", stack, null));

            if (cur.equals(end)) {
                steps.add(snap(ns, es, "Arrivée " + end + " atteinte !", stack, par));
                break;
            }

            visited.add(cur);
            ns.put(cur, NodeState.VISITED);

            List<Graph.Edge> nbEdges = new ArrayList<>(g.getNeighborEdges(cur));
            Collections.reverse(nbEdges);

            for (Graph.Edge e : nbEdges) {
                String nb = g.neighborOf(e, cur);
                if (!visited.contains(nb)) {
                    par.put(nb, cur);
                    ns.put(nb, NodeState.QUEUED);
                    stack.push(nb);
                    es.put(e.key(), EdgeState.ACTIVE);
                    steps.add(snap(ns, es, nb + " empilé depuis " + cur + ".", stack, null));
                    es.put(e.key(), EdgeState.PATH);
                }
            }

            steps.add(snap(ns, es, cur + " visité.", stack, null));
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
                                  String msg, Deque<String> stack, Map<String, String> prev) {
        List<String> list = new ArrayList<>(stack);
        return new AlgoStep(ns, es, msg, list, null, prev);
    }
}
