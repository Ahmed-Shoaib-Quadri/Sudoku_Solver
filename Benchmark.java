import java.util.Arrays;

/**
 * Benchmark runner comparing DLX (DLXSolver) vs Backtracking (local implementation).
 *
 * Usage:
 *   javac *.java
 *   java Benchmark
 *
 * Make sure DLXSolver.java is in the same directory and compiled.
 */
public class Benchmark {

    // A small suite of Sudoku puzzles (0 = empty). Each puzzle is given as 9 lines joined into one string.
    // Source: curated mix (easy, medium, hard). You can add more puzzles to the array.
    private static final String[] PUZZLES = new String[] {
        // Easy
        "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
        // Medium
        "000260701680070090190004500820100040004602900050003028009300074040050036703018000",
        // Hard
        "005300000800000020070010500400005300010070006003200080060500009004000030000009700",
        // Very Hard (from top-judge sets)
        "100007090030020008009600500005300900010080002600004000300000010040000007007000300",
        // Expert / Hardest-ish
        "000000907000420180000705026100904000050000040000507009920108000034059000507000000",
        // Another hard
        "048000000000020000000000030000401000003000100000206000010000000000070000000000860",
        // A tough (few givens)
        "000000000000003085001020000000507000004000100090000000500000073000000000000000000"
    };

    public static void main(String[] args) {
        System.out.println("Sudoku solver benchmark — DLX vs Backtracking");
        System.out.println("Running " + PUZZLES.length + " puzzles...\n");

        long totalDlx = 0;
        long totalBack = 0;
        int solvedByBoth = 0;

        for (int idx = 0; idx < PUZZLES.length; idx++) {
            String p = PUZZLES[idx];
            int[][] grid = parsePuzzle(p);
            System.out.println("Puzzle " + (idx + 1) + " (grid):");
            printGrid(grid);

            // DLX run
            DLXSolver dlx = new DLXSolver();
            int[][] copyForDlx = deepCopy(grid);
            long start = System.nanoTime();
            int[][] dlxSolution = dlx.solve(copyForDlx);
            long dlxMs = (System.nanoTime() - start) / 1_000_000L;

            // Backtracking run
            int[][] copyForBack = deepCopy(grid);
            start = System.nanoTime();
            boolean backSolved = backtrackSolve(copyForBack);
            long backMs = (System.nanoTime() - start) / 1_000_000L;

            // Verify and compare
            boolean dlxSolved = dlxSolution != null;
            boolean same = false;
            if (dlxSolved && backSolved) {
                same = Arrays.deepEquals(dlxSolution, copyForBack);
            }

            System.out.printf(" DLX:         %s    time: %4d ms\n", (dlxSolved ? "SOLVED" : "UNSOLV"), dlxMs);
            System.out.printf(" Backtracking:%s    time: %4d ms\n", (backSolved ? "SOLVED" : "UNSOLV"), backMs);
            System.out.println(" Solutions match: " + (same ? "YES" : "NO"));
            System.out.println("-----------------------------------------------------\n");

            if (dlxSolved) totalDlx += dlxMs;
            if (backSolved) totalBack += backMs;
            if (dlxSolved && backSolved && same) solvedByBoth++;
        }

        System.out.println("Summary:");
        System.out.println(" Puzzles run: " + PUZZLES.length);
        System.out.println(" Puzzles solved by both with matching solutions: " + solvedByBoth);
        System.out.printf(" Average DLX time (successful solves only): %s ms\n",
                (totalDlx == 0 ? "N/A" : String.format("%.2f", totalDlx / (double) PUZZLES.length)));
        System.out.printf(" Average Backtracking time (successful solves only): %s ms\n",
                (totalBack == 0 ? "N/A" : String.format("%.2f", totalBack / (double) PUZZLES.length)));
    }

    // parse puzzle from 81-char numeric string (0 = empty)
    private static int[][] parsePuzzle(String s) {
        if (s.length() != 81) throw new IllegalArgumentException("Puzzle must be 81 chars");
        int[][] g = new int[9][9];
        for (int i = 0; i < 81; i++) {
            char ch = s.charAt(i);
            int r = i / 9, c = i % 9;
            if (ch == '.' || ch == '0') g[r][c] = 0;
            else g[r][c] = ch - '0';
        }
        return g;
    }

    private static void printGrid(int[][] g) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                System.out.print((g[r][c] == 0 ? '.' : g[r][c]));
                if (c == 2 || c == 5) System.out.print(" | ");
                else if (c < 8) System.out.print(" ");
            }
            System.out.println();
            if (r == 2 || r == 5) System.out.println("------+-------+------");
        }
        System.out.println();
    }

    // deep copy utility
    private static int[][] deepCopy(int[][] src) {
        int[][] d = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(src[i], 0, d[i], 0, 9);
        return d;
    }

    // Backtracking solver implementation (simple, consistent with your GUI helper)
    private static boolean backtrackSolve(int[][] board) {
        return backtrackHelper(board, 0, 0);
    }

    private static boolean backtrackHelper(int[][] board, int row, int col) {
        if (row == 9) return true;
        int nrow, ncol;
        if (col == 8) { nrow = row + 1; ncol = 0; }
        else { nrow = row; ncol = col + 1; }

        if (board[row][col] != 0) {
            return backtrackHelper(board, nrow, ncol);
        } else {
            for (int num = 1; num <= 9; num++) {
                if (isSafe(board, row, col, num)) {
                    board[row][col] = num;
                    if (backtrackHelper(board, nrow, ncol)) return true;
                    board[row][col] = 0;
                }
            }
            return false;
        }
    }

    private static boolean isSafe(int[][] board, int row, int col, int number) {
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == number) return false;
            if (board[row][i] == number) return false;
        }
        int sr = (row / 3) * 3, sc = (col / 3) * 3;
        for (int r = sr; r < sr + 3; r++) {
            for (int c = sc; c < sc + 3; c++) {
                if (board[r][c] == number) return false;
            }
        }
        return true;
    }
}
