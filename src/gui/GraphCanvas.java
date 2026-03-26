package gui;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;
import model.MazeModel;
import model.MazeStep;
import model.MazeStep.CellState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Panneau de rendu unifié : affiche soit un graphe (Dijkstra / A*)
 * soit un labyrinthe (BFS / DFS) selon le mode actif.
 */
public class GraphCanvas extends JPanel {

    public enum Mode { GRAPH, MAZE }

    private static final int R = 24;
    private static final Color COL_UNVISITED_FILL   = new Color(0xE6F1FB);
    private static final Color COL_UNVISITED_STROKE = new Color(0x378ADD);
    private static final Color COL_UNVISITED_TEXT   = new Color(0x0C447C);
    private static final Color COL_QUEUED_FILL      = new Color(0xFAEEDA);
    private static final Color COL_QUEUED_STROKE    = new Color(0xBA7517);
    private static final Color COL_QUEUED_TEXT      = new Color(0x633806);
    private static final Color COL_CURRENT_FILL     = new Color(0xFAECE7);
    private static final Color COL_CURRENT_STROKE   = new Color(0x993C1D);
    private static final Color COL_CURRENT_TEXT     = new Color(0x712B13);
    private static final Color COL_VISITED_FILL     = new Color(0xEAF3DE);
    private static final Color COL_VISITED_STROKE   = new Color(0x3B6D11);
    private static final Color COL_VISITED_TEXT     = new Color(0x27500A);
    private static final Color COL_START_FILL       = new Color(0xEEEDFE);
    private static final Color COL_START_STROKE     = new Color(0x534AB7);
    private static final Color COL_START_TEXT       = new Color(0x3C3489);
    private static final Color COL_END_FILL         = new Color(0xFBEAF0);
    private static final Color COL_END_STROKE       = new Color(0x993556);
    private static final Color COL_END_TEXT         = new Color(0x72243E);
    private static final Color COL_EDGE_NORMAL      = new Color(0xB4B2A9);
    private static final Color COL_EDGE_ACTIVE      = new Color(0xD85A30);
    private static final Color COL_EDGE_PATH        = new Color(0x3B6D11);

    private static final Color MAZE_WALL     = new Color(0x2C2C2A);
    private static final Color MAZE_FLOOR    = new Color(0xF1EFE8);
    private static final Color MAZE_OPEN     = new Color(0x378ADD);
    private static final Color MAZE_CURRENT  = new Color(0xE24B4A);
    private static final Color MAZE_CLOSED   = new Color(0x85B7EB);
    private static final Color MAZE_ROUTE    = new Color(0x1D9E75);
    private static final Color MAZE_START_BG = new Color(0x534AB7);
    private static final Color MAZE_START_FG = new Color(0xE6F1FB);
    private static final Color MAZE_END_BG   = new Color(0xD4537E);
    private static final Color MAZE_END_FG   = new Color(0xFBEAF0);

    private Mode mode = Mode.GRAPH;

    private Graph  graph;
    private Map<String, Point> positions = new LinkedHashMap<>();
    private AlgoStep graphStep  = null;
    private boolean  showWeights = true;
    private String   selectedNode = null;
    private AddEdgeListener addEdgeListener;

    private MazeModel maze     = null;
    private MazeStep  mazeStep = null;

    public interface AddEdgeListener {
        void onEdgeAdd(String u, String v);
    }

    public GraphCanvas(Graph graph) {
        this.graph = graph;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 420));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mode != Mode.GRAPH || graphStep != null) return;
                String clicked = getNodeAt(e.getPoint());
                if (clicked == null) return;
                if (selectedNode == null) {
                    selectedNode = clicked;
                } else if (selectedNode.equals(clicked)) {
                    selectedNode = null;
                } else {
                    if (addEdgeListener != null) addEdgeListener.onEdgeAdd(selectedNode, clicked);
                    selectedNode = null;
                }
                repaint();
            }
        });
    }


    public void setMode(Mode m)   { this.mode = m; repaint(); }
    public Mode getMode()         { return mode; }

    public void setGraph(Graph graph) {
        this.graph = graph; this.graphStep = null; this.selectedNode = null;
        layoutNodes(); repaint();
    }
    public void setStep(AlgoStep step, boolean showWeights) {
        this.graphStep = step; this.showWeights = showWeights; this.selectedNode = null; repaint();
    }
    public void resetStep(boolean showWeights) {
        this.graphStep = null; this.showWeights = showWeights; this.selectedNode = null; repaint();
    }
    public void setAddEdgeListener(AddEdgeListener l) { this.addEdgeListener = l; }

    public void setMaze(MazeModel maze)   { this.maze = maze; this.mazeStep = null; repaint(); }
    public void setMazeStep(MazeStep s)   { this.mazeStep = s; repaint(); }
    public void resetMazeStep()           { this.mazeStep = null; repaint(); }


    public void layoutNodes() {
        positions.clear();
        if (graph == null) return;
        List<String> nodes = graph.getNodes();
        int n = nodes.size();
        if (n == 0) return;
        int w = getWidth()  > 0 ? getWidth()  : 600;
        int h = getHeight() > 0 ? getHeight() : 420;
        int cx = w / 2, cy = h / 2;
        int radius = Math.min(w, h) / 2 - 55;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            positions.put(nodes.get(i), new Point(
                (int)(cx + radius * Math.cos(angle)),
                (int)(cy + radius * Math.sin(angle))
            ));
        }
    }


    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (mode == Mode.MAZE) paintMaze(g);
        else                   paintGraph(g);
    }


    private void paintMaze(Graphics2D g) {
        if (maze == null) return;
        int cols = maze.getCols(), rows = maze.getRows();
        int w = getWidth(), h = getHeight();
        int cell = Math.min(w / cols, h / rows);
        int offX = (w - cell * cols) / 2;
        int offY = (h - cell * rows) / 2;
        CellState[] states = mazeStep != null ? mazeStep.cellStates : null;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int i  = maze.idx(r, c);
                int px = offX + c * cell;
                int py = offY + r * cell;
                if (maze.getCell(i) == MazeModel.WALL) {
                    g.setColor(MAZE_WALL);
                } else if (states == null) {
                    g.setColor(MAZE_FLOOR);
                } else {
                    g.setColor(switch (states[i]) {
                        case OPEN    -> MAZE_OPEN;
                        case CURRENT -> MAZE_CURRENT;
                        case CLOSED  -> MAZE_CLOSED;
                        case ROUTE   -> MAZE_ROUTE;
                        default      -> MAZE_FLOOR;
                    });
                }
                g.fillRect(px, py, cell, cell);
            }
        }
        drawMazeMarker(g, maze.getStartCell(), offX, offY, cell, MAZE_START_BG, MAZE_START_FG, "S");
        drawMazeMarker(g, maze.getEndCell(),   offX, offY, cell, MAZE_END_BG,   MAZE_END_FG,   "E");
    }

    private void drawMazeMarker(Graphics2D g, int cellIdx, int offX, int offY, int cellSize,
                                 Color bg, Color fg, String letter) {
        int r  = maze.rowOf(cellIdx);
        int c  = maze.colOf(cellIdx);
        int px = offX + c * cellSize;
        int py = offY + r * cellSize;
        g.setColor(bg);
        g.fillRect(px, py, cellSize, cellSize);
        g.setColor(fg);
        int fs = Math.max(8, cellSize - 4);
        g.setFont(new Font("SansSerif", Font.BOLD, fs));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(letter,
            px + (cellSize - fm.stringWidth(letter)) / 2,
            py + (cellSize - fm.getHeight()) / 2 + fm.getAscent());
    }


    private void paintGraph(Graphics2D g) {
        if (graph == null) return;
        if (positions.isEmpty()) layoutNodes();
        drawEdges(g);
        drawNodes(g);
    }

    private void drawEdges(Graphics2D g) {
        for (Graph.Edge e : graph.getEdges()) {
            Point pu = positions.get(e.u);
            Point pv = positions.get(e.v);
            if (pu == null || pv == null) continue;
            EdgeState es = graphStep != null
                ? graphStep.edgeStates.getOrDefault(e.key(), EdgeState.NORMAL) : EdgeState.NORMAL;
            Color col = es == EdgeState.ACTIVE ? COL_EDGE_ACTIVE
                      : es == EdgeState.PATH   ? COL_EDGE_PATH : COL_EDGE_NORMAL;
            float lw = es != EdgeState.NORMAL ? 2.8f : 1.8f;
            double dx = pv.x - pu.x, dy = pv.y - pu.y;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) continue;
            g.setColor(col);
            g.setStroke(new BasicStroke(lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(pu.x + dx/len*R, pu.y + dy/len*R,
                                     pv.x - dx/len*R, pv.y - dy/len*R));
            if (showWeights) {
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.setColor(new Color(0x888780));
                g.drawString(String.valueOf(e.weight), (pu.x+pv.x)/2 + 4, (pu.y+pv.y)/2 - 4);
            }
        }
    }

    private void drawNodes(Graphics2D g) {
        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font distFont  = new Font("SansSerif", Font.PLAIN, 11);
        for (String id : graph.getNodes()) {
            Point p = positions.get(id);
            if (p == null) continue;
            NodeState state = graphStep != null
                ? graphStep.nodeStates.getOrDefault(id, NodeState.UNVISITED) : NodeState.UNVISITED;
            Color fill, stroke, textCol;
            switch (state) {
                case QUEUED:  fill=COL_QUEUED_FILL;    stroke=COL_QUEUED_STROKE;    textCol=COL_QUEUED_TEXT;    break;
                case CURRENT: fill=COL_CURRENT_FILL;   stroke=COL_CURRENT_STROKE;   textCol=COL_CURRENT_TEXT;   break;
                case VISITED: fill=COL_VISITED_FILL;   stroke=COL_VISITED_STROKE;   textCol=COL_VISITED_TEXT;   break;
                case START:   fill=COL_START_FILL;     stroke=COL_START_STROKE;     textCol=COL_START_TEXT;     break;
                case END:     fill=COL_END_FILL;       stroke=COL_END_STROKE;       textCol=COL_END_TEXT;       break;
                default:      fill=COL_UNVISITED_FILL; stroke=COL_UNVISITED_STROKE; textCol=COL_UNVISITED_TEXT;
            }
            if (id.equals(selectedNode)) { fill = COL_QUEUED_FILL; stroke = COL_QUEUED_STROKE; }
            g.setStroke(new BasicStroke(state == NodeState.CURRENT ? 3f : 2f));
            g.setColor(fill);   g.fillOval(p.x-R, p.y-R, 2*R, 2*R);
            g.setColor(stroke); g.drawOval(p.x-R, p.y-R, 2*R, 2*R);
            g.setFont(labelFont);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(textCol);
            g.drawString(id, p.x - fm.stringWidth(id)/2, p.y + fm.getAscent()/2 - 1);
            if (graphStep != null && graphStep.dist != null) {
                Integer d = graphStep.dist.get(id);
                String ds = (d == null || d == Integer.MAX_VALUE) ? "∞" : String.valueOf(d);
                g.setFont(distFont);
                FontMetrics dfm = g.getFontMetrics();
                g.setColor(new Color(0x5F5E5A));
                g.drawString(ds, p.x - dfm.stringWidth(ds)/2, p.y + R + 14);
            }
        }
    }

    private String getNodeAt(Point pt) {
        for (Map.Entry<String, Point> e : positions.entrySet())
            if (pt.distance(e.getValue()) <= R + 4) return e.getKey();
        return null;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (mode == Mode.GRAPH) layoutNodes();
    }
}
