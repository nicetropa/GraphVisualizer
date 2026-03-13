package algorithm;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;

import java.util.*;

public class AStar {

    public static List<AlgoStep> compute(Graph g, String start, String end) {
        List<AlgoStep> steps = new ArrayList<>();

        Map<String, Integer> gScore = new HashMap<>();
        Map<String, Integer> fScore = new HashMap<>();
        Map<String, String>  prev   = new HashMap<>();
        Set<String>          closed = new HashSet<>();

        for (String n : g.getNodes()) {
            gScore.put(n, Integer.MAX_VALUE);
            fScore.put(n, Integer.MAX_VALUE);
        }
        gScore.put(start, 0);
        fScore.put(start, h(start, end));


        PriorityQueue<String> open = new PriorityQueue<>(
            Comparator.comparingInt(n -> fScore.getOrDefault(n, Integer.MAX_VALUE))
        );
        open.add(start);

        Map<String, NodeState> ns = initStates(g, start, end);
        Map<String, EdgeState> es = new HashMap<>();

        ns.put(start, NodeState.QUEUED);
        steps.add(snap(ns, es,
            "Départ depuis " + start + ". Coût parcouru g=0, "
                + "estimation jusqu'à " + end + " h=" + h(start, end)
                + " → priorité f=" + fScore.get(start) + ".",
            gScore, fScore, null));

        while (!open.isEmpty()) {
            String cur = open.poll();

            if (closed.contains(cur)) continue;
            closed.add(cur);
            ns.put(cur, NodeState.CURRENT);

            steps.add(snap(ns, es,
                "On choisit " + cur + " car c'est le nœud avec la plus faible priorité "
                    + "(f=" + fmt(fScore, cur) + " = coût réel " + fmt(gScore, cur)
                    + " + estimation restante " + h(cur, end) + ").",
                gScore, fScore, null));

            if (cur.equals(end)) {
                ns.put(cur, NodeState.VISITED);
                steps.add(snap(ns, es,
                    "Destination " + end + " atteinte ! "
                        + "Coût total du chemin optimal : " + fmt(gScore, end) + ".",
                    gScore, fScore, prev));
                break;
            }

            for (Graph.Edge e : g.getNeighborEdges(cur)) {
                String nb = g.neighborOf(e, cur);
                if (closed.contains(nb)) continue;

                int tentG = gScore.get(cur) + e.weight;
                es.put(e.key(), EdgeState.ACTIVE);

                steps.add(snap(ns, es,
                    "Voisin " + nb + " : passer par " + cur + " coûterait "
                        + fmt(gScore, cur) + "+" + e.weight + "=" + tentG
                        + " (meilleur connu : " + fmt(gScore, nb) + ").",
                    gScore, fScore, null));

                if (tentG < gScore.get(nb)) {
                    prev.put(nb, cur);
                    gScore.put(nb, tentG);
                    fScore.put(nb, tentG + h(nb, end));
                    ns.put(nb, NodeState.QUEUED);
                    if (!open.contains(nb)) open.add(nb);

                    es.put(e.key(), EdgeState.PATH);
                    steps.add(snap(ns, es,
                        "Meilleur chemin vers " + nb + " trouvé via " + cur + " ! "
                            + "Nouveau coût g=" + tentG
                            + ", estimation restante h=" + h(nb, end)
                            + " → priorité f=" + fScore.get(nb) + ".",
                        gScore, fScore, null));
                } else {
                    es.put(e.key(), prev.containsKey(nb) && cur.equals(prev.get(nb))
                        ? EdgeState.PATH : EdgeState.NORMAL);
                    steps.add(snap(ns, es,
                        "Chemin vers " + nb + " via " + cur + " ignoré : "
                            + tentG + " >= meilleur connu " + fmt(gScore, nb) + ".",
                        gScore, fScore, null));
                }
            }

            ns.put(cur, NodeState.VISITED);
            steps.add(snap(ns, es,
                "Tous les voisins de " + cur + " ont été examinés. "
                    + cur + " est définitivement traité (coût final : " + fmt(gScore, cur) + ").",
                gScore, fScore, null));
        }

        return steps;
    }

    private static int h(String node, String end) {
        int nIdx = labelIndex(node);
        int eIdx = labelIndex(end);
        return Math.abs(nIdx - eIdx);
    }

    private static int labelIndex(String id) {
        if (id == null || id.isEmpty()) return 0;
        return id.charAt(0) - 'A';
    }

    private static String fmt(Map<String, Integer> map, String key) {
        Integer v = map.get(key);
        return (v == null || v == Integer.MAX_VALUE) ? "∞" : String.valueOf(v);
    }

    private static Map<String, NodeState> initStates(Graph g, String start, String end) {
        Map<String, NodeState> ns = new LinkedHashMap<>();
        for (String n : g.getNodes()) ns.put(n, NodeState.UNVISITED);
        ns.put(start, NodeState.START);
        if (!end.equals(start)) ns.put(end, NodeState.END);
        return ns;
    }

    private static AlgoStep snap(Map<String, NodeState> ns,
                                  Map<String, EdgeState> es,
                                  String msg,
                                  Map<String, Integer> gScore,
                                  Map<String, Integer> fScore,
                                  Map<String, String>  prev) {
        Map<String, Integer> display = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : fScore.entrySet()) {
            display.put(entry.getKey(),
                entry.getValue() == Integer.MAX_VALUE ? Integer.MAX_VALUE : entry.getValue());
        }
        return new AlgoStep(ns, es, msg, null, display, prev);
    }
}