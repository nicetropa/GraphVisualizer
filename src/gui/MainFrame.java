package gui;

import algorithm.AStar;
import algorithm.BFS;
import algorithm.DFS;
import algorithm.Dijkstra;
import model.AlgoStep;
import model.Graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String[] LETTERS = {"A","B","C","D","E","F","G","H","I","J"};
    private static final Color BG = new Color(0xF8F7F4);

    private final Graph graph = new Graph();
    private final GraphCanvas canvas = new GraphCanvas(graph);
    private final EdgePanel edgePanel = new EdgePanel(graph);

    private JComboBox<String> algoBox, startBox, endBox;
    private JSpinner nodeSpinner;
    private JButton btnRun, btnStep, btnReset, btnGen;
    private JLabel infoLabel, queueLabel, pathLabel;

    private List<AlgoStep> steps = new ArrayList<>();
    private int stepIdx = 0;
    private boolean running = false;

    public MainFrame() {
        super("Visualiseur de graphe — BFS / DFS / Dijkstra / A*");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG);

        buildTopBar();
        buildCenter();
        buildBottomBar();

        edgePanel.setOnChanged(() -> { canvas.resetStep(isDijkstraOrAStar()); canvas.repaint(); });
        canvas.setAddEdgeListener((u, v) -> {
            if (!graph.hasEdge(u, v)) {
                graph.addEdge(u, v, 1);
                edgePanel.refresh();
                canvas.resetStep(isDijkstraOrAStar());
            }
        });

        generateGraph();

        pack();
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
    }

    private void buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setBackground(BG);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD3D1C7)));

        algoBox = new JComboBox<>(new String[]{"Dijkstra", "A*", "BFS", "DFS"});
        algoBox.addActionListener(e -> {
            edgePanel.setShowWeights(isDijkstraOrAStar());
            canvas.resetStep(isDijkstraOrAStar());
        });

        nodeSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 10, 1));
        nodeSpinner.setPreferredSize(new Dimension(55, 26));

        btnGen = btn("Générer");
        btnGen.addActionListener(e -> generateGraph());

        top.add(label("Algorithme :"));
        top.add(algoBox);
        top.add(Box.createHorizontalStrut(12));
        top.add(label("Sommets :"));
        top.add(nodeSpinner);
        top.add(btnGen);
        top.add(Box.createHorizontalStrut(12));

        startBox = new JComboBox<>();
        endBox   = new JComboBox<>();
        top.add(label("Départ :"));
        top.add(startBox);
        top.add(label("Arrivée :"));
        top.add(endBox);
        top.add(Box.createHorizontalStrut(12));

        btnRun   = btnPrimary("▶  Lancer");
        btnStep  = btn("Étape suivante");
        btnReset = btn("Réinitialiser");
        btnStep.setEnabled(false);

        btnRun.addActionListener(e -> runAlgo());
        btnStep.addActionListener(e -> doStep());
        btnReset.addActionListener(e -> resetAlgo());

        top.add(btnRun);
        top.add(btnStep);
        top.add(btnReset);

        add(top, BorderLayout.NORTH);
    }

    private void buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(BG);
        split.setDividerSize(6);
        split.setResizeWeight(0.72);

        canvas.setBorder(new EmptyBorder(8, 8, 8, 8));
        split.setLeftComponent(canvas);

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(BG);
        right.setBorder(new EmptyBorder(8, 4, 8, 8));
        right.setPreferredSize(new Dimension(230, 400));

        edgePanel.setShowWeights(true);
        right.add(edgePanel, BorderLayout.CENTER);

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        addRow.setBackground(BG);
        addRow.setBorder(new EmptyBorder(4, 0, 0, 0));
        JComboBox<String> addU = new JComboBox<>();
        JComboBox<String> addV = new JComboBox<>();
        JSpinner addW = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        addW.setPreferredSize(new Dimension(48, 24));
        addU.setPreferredSize(new Dimension(50, 24));
        addV.setPreferredSize(new Dimension(50, 24));
        JButton addBtn = btn("+ Ajouter");
        addBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));

        addRow.add(addU); addRow.add(label("→")); addRow.add(addV);
        addRow.add(label("w:")); addRow.add(addW); addRow.add(addBtn);
        right.add(addRow, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String u = (String) addU.getSelectedItem();
            String v = (String) addV.getSelectedItem();
            if (u == null || v == null || u.equals(v)) return;
            if (!graph.hasEdge(u, v)) {
                graph.addEdge(u, v, (Integer) addW.getValue());
                edgePanel.refresh();
                canvas.resetStep(isDijkstraOrAStar());
            }
        });

        startBox.addActionListener(e -> refreshAddBoxes(addU, addV));
        endBox.addActionListener(e -> {});
        refreshAddBoxes(addU, addV);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent we) {
                refreshAddBoxes(addU, addV);
            }
        });

        split.setRightComponent(right);
        add(split, BorderLayout.CENTER);

        startBox.addActionListener(e2 -> refreshAddBoxes(addU, addV));

        Runnable syncAddBoxes = () -> {
            addU.removeAllItems(); addV.removeAllItems();
            for (String n : graph.getNodes()) { addU.addItem(n); addV.addItem(n); }
            if (addV.getItemCount() > 1) addV.setSelectedIndex(1);
        };
        edgePanel.setOnChanged(() -> { syncAddBoxes.run(); canvas.resetStep(isDijkstraOrAStar()); });
    }

    private void refreshAddBoxes(JComboBox<String> addU, JComboBox<String> addV) {
        addU.removeAllItems(); addV.removeAllItems();
        for (String n : graph.getNodes()) { addU.addItem(n); addV.addItem(n); }
        if (addV.getItemCount() > 1) addV.setSelectedIndex(1);
    }

    private void buildBottomBar() {
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.setBackground(BG);
        bot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD3D1C7)),
            new EmptyBorder(6, 10, 6, 10)
        ));

        infoLabel = new JLabel("Configurez le graphe puis cliquez sur Lancer.");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(0x2C2C2A));

        queueLabel = new JLabel(" ");
        queueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        queueLabel.setForeground(new Color(0x5F5E5A));

        pathLabel = new JLabel(" ");
        pathLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        pathLabel.setForeground(new Color(0x27500A));

        bot.add(infoLabel);
        bot.add(Box.createVerticalStrut(2));
        bot.add(queueLabel);
        bot.add(Box.createVerticalStrut(2));
        bot.add(pathLabel);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legend.setBackground(BG);
        legend.add(legendDot(new Color(0xE6F1FB), new Color(0x378ADD), "non visité"));
        legend.add(legendDot(new Color(0xFAEEDA), new Color(0xBA7517), "en attente"));
        legend.add(legendDot(new Color(0xFAECE7), new Color(0x993C1D), "nœud actuel"));
        legend.add(legendDot(new Color(0xEAF3DE), new Color(0x3B6D11), "visité"));
        legend.add(legendDot(new Color(0xEEEDFE), new Color(0x534AB7), "départ"));
        legend.add(legendDot(new Color(0xFBEAF0), new Color(0x993556), "arrivée"));
        bot.add(legend);

        add(bot, BorderLayout.SOUTH);
    }

    private void generateGraph() {
        resetAlgo();
        graph.clear();
        int n = (Integer) nodeSpinner.getValue();
        for (int i = 0; i < n; i++) graph.addNode(LETTERS[i]);
        Random rnd = new Random();
        List<String> ids = new ArrayList<>(graph.getNodes());
        for (int i = 0; i < ids.size() - 1; i++) graph.addEdge(ids.get(i), ids.get(i+1), rnd.nextInt(9)+1);
        if (ids.size() > 2) graph.addEdge(ids.get(0), ids.get(ids.size()-1), rnd.nextInt(9)+1);
        for (int i = 0; i < ids.size() / 2; i++) {
            String a = ids.get(i), b = ids.get((i+2) % ids.size());
            if (!graph.hasEdge(a, b)) graph.addEdge(a, b, rnd.nextInt(9)+1);
        }

        refreshNodeSelects(ids);
        edgePanel.refresh();
        canvas.setGraph(graph);
        canvas.resetStep(isDijkstraOrAStar());
    }

    private void refreshNodeSelects(List<String> ids) {
        startBox.removeAllItems();
        endBox.removeAllItems();
        for (String id : ids) { startBox.addItem(id); endBox.addItem(id); }
        if (ids.size() > 1) endBox.setSelectedIndex(ids.size() - 1);
    }

    private void runAlgo() {
        String start = (String) startBox.getSelectedItem();
        String end   = (String) endBox.getSelectedItem();
        if (start == null || end == null) return;

        String algo = (String) algoBox.getSelectedItem();
        assert algo != null;
        switch (algo) {
            case "BFS"      -> steps = BFS.compute(graph, start, end);
            case "DFS"      -> steps = DFS.compute(graph, start, end);
            case "Dijkstra" -> steps = Dijkstra.compute(graph, start, end);
            case "A*" -> steps = AStar.compute(graph, start, end);
        }
        stepIdx = 0; running = true;
        btnRun.setEnabled(false);
        btnStep.setEnabled(true);
        pathLabel.setText(" ");
        if (!steps.isEmpty()) applyStep(steps.get(stepIdx++));
        if (stepIdx >= steps.size()) finishRun();
    }

    private void doStep() {
        if (stepIdx < steps.size()) {
            applyStep(steps.get(stepIdx++));
            if (stepIdx >= steps.size()) finishRun();
        }
    }

    private void applyStep(AlgoStep step) {
        canvas.setStep(step, isDijkstraOrAStar());
        infoLabel.setText(step.message);

        if (step.queue != null) {
            String algo = (String) algoBox.getSelectedItem();
            String label = "DFS".equals(algo) ? "Pile : " : "File : ";
            queueLabel.setText(label + "[ " + String.join(" , ", step.queue) + " ]");
        } else {
            queueLabel.setText(" ");
        }

        if (step.prev != null) {
            String start = (String) startBox.getSelectedItem();
            String end   = (String) endBox.getSelectedItem();
            List<String> path = reconstructPath(step.prev, start, end);
            if (path != null) {
                String cost = "";
                if (step.dist != null) {
                    Integer d = step.dist.get(end);
                    if (d != null && d != Integer.MAX_VALUE) cost = "  (coût total : " + d + ")";
                }
                pathLabel.setText("Chemin : " + String.join(" → ", path) + cost);
            } else {
                pathLabel.setText("Aucun chemin trouvé.");
            }
        }
    }

    private void finishRun() {
        running = false;
        btnStep.setEnabled(false);
    }

    private void resetAlgo() {
        steps.clear(); stepIdx = 0; running = false;
        btnRun.setEnabled(true);
        btnStep.setEnabled(false);
        infoLabel.setText("Configurez le graphe puis cliquez sur Lancer.");
        queueLabel.setText(" ");
        pathLabel.setText(" ");
        canvas.resetStep(isDijkstraOrAStar());
    }

    private List<String> reconstructPath(Map<String, String> prev, String start, String end) {
        if (prev == null || end == null || !prev.containsKey(end)) return null;
        Deque<String> path = new ArrayDeque<>();
        String cur = end;
        while (cur != null) { path.addFirst(cur); cur = prev.get(cur); }
        if (!path.peekFirst().equals(start)) return null;
        return new ArrayList<>(path);
    }

    private boolean isDijkstraOrAStar() {
        return "Dijkstra".equals(algoBox.getSelectedItem()) || "A*".equals(algoBox.getSelectedItem());
    }

    private JButton btn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return b;
    }

    private JButton btnPrimary(String text) {
        JButton b = btn(text);
        b.setBackground(new Color(0x185FA5));
        b.setForeground(new Color(0xE6F1FB));
        b.setOpaque(true);
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(0x5F5E5A));
        return l;
    }

    private JPanel legendDot(Color fill, Color stroke, String txt) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(BG);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill); g2.fillOval(0, 0, 12, 12);
                g2.setColor(stroke); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, 11, 11);
            }
        };
        dot.setPreferredSize(new Dimension(13, 13));
        dot.setOpaque(false);
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(0x5F5E5A));
        p.add(dot); p.add(l);
        return p;
    }
}
