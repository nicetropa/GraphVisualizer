package gui;

import model.Graph;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EdgePanel extends JPanel {

    private final Graph graph;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private Runnable onChanged;
    private boolean showWeights = true;

    public EdgePanel(Graph graph) {
        this.graph = graph;
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createTitledBorder("Arêtes"));

        tableModel = new DefaultTableModel(new Object[]{"De", "Vers", "Poids", ""}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2 || c == 3; }
            @Override public Class<?> getColumnClass(int c) { return c == 2 ? Integer.class : Object.class; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(70);

        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), graph, this));

        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2) {
                int row = e.getFirstRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    String u = (String) tableModel.getValueAt(row, 0);
                    String v = (String) tableModel.getValueAt(row, 1);
                    Object wObj = tableModel.getValueAt(row, 2);
                    if (wObj instanceof Integer) {
                        int w = Math.max(1, (Integer) wObj);
                        graph.setWeight(u, v, w);
                        if (onChanged != null) onChanged.run();
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setOnChanged(Runnable r) { this.onChanged = r; }

    public void setShowWeights(boolean show) {
        this.showWeights = show;
        table.getColumnModel().getColumn(2).setMaxWidth(show ? 200 : 0);
        table.getColumnModel().getColumn(2).setMinWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(show ? 60 : 0);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (Graph.Edge e : graph.getEdges()) {
            tableModel.addRow(new Object[]{e.u, e.v, e.weight, "Suppr"});
        }
    }

    public void notifyChanged() { if (onChanged != null) onChanged.run(); }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v != null ? v.toString() : "");
            setForeground(new Color(0xA32D2D));
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final Graph graph;
        private final EdgePanel panel;
        private String label;
        private int row;

        public ButtonEditor(JCheckBox cb, Graph graph, EdgePanel panel) {
            super(cb);
            this.graph = graph;
            this.panel = panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            label = v != null ? v.toString() : "";
            row = r;
            JButton btn = new JButton(label);
            btn.setForeground(new Color(0xA32D2D));
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            btn.addActionListener(e -> {
                stopCellEditing();
                String u = (String) panel.tableModel.getValueAt(row, 0);
                String v2 = (String) panel.tableModel.getValueAt(row, 1);
                graph.removeEdge(u, v2);
                panel.refresh();
                panel.notifyChanged();
            });
            return btn;
        }

        @Override public Object getCellEditorValue() { return label; }
    }
}
