package model;

import java.util.*;

public class Graph {
    private final List<String> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void addNode(String id) {
        if (!nodes.contains(id)) nodes.add(id);
    }

    public void addEdge(String u, String v, int weight) {
        if (!hasEdge(u, v)) edges.add(new Edge(u, v, weight));
    }

    public boolean removeEdge(String u, String v) {
        return edges.removeIf(e -> e.connects(u, v));
    }

    public boolean hasEdge(String u, String v) {
        return edges.stream().anyMatch(e -> e.connects(u, v));
    }

    public void setWeight(String u, String v, int w) {
        edges.stream().filter(e -> e.connects(u, v)).findFirst().ifPresent(e -> e.weight = w);
    }

    public List<String> getNodes() { return Collections.unmodifiableList(nodes); }
    public List<Edge> getEdges()   { return Collections.unmodifiableList(edges); }

    public List<Edge> getNeighborEdges(String u) {
        List<Edge> result = new ArrayList<>();
        for (Edge e : edges) {
            if (e.u.equals(u) || e.v.equals(u)) result.add(e);
        }
        return result;
    }

    public String neighborOf(Edge e, String u) {
        return e.u.equals(u) ? e.v : e.u;
    }

    public static class Edge {
        public final String u, v;
        public int weight;

        public Edge(String u, String v, int weight) {
            this.u = u; this.v = v; this.weight = weight;
        }

        public boolean connects(String a, String b) {
            return (u.equals(a) && v.equals(b)) || (u.equals(b) && v.equals(a));
        }

        public String key() {
            String[] s = {u, v};
            Arrays.sort(s);
            return s[0] + "-" + s[1];
        }
    }
}
