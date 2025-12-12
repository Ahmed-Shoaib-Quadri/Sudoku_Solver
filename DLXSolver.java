import java.util.ArrayList;
import java.util.List;

/**
 * DLXSolver for 9x9 Sudoku using Algorithm X + Dancing Links.
 *
 * Usage:
 *   DLXSolver solver = new DLXSolver();
 *   int[][] solved = solver.solve(grid); // grid has 0 for empty cells
 *   // solved == null => no solution
 */
public class DLXSolver {

    // Basic node for dancing links
    private static class Node {
        Node L, R, U, D;
        Column C;
        int r, c, d; // row, col, digit for mapping back (1..9 for d), r,c are 0..8

        Node() {
            L = R = U = D = this;
        }
    }

    // Column header
    private static class Column extends Node {
        int size;
        String name;

        Column(String name) {
            super();
            this.name = name;
            this.size = 0;
            this.C = this; // header references itself
        }
    }

    private Column header;
    private final List<Node> solution;

    public DLXSolver() {
        solution = new ArrayList<>();
    }

    /**
     * Solve given 9x9 grid (0 = empty). Returns solved grid or null if unsolvable.
     */
    public int[][] solve(int[][] grid) {
        buildStructure(grid);
        solution.clear();
        boolean ok = search(0);
        if (!ok) return null;
        return buildSolutionGrid(grid);
    }

    // Build DLX structure from grid
    private void buildStructure(int[][] grid) {
        // Create header and 324 column headers
        header = new Column("header");
        // initialize header horizontal links to itself
        header.R = header.L = header;

        Column[] cols = new Column[324];
        for (int i = 0; i < 324; i++) {
            cols[i] = new Column(Integer.toString(i));
            // insert to the right of header (append)
            cols[i].R = header;
            cols[i].L = header.L;
            header.L.R = cols[i];
            header.L = cols[i];
            cols[i].U = cols[i].D = cols[i];
        }

        // For each candidate (r,c,d) create a row covering 4 columns
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] != 0) {
                    int d = grid[r][c];
                    addRow(cols, r, c, d);
                } else {
                    for (int d = 1; d <= 9; d++) addRow(cols, r, c, d);
                }
            }
        }
    }

    // add a row representing (r,c,d) covering 4 columns:
    // cell constraint (r,c), row constraint (r,d), column constraint (c,d), box constraint (b,d)
    private void addRow(Column[] cols, int r, int c, int d) {
        int cellIndex = r * 9 + c;                 // 0..80
        int rowIndex  = 81 + r * 9 + (d - 1);      // 81..161
        int colIndex  = 162 + c * 9 + (d - 1);     // 162..242
        int box = (r / 3) * 3 + (c / 3);
        int boxIndex  = 243 + box * 9 + (d - 1);   // 243..323

        int[] colIds = new int[] { cellIndex, rowIndex, colIndex, boxIndex };

        Node first = null;
        Node prev = null;

        // create 4 nodes, link vertically into each column, link horizontally as a row
        for (int cid : colIds) {
            Column col = cols[cid];
            Node node = new Node();
            node.C = col;
            node.r = r;
            node.c = c;
            node.d = d;

            // vertical insert at bottom (above column header)
            node.D = col;
            node.U = col.U;
            col.U.D = node;
            col.U = node;
            col.size++;

            if (first == null) first = node;
            if (prev != null) {
                node.L = prev;
                prev.R = node;
            }
            prev = node;
        }
        // close horizontal loop
        first.L = prev;
        prev.R = first;
    }

    private void cover(Column c) {
        c.R.L = c.L;
        c.L.R = c.R;
        for (Node i = c.D; i != c; i = i.D) {
            for (Node j = i.R; j != i; j = j.R) {
                j.D.U = j.U;
                j.U.D = j.D;
                j.C.size--;
            }
        }
    }

    private void uncover(Column c) {
        for (Node i = c.U; i != c; i = i.U) {
            for (Node j = i.L; j != i; j = j.L) {
                j.C.size++;
                j.D.U = j;
                j.U.D = j;
            }
        }
        c.R.L = c;
        c.L.R = c;
    }

    private Column chooseColumn() {
        // choose column with minimum size (heuristic)
        int min = Integer.MAX_VALUE;
        Column best = null;
        for (Node n = header.R; n != header; n = n.R) {
            Column c = (Column) n;
            if (c.size < min) {
                min = c.size;
                best = c;
            }
        }
        return best;
    }

    private boolean search(int k) {
        if (header.R == header) {
            return true; // solved
        }
        Column c = chooseColumn();
        if (c == null || c.size == 0) return false;
        cover(c);
        for (Node r = c.D; r != c; r = r.D) {
            solution.add(r);
            for (Node j = r.R; j != r; j = j.R) cover(j.C);
            if (search(k + 1)) return true;
            // backtrack
            solution.remove(solution.size() - 1);
            for (Node j = r.L; j != r; j = j.L) uncover(j.C);
        }
        uncover(c);
        return false;
    }

    private int[][] buildSolutionGrid(int[][] given) {
        int[][] result = new int[9][9];
        // Start with given to preserve them (not strictly necessary)
        for (int i = 0; i < 9; i++) System.arraycopy(given[i], 0, result[i], 0, 9);
        for (Node n : solution) {
            // find the node in the row that holds r,c,d (any of the row's nodes has same r,c,d)
            Node any = n;
            int r = any.r, c = any.c, d = any.d;
            result[r][c] = d;
        }
        return result;
    }
}
