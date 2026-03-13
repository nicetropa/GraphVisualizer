package gui;

import model.AlgoStep;
import model.AlgoStep.NodeState;
import model.AlgoStep.EdgeState;
import model.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GraphCanvas extends JPanel {

    private static final int R = 24;
    private static final Color COL_UNVISITED_FILL  = new Color(0xE6F1FB);
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

    private static final Color COL_EDGE_NORMAL  = new Color(0xB4B2A9);
    private static final Color COL_EDGE_ACTIVE  = new Color(0xD85A30);
    private static final Color COL_EDGE_PATH    = new Color(0x3B6D11);

    private Graph graph;
    private Map<String, Point> positions = new LinkedHashMap<>();
    private AlgoStep currentStep = null;
    private boolean showWeights = true;

    private String selectedNode = null;
    private AddEdgeListener addEdgeListener;

    public interface AddEdgeListener {
        void onEdgeAdd(String u, String v);
    }

    public GraphCanvas(Graph graph) {
        this.graph = graph;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 380));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentStep != null) return;
                String clicked = getNodeAt(e.getPoint());
                if (clicked != null) {
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
            }
        });
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        this.currentStep = null;
        this.selectedNode = null;
        layoutNodes();
        repaint();
    }

    public void setStep(AlgoStep step, boolean showWeights) {
        this.currentStep = step;
        this.showWeights = showWeights;
        this.selectedNode = null;
        repaint();
    }

    public void resetStep(boolean showWeights) {
        this.currentStep = null;
        this.showWeights = showWeights;
        this.selectedNode = null;
        repaint();
    }

    public void setAddEdgeListener(AddEdgeListener l) { this.addEdgeListener = l; }

    public void layoutNodes() {
        positions.clear();
        List<String> nodes = graph.getNodes();
        int n = nodes.size();
        if (n == 0) return;
        int w = getWidth() > 0 ? getWidth() : 600;
        int h = getHeight() > 0 ? getHeight() : 380;
        int cx = w / 2, cy = h / 2, radius = Math.min(w, h) / 2 - 50;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            int x = (int)(cx + radius * Math.cos(angle));
            int y = (int)(cy + radius * Math.sin(angle));
            positions.put(nodes.get(i), new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (positions.isEmpty()) layoutNodes();
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawEdges(g);
        drawNodes(g);
    }

    private void drawEdges(Graphics2D g) {
        for (Graph.Edge e : graph.getEdges()) {
            Point pu = positions.get(e.u);
            Point pv = positions.get(e.v);
            if (pu == null || pv == null) continue;

            EdgeState es = currentStep != null ? currentStep.edgeStates.getOrDefault(e.key(), EdgeState.NORMAL) : EdgeState.NORMAL;
            Color col = es == EdgeState.ACTIVE ? COL_EDGE_ACTIVE : es == EdgeState.PATH ? COL_EDGE_PATH : COL_EDGE_NORMAL;
            float lw  = es != EdgeState.NORMAL ? 2.8f : 1.8f;

            double dx = pv.x - pu.x, dy = pv.y - pu.y;
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len == 0) continue;

            double x1 = pu.x + dx/len*R, y1 = pu.y + dy/len*R;
            double x2 = pv.x - dx/len*R, y2 = pv.y - dy/len*R;

            g.setColor(col);
            g.setStroke(new BasicStroke(lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(x1, y1, x2, y2));

            if (showWeights) {
                int mx = (pu.x + pv.x) / 2, my = (pu.y + pv.y) / 2;
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.setColor(new Color(0x888780));
                g.drawString(String.valueOf(e.weight), mx + 4, my - 4);
            }
        }
    }

    private void drawNodes(Graphics2D g) {
        List<String> nodes = graph.getNodes();
        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font distFont  = new Font("SansSerif", Font.PLAIN, 11);

        for (String id : nodes) {
            Point p = positions.get(id);
            if (p == null) continue;

            NodeState state = currentStep != null ? currentStep.nodeStates.getOrDefault(id, NodeState.UNVISITED) : NodeState.UNVISITED;
            Color fill, stroke, textCol;

            switch (state) {
                case QUEUED:  fill=COL_QUEUED_FILL;  stroke=COL_QUEUED_STROKE;  textCol=COL_QUEUED_TEXT;  break;
                case CURRENT: fill=COL_CURRENT_FILL; stroke=COL_CURRENT_STROKE; textCol=COL_CURRENT_TEXT; break;
                case VISITED: fill=COL_VISITED_FILL; stroke=COL_VISITED_STROKE; textCol=COL_VISITED_TEXT; break;
                case START:   fill=COL_START_FILL;   stroke=COL_START_STROKE;   textCol=COL_START_TEXT;   break;
                case END:     fill=COL_END_FILL;     stroke=COL_END_STROKE;     textCol=COL_END_TEXT;     break;
                default:      fill=COL_UNVISITED_FILL; stroke=COL_UNVISITED_STROKE; textCol=COL_UNVISITED_TEXT;
            }

            if (id.equals(selectedNode)) { fill=COL_QUEUED_FILL; stroke=COL_QUEUED_STROKE; }

            float sw = state == NodeState.CURRENT ? 3f : 2f;
            g.setStroke(new BasicStroke(sw));
            g.setColor(fill);
            g.fillOval(p.x-R, p.y-R, 2*R, 2*R);
            g.setColor(stroke);
            g.drawOval(p.x-R, p.y-R, 2*R, 2*R);

            g.setFont(labelFont);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(textCol);
            g.drawString(id, p.x - fm.stringWidth(id)/2, p.y + fm.getAscent()/2 - 1);

            if (currentStep != null && currentStep.dist != null) {
                Integer d = currentStep.dist.get(id);
                String dStr = (d == null || d == Integer.MAX_VALUE) ? "∞" : String.valueOf(d);
                g.setFont(distFont);
                FontMetrics dfm = g.getFontMetrics();
                g.setColor(new Color(0x5F5E5A));
                g.drawString(dStr, p.x - dfm.stringWidth(dStr)/2, p.y + R + 14);
            }
        }
    }

    private String getNodeAt(Point pt) {
        for (Map.Entry<String, Point> entry : positions.entrySet()) {
            Point p = entry.getValue();
            if (pt.distance(p) <= R + 4) return entry.getKey();
        }
        return null;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        layoutNodes();
    }
}
