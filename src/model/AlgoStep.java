package model;

import java.util.*;

public class AlgoStep {
    public enum NodeState { UNVISITED, QUEUED, CURRENT, VISITED, START, END }
    public enum EdgeState { NORMAL, ACTIVE, PATH }

    public final Map<String, NodeState> nodeStates;
    public final Map<String, EdgeState> edgeStates;
    public final String message;
    public final List<String> queue;
    public final Map<String, Integer> dist;
    public final Map<String, String> prev;

    public AlgoStep(Map<String, NodeState> nodeStates,
                    Map<String, EdgeState> edgeStates,
                    String message,
                    List<String> queue,
                    Map<String, Integer> dist,
                    Map<String, String> prev) {
        this.nodeStates = Collections.unmodifiableMap(new LinkedHashMap<>(nodeStates));
        this.edgeStates = Collections.unmodifiableMap(new HashMap<>(edgeStates));
        this.message    = message;
        this.queue      = queue != null ? Collections.unmodifiableList(new ArrayList<>(queue)) : null;
        this.dist       = dist  != null ? Collections.unmodifiableMap(new LinkedHashMap<>(dist)) : null;
        this.prev       = prev  != null ? Collections.unmodifiableMap(new HashMap<>(prev)) : null;
    }
}
