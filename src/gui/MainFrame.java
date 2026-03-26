package gui;

import algorithm.*;
import model.*;
import model.MazeStep.CellState;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String[] LETTERS = {"A","B","C","D","E","F","G","H","I","J"};
    private static final Color BG = new Color(0xF8F7F4);

    // ── Données ───────────────────────────────────────────────────────────────
    private final Graph graph = new Graph();
    private MazeModel maze = null;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final GraphCanvas canvas = new GraphCanvas(graph);
    private final EdgePanel   edgePanel = new EdgePanel(graph);

    private JComboBox<String> algoBox;
    private JComboBox<String> sizeBox;
    private JSpinner nodeSpinner, seedSpinner, mazeSeedSpinner;
    private JButton btnGen, btnPrepare, btnAuto, btnStep, btnReset, btnRndSeed;
    private JLabel infoLabel, statsLabel, pathLabel;
    private JPanel graphConfigPanel, mazeConfigPanel, edgeContainer;
    private JPanel startEndPanel;
    private JComboBox<String> startBox, endBox;

    private List<AlgoStep>  graphSteps = new ArrayList<>();
    private List<MazeStep>  mazeSteps  = new ArrayList<>();
    private int stepIdx = 0;
    private boolean autoRunning = false;
    private Timer autoTimer = null;
    private JSlider speedSlider;

    private enum UiState { IDLE, READY, RUNNING, DONE }

    public MainFrame() {
        super("Visualiseur — BFS/DFS (labyrinthe) · Dijkstra/A* (graphe)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG);

        buildTopBar();
        buildCenter();
        buildBottomBar();

        edgePanel.setOnChanged(() -> canvas.resetStep(isDijkstraOrAStar()));
        canvas.setAddEdgeListener((u, v) -> {
            if (!graph.hasEdge(u, v)) {
                graph.addEdge(u, v, 1);
                edgePanel.refresh();
                canvas.resetStep(isDijkstraOrAStar());
            }
        });

        switchMode();
        pack();
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);
    }


    private void buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setBackground(BG);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD3D1C7)));

        algoBox = new JComboBox<>(new String[]{"BFS — labyrinthe", "DFS — labyrinthe", "Dijkstra — graphe", "A* — graphe"});
        algoBox.addActionListener(e -> switchMode());

        // Config labyrinthe
        mazeConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        mazeConfigPanel.setBackground(BG);
        sizeBox = new JComboBox<>(new String[]{"11×11","21×21","31×31","41×41"});
        sizeBox.setSelectedIndex(1);
        mazeSeedSpinner = new JSpinner(new SpinnerNumberModel(42, 0, 999999, 1));
        mazeSeedSpinner.setPreferredSize(new Dimension(72, 26));
        btnRndSeed = btn("Aléatoire");
        btnRndSeed.addActionListener(e -> {
            mazeSeedSpinner.setValue((int)(Math.random() * 100000));
            doGenerate();
        });
        mazeConfigPanel.add(label("Taille :"));   mazeConfigPanel.add(sizeBox);
        mazeConfigPanel.add(label("Graine :"));   mazeConfigPanel.add(mazeSeedSpinner);
        mazeConfigPanel.add(btnRndSeed);

        // Config graphe
        graphConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        graphConfigPanel.setBackground(BG);
        nodeSpinner = new JSpinner(new SpinnerNumberModel(7, 3, 10, 1));
        nodeSpinner.setPreferredSize(new Dimension(52, 26));
        seedSpinner = new JSpinner(new SpinnerNumberModel(42, 0, 999999, 1));
        seedSpinner.setPreferredSize(new Dimension(72, 26));
        JButton btnGrnd = btn("Aléatoire");
        btnGrnd.addActionListener(e -> { seedSpinner.setValue((int)(Math.random() * 100000)); doGenerate(); });
        graphConfigPanel.add(label("Sommets :")); graphConfigPanel.add(nodeSpinner);
        graphConfigPanel.add(label("Graine :"));  graphConfigPanel.add(seedSpinner);
        graphConfigPanel.add(btnGrnd);

        // Départ/Arrivée
        startEndPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        startEndPanel.setBackground(BG);
        startBox = new JComboBox<>(); startBox.setPreferredSize(new Dimension(55, 26));
        endBox   = new JComboBox<>(); endBox.setPreferredSize(new Dimension(55, 26));
        startEndPanel.add(label("Départ :"));  startEndPanel.add(startBox);
        startEndPanel.add(label("Arrivée :")); startEndPanel.add(endBox);

        btnGen = btn("Générer");
        btnGen.addActionListener(e -> doGenerate());

        top.add(label("Algorithme :")); top.add(algoBox);
        top.add(mazeConfigPanel);
        top.add(graphConfigPanel);
        top.add(startEndPanel);
        top.add(btnGen);
        add(top, BorderLayout.NORTH);
    }

    private void buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(BG);
        split.setDividerSize(6);
        split.setResizeWeight(0.72);

        canvas.setBorder(new EmptyBorder(8, 8, 8, 8));
        split.setLeftComponent(canvas);

        edgeContainer = new JPanel(new BorderLayout());
        edgeContainer.setBackground(BG);
        edgeContainer.setBorder(new EmptyBorder(8, 4, 8, 8));
        edgeContainer.setPreferredSize(new Dimension(230, 400));
        edgeContainer.add(edgePanel, BorderLayout.CENTER);

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        addRow.setBackground(BG);
        addRow.setBorder(new EmptyBorder(4, 0, 0, 0));
        JComboBox<String> addU = new JComboBox<>(); addU.setPreferredSize(new Dimension(50, 24));
        JComboBox<String> addV = new JComboBox<>(); addV.setPreferredSize(new Dimension(50, 24));
        JSpinner addW = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        addW.setPreferredSize(new Dimension(48, 24));
        JButton addBtn = btn("+ Ajouter");
        addBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        addRow.add(addU); addRow.add(label("→")); addRow.add(addV);
        addRow.add(label("w:")); addRow.add(addW); addRow.add(addBtn);

        addBtn.addActionListener(e -> {
            String u = (String) addU.getSelectedItem(), v = (String) addV.getSelectedItem();
            if (u == null || v == null || u.equals(v)) return;
            if (!graph.hasEdge(u, v)) { graph.addEdge(u, v, (Integer) addW.getValue()); edgePanel.refresh(); canvas.resetStep(isDijkstraOrAStar()); }
        });
        edgeContainer.add(addRow, BorderLayout.SOUTH);

        Runnable syncAdd = () -> {
            addU.removeAllItems(); addV.removeAllItems();
            for (String n : graph.getNodes()) { addU.addItem(n); addV.addItem(n); }
            if (addV.getItemCount() > 1) addV.setSelectedIndex(1);
        };
        edgePanel.setOnChanged(() -> { syncAdd.run(); canvas.resetStep(isDijkstraOrAStar()); });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent we) { syncAdd.run(); }
        });

        split.setRightComponent(edgeContainer);
        add(split, BorderLayout.CENTER);
    }

    private void buildBottomBar() {
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.setBackground(BG);
        bot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD3D1C7)),
            new EmptyBorder(6, 10, 6, 10)));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.setBackground(BG);
        btnPrepare = btnPrimary("Calculer les étapes");
        btnAuto    = btn("▶ Automatique");
        btnStep    = btn("Étape suivante");
        btnReset   = btn("Réinitialiser");
        speedSlider = new JSlider(1, 50, 15);
        speedSlider.setPreferredSize(new Dimension(80, 24));
        speedSlider.setBackground(BG);

        btnPrepare.addActionListener(e -> doPrepare());
        btnAuto.addActionListener(e -> toggleAuto());
        btnStep.addActionListener(e -> doStep());
        btnReset.addActionListener(e -> doReset());

        controls.add(btnPrepare);
        controls.add(btnAuto);
        controls.add(btnStep);
        controls.add(btnReset);
        controls.add(label("Vitesse :"));
        controls.add(speedSlider);
        bot.add(controls);

        infoLabel  = new JLabel("Générez puis calculez les étapes.");
        statsLabel = new JLabel(" ");
        pathLabel  = new JLabel(" ");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(0x5F5E5A));
        pathLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        pathLabel.setForeground(new Color(0x27500A));
        bot.add(infoLabel); bot.add(statsLabel); bot.add(pathLabel);

        JPanel legend = buildLegend();
        bot.add(legend);
        add(bot, BorderLayout.SOUTH);

        setUiState(UiState.IDLE);
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        p.setBackground(BG);
        // légende commune
        p.add(legendDot(new Color(0x534AB7), "départ"));
        p.add(legendDot(new Color(0xD4537E), "arrivée"));
        p.add(legendDot(new Color(0xFAEEDA), "en attente"));
        p.add(legendDot(new Color(0xFAECE7), "actuel"));
        p.add(legendDot(new Color(0x85B7EB), "exploré"));
        p.add(legendDot(new Color(0x1D9E75), "chemin"));
        return p;
    }


    private boolean isMazeMode() {
        int idx = algoBox.getSelectedIndex();
        return idx == 0 || idx == 1;
    }
    private boolean isDijkstraOrAStar() { return !isMazeMode(); }

    private void switchMode() {
        boolean maze = isMazeMode();
        mazeConfigPanel.setVisible(maze);
        graphConfigPanel.setVisible(!maze);
        startEndPanel.setVisible(!maze);
        edgeContainer.setVisible(!maze);
        canvas.setMode(maze ? GraphCanvas.Mode.MAZE : GraphCanvas.Mode.GRAPH);
        edgePanel.setShowWeights(algoBox.getSelectedIndex() == 2); // Dijkstra
        doGenerate();
    }


    private void doGenerate() {
        stopAuto();
        if (isMazeMode()) {
            int size = switch (sizeBox.getSelectedIndex()) { case 0->11; case 2->31; case 3->41; default->21; };
            long seed = ((Number) mazeSeedSpinner.getValue()).longValue();
            maze = new MazeModel(size, seed);
            canvas.setMaze(maze);
            canvas.setMode(GraphCanvas.Mode.MAZE);
        } else {
            generateGraph();
            canvas.setMode(GraphCanvas.Mode.GRAPH);
        }
        clearSteps();
        setUiState(UiState.IDLE);
        infoLabel.setText("Prêt — cliquez sur \"Calculer les étapes\".");
    }

    private void generateGraph() {
        graph.clear();
        int n = (Integer) nodeSpinner.getValue();
        java.util.Random rnd = new java.util.Random(((Number) seedSpinner.getValue()).longValue());
        for (int i = 0; i < n; i++) graph.addNode(LETTERS[i]);
        List<String> ids = new ArrayList<>(graph.getNodes());
        for (int i = 0; i < ids.size()-1; i++) graph.addEdge(ids.get(i), ids.get(i+1), rnd.nextInt(9)+1);
        if (ids.size() > 2) graph.addEdge(ids.get(0), ids.get(ids.size()-1), rnd.nextInt(9)+1);
        for (int i = 0; i < ids.size()/2; i++) {
            String a = ids.get(i), b = ids.get((i+2) % ids.size());
            if (!graph.hasEdge(a, b)) graph.addEdge(a, b, rnd.nextInt(9)+1);
        }
        refreshNodeSelects();
        edgePanel.refresh();
        canvas.setGraph(graph);
        canvas.resetStep(isDijkstraOrAStar());
    }

    private void refreshNodeSelects() {
        List<String> ids = graph.getNodes();
        startBox.removeAllItems(); endBox.removeAllItems();
        for (String id : ids) { startBox.addItem(id); endBox.addItem(id); }
        if (ids.size() > 1) endBox.setSelectedIndex(ids.size()-1);
    }


    private void doPrepare() {
        stopAuto();
        clearSteps();
        int algo = algoBox.getSelectedIndex();
        if (algo == 0) mazeSteps  = algorithm.MazeBFS.compute(maze);
        else if (algo == 1) mazeSteps = algorithm.MazeDFS.compute(maze);
        else {
            String s = (String) startBox.getSelectedItem();
            String e = (String) endBox.getSelectedItem();
            if (s == null || e == null) return;
            if (algo == 2) graphSteps = Dijkstra.compute(graph, s, e);
            else           graphSteps = AStar.compute(graph, s, e);
        }
        stepIdx = 0;
        int total = isMazeMode() ? mazeSteps.size() : graphSteps.size();
        infoLabel.setText(total + " étapes calculées — utilisez \"▶ Automatique\" ou \"Étape suivante\".");
        statsLabel.setText(" ");
        pathLabel.setText(" ");
        // Réinitialise l'affichage
        if (isMazeMode()) canvas.resetMazeStep();
        else              canvas.resetStep(isDijkstraOrAStar());
        setUiState(UiState.READY);
    }


    private void toggleAuto() {
        if (autoRunning) {
            stopAuto();
            setUiState(UiState.READY);
        } else {
            if (stepIdx >= totalSteps()) stepIdx = 0;
            startAuto();
        }
    }

    private void startAuto() {
        autoRunning = true;
        setUiState(UiState.RUNNING);
        int delay = Math.round(1000f / speedSlider.getValue());
        autoTimer = new Timer(delay, e -> {
            if (stepIdx < totalSteps()) {
                applyCurrentStep();
                stepIdx++;
                // Ajuste le délai dynamiquement
                autoTimer.setDelay(Math.round(1000f / speedSlider.getValue()));
            } else {
                stopAuto();
                setUiState(UiState.DONE);
            }
        });
        autoTimer.start();
    }

    private void stopAuto() {
        if (autoTimer != null) { autoTimer.stop(); autoTimer = null; }
        autoRunning = false;
    }

    private void doStep() {
        stopAuto();
        if (stepIdx < totalSteps()) {
            applyCurrentStep();
            stepIdx++;
            if (stepIdx >= totalSteps()) setUiState(UiState.DONE);
            else setUiState(UiState.READY);
        }
    }

    private void doReset() {
        stopAuto();
        clearSteps();
        if (isMazeMode()) canvas.resetMazeStep();
        else              canvas.resetStep(isDijkstraOrAStar());
        infoLabel.setText("Réinitialisé — cliquez sur \"Calculer les étapes\" pour recommencer.");
        statsLabel.setText(" ");
        pathLabel.setText(" ");
        setUiState(UiState.IDLE);
    }

    private void clearSteps() {
        graphSteps.clear(); mazeSteps.clear(); stepIdx = 0;
    }

    private int totalSteps() {
        return isMazeMode() ? mazeSteps.size() : graphSteps.size();
    }


    private void applyCurrentStep() {
        if (isMazeMode()) {
            MazeStep step = mazeSteps.get(stepIdx);
            canvas.setMazeStep(step);
            infoLabel.setText(step.message);
            int explored = 0;
            for (CellState cs : step.cellStates)
                if (cs == CellState.CLOSED || cs == CellState.CURRENT) explored++;
            int route = 0;
            for (CellState cs : step.cellStates) if (cs == CellState.ROUTE) route++;
            String st = "Cellules explorées : " + explored;
            if (step.queueSize >= 0)  st += (algoBox.getSelectedIndex() == 0 ? " · File : " : " · Pile : ") + step.queueSize;
            if (route > 0)            st += " · Chemin : " + route + " cases";
            statsLabel.setText(st);
            if (step.routeLength > 0) pathLabel.setText("Chemin trouvé : " + step.routeLength + " cases.");
        } else {
            AlgoStep step = graphSteps.get(stepIdx);
            canvas.setStep(step, isDijkstraOrAStar());
            infoLabel.setText(step.message);
            long visited = step.nodeStates.values().stream()
                .filter(v -> v == AlgoStep.NodeState.VISITED || v == AlgoStep.NodeState.CURRENT).count();
            statsLabel.setText("Nœuds explorés : " + visited);
            if (step.prev != null) {
                String start = (String) startBox.getSelectedItem();
                String end   = (String) endBox.getSelectedItem();
                List<String> path = reconstructGraphPath(step.prev, start, end);
                if (path != null) {
                    String cost = "";
                    if (step.dist != null) {
                        Integer d = step.dist.get(end);
                        if (d != null && d != Integer.MAX_VALUE) cost = " (coût : " + d + ")";
                    }
                    pathLabel.setText("Chemin : " + String.join(" → ", path) + cost);
                } else {
                    pathLabel.setText("Aucun chemin trouvé.");
                }
            }
        }
    }

    private List<String> reconstructGraphPath(Map<String, String> prev, String start, String end) {
        if (prev == null || !prev.containsKey(end)) return null;
        java.util.Deque<String> path = new java.util.ArrayDeque<>();
        String cur = end;
        int guard = 0;
        while (cur != null && guard++ < 50) { path.addFirst(cur); cur = prev.get(cur); }
        if (!path.peekFirst().equals(start)) return null;
        return new ArrayList<>(path);
    }


    private void setUiState(UiState state) {
        btnPrepare.setEnabled(state == UiState.IDLE);
        btnAuto.setEnabled(state == UiState.READY || state == UiState.RUNNING || state == UiState.DONE);
        btnStep.setEnabled(state == UiState.READY);
        btnReset.setEnabled(state != UiState.IDLE);
        speedSlider.setEnabled(state != UiState.IDLE);
        btnAuto.setText(state == UiState.RUNNING ? "⏸ Pause" : "▶ Automatique");
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
    private JPanel legendDot(Color fill, String txt) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(BG);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill); g.fillOval(0,0,12,12);
            }
        };
        dot.setPreferredSize(new Dimension(13, 13)); dot.setOpaque(false);
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(0x5F5E5A));
        p.add(dot); p.add(l);
        return p;
    }
}
