package model;

import java.util.*;

/**
 * Modèle d'un labyrinthe parfait généré par l'algorithme de Prim randomisé.
 * Utilise un PRNG déterministe (Mulberry32) pour une génération reproductible.
 */
public class MazeModel {

    public static final int WALL  = 0;
    public static final int FLOOR = 1;

    private final int cols;
    private final int rows;
    private final int[] grid;
    private final int startCell;
    private final int endCell;

    public MazeModel(int size, long seed) {
        this.cols = size;
        this.rows = size;
        this.grid = new int[rows * cols];
        Arrays.fill(grid, WALL);

        generate(seed);

        this.startCell = idx(1, 1);
        this.endCell   = idx(rows - 2, cols - 2);
    }


    private void generate(long seed) {
        long[] state = {seed & 0xFFFFFFFFL};

        grid[idx(1, 1)] = FLOOR;
        List<int[]> walls = new ArrayList<>(neighbors2(1, 1, 1, 1));

        while (!walls.isEmpty()) {
            int wi = (int)(nextRnd(state) * walls.size());
            int[] w = walls.remove(wi);
            int r1 = w[0], c1 = w[1], r2 = w[2], c2 = w[3];
            if (grid[idx(r2, c2)] == WALL) {
                grid[idx(r2, c2)] = FLOOR;
                grid[idx((r1 + r2) / 2, (c1 + c2) / 2)] = FLOOR;
                walls.addAll(neighbors2(r2, c2, r2, c2));
            }
        }
    }

    private List<int[]> neighbors2(int r, int c, int fromR, int fromC) {
        List<int[]> list = new ArrayList<>();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1)
                list.add(new int[]{fromR, fromC, nr, nc});
        }
        return list;
    }

    /** Mulberry32 — PRNG déterministe 32 bits */
    private double nextRnd(long[] state) {
        state[0] = (state[0] + 0x6D2B79F5L) & 0xFFFFFFFFL;
        long t = state[0];
        t = (t ^ (t >>> 15)) * ((1 | t) & 0xFFFFFFFFL) & 0xFFFFFFFFL;
        t = (t ^ (t + ((t ^ (t >>> 7)) * ((61 | t) & 0xFFFFFFFFL) & 0xFFFFFFFFL))) & 0xFFFFFFFFL;
        return ((t ^ (t >>> 14)) & 0xFFFFFFFFL) / 4294967296.0;
    }


    public int getCols()      { return cols; }
    public int getRows()      { return rows; }
    public int getStartCell() { return startCell; }
    public int getEndCell()   { return endCell; }

    public int getCell(int i)          { return grid[i]; }
    public int getCell(int r, int c)   { return grid[idx(r, c)]; }
    public int idx(int r, int c)       { return r * cols + c; }
    public int rowOf(int i)            { return i / cols; }
    public int colOf(int i)            { return i % cols; }
    public int size()                  { return rows * cols; }

    /** Voisins FLOOR accessibles depuis la cellule i (4-connectivité) */
    public List<Integer> neighbors(int i) {
        int r = rowOf(i), c = colOf(i);
        List<Integer> result = new ArrayList<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[idx(nr, nc)] == FLOOR)
                result.add(idx(nr, nc));
        }
        return result;
    }
}
