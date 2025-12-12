// importing all necessary AWT files and Swing files
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sudoku UI + integration with DLX, with UI polish:
 *  - disables solver buttons while solving
 *  - shows elapsed time after solve
 *  - validates input for immediate conflicts
 *  - legend explaining colors
 */
public class Sudoku implements KeyListener, ActionListener {
    // GUI Component variables
    private JFrame frame;
    private Label headingLabel;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem loadPreviousState;
    private JMenuItem saveCurrentState;
    private Label l2;
    private Label downLabel;
    private JLabel legendLabel;              // new legend
    private JButton solveButton;
    private JButton dlxSolveButton;          // new DLX button
    private JButton refreshButton;
    private JPanel p;
    private static JTextField[][] t;

    // Counter variables
    private int i;
    private int j;
    private int k = 0;
    private int m = 0;
    private int keyX = 0;
    private int keyY = 0;

    // Board to solve sudoku
    private static int[][] checkboard = new int[9][9];

    // Constructor
    Sudoku() {
        frame = new JFrame();
        headingLabel = new Label("           Sudoku Solver");
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        loadPreviousState = new JMenuItem("Load State");
        saveCurrentState = new JMenuItem("Save State");
        l2 = new Label("* Use \"Arrow Keys\" to navigate and fill the cells*");
        downLabel = new Label();
        legendLabel = new JLabel("Black = Clue or Question  |  Red = DLX solution  |  Blue = Backtracking solution");

        solveButton = new JButton("Solve");
        dlxSolveButton = new JButton("Solve (DLX)"); // new button
        refreshButton = new JButton("Refresh");
        p = new JPanel();
        t = new JTextField[9][9];

        frame.setLayout(null);
        frame.setBounds(100, 50, 760, 700);
        frame.getContentPane().setBackground(new Color(0, 39, 58));
        frame.setTitle("Sudoku Solver");

        frame.add(headingLabel);
        frame.add(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(loadPreviousState);
        fileMenu.add(saveCurrentState);
        frame.add(l2);
        frame.add(p);
        frame.add(solveButton);
        frame.add(dlxSolveButton);
        frame.add(refreshButton);
        frame.add(downLabel);
        frame.add(legendLabel);

        headingLabel.setBounds(0, 0, 760, 60);
        menuBar.setBounds(0, 61, 760, 25);
        l2.setBounds(100, 90, 700, 50);
        p.setBounds(160, 150, 274, 274);
        solveButton.setBounds(140, 470, 90, 30);
        dlxSolveButton.setBounds(240, 470, 120, 30);
        refreshButton.setBounds(380, 470, 90, 30);
        downLabel.setBounds(20, 600, 700, 25);
        legendLabel.setBounds(20, 620, 720, 30);

        headingLabel.setBackground(new Color(0, 65, 98));
        headingLabel.setFont(new Font("Consolas", Font.ITALIC, 45));
        headingLabel.setForeground(new Color(255, 255, 255));

        loadPreviousState.setToolTipText("Loads the Previous State...");
        saveCurrentState.setToolTipText("Saves the Current State...");

        loadPreviousState.addActionListener(this);
        saveCurrentState.addActionListener(this);

        l2.setFont(new Font("Consolas", Font.ITALIC, 22));
        l2.setForeground(new Color(250, 250, 250));

        p.setBackground(Color.BLACK);
        p.setLayout(null);

        solveButton.setBackground(Color.GREEN);
        dlxSolveButton.setBackground(new Color(60, 179, 113)); // medium sea green
        dlxSolveButton.setForeground(Color.WHITE);
        refreshButton.setBackground(Color.red);
        refreshButton.setForeground(Color.white);

        solveButton.addActionListener(this);
        dlxSolveButton.addActionListener(this); // listen
        refreshButton.addActionListener(this);

        downLabel.setForeground(Color.white);
        downLabel.setFont(new Font("Consolas", Font.ITALIC, 18));
        legendLabel.setForeground(Color.WHITE);
        legendLabel.setFont(new Font("Consolas", Font.PLAIN, 14));

        // Placing TextFields over the Panel (81 TextFields)
        for (i = 0; i < 9; i++) {
            if (i % 3 == 0 && i != 0) {
                m = m + 2;
            }
            k = 0;
            for (j = 0; j < 9; j++) {
                t[i][j] = new JTextField();
                t[i][j].setFont(new Font("Consolas", Font.BOLD, 20));
                t[i][j].addKeyListener(this);
                p.add(t[i][j]);
                if (j % 3 == 0 && j != 0) {
                    k = k + 2;
                }
                t[i][j].setBounds(j * 30 + k, i * 30 + m, 30, 30);
                t[i][j].setHorizontalAlignment(JTextField.CENTER);
            }
        }
        //------------------------------------------------------------//

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    } // End of Constructor

    // Overriding All Methods of KeyListener
    @Override
    public void keyTyped(KeyEvent k) {
        downLabel.setText("Key Typed is : " + k.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent k) {
        // ...This function is left empty...
    }

    @Override
    public void keyPressed(KeyEvent k) {
        int x = k.getKeyCode();

        switch (x) {
            case KeyEvent.VK_RIGHT:
                if (keyY == 8) keyY = -1;
                t[keyX][++keyY].requestFocus();
                break;
            case KeyEvent.VK_LEFT:
                if (keyY == 0) keyY = 9;
                t[keyX][--keyY].requestFocus();
                break;
            case KeyEvent.VK_UP:
                if (keyX == 0) keyX = 9;
                t[--keyX][keyY].requestFocus();
                break;
            case KeyEvent.VK_DOWN:
                if (keyX == 8) keyX = -1;
                t[++keyX][keyY].requestFocus();
                break;
            default:
                break;
        }
    }

    // Overriding method of ActionListener
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == solveButton) {
            // Backtracking solver path
            try {
                int[][] grid = readGridFromUI();

                // Validate input before solving
                String validationError = validateGrid(grid);
                if (validationError != null) {
                    JOptionPane.showMessageDialog(frame, "Invalid puzzle: " + validationError,
                            "Input validation error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // record which cells were originally filled so we can color them after solving
                final boolean[][] isFixed = new boolean[9][9];
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        String s = t[r][c].getText().trim();
                        isFixed[r][c] = !s.isEmpty();
                    }
                }

                // disable buttons while solving
                setSolvingState(true);

                // measure time
                final long startTime = System.nanoTime();

                // run on background
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        // copy grid into checkboard
                        for (int r = 0; r < 9; r++)
                            System.arraycopy(grid[r], 0, checkboard[r], 0, 9);
                        boolean solved = helper(checkboard, 0, 0);
                        return solved;
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean solved = get();
                            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
                            if (solved) {
                                display();
                                downLabel.setText("Solved with Backtracking. Time: " + elapsedMs + " ms");

                                // color: original clues black, solver-filled cells BLUE
                                for (int r = 0; r < 9; r++) {
                                    for (int c = 0; c < 9; c++) {
                                        if (isFixed[r][c]) {
                                            t[r][c].setForeground(Color.BLACK);
                                            t[r][c].setBackground(Color.WHITE);
                                            t[r][c].setEditable(false); // lock original clues after solve
                                        } else {
                                            // solver filled this cell -> blue
                                            t[r][c].setForeground(new Color(0, 0, 255)); // pure blue
                                            t[r][c].setBackground(new Color(235, 240, 255)); // soft blue background
                                            t[r][c].setEditable(false); // lock solved cell
                                        }
                                    }
                                }
                            } else {
                                downLabel.setText("No solution found (Backtracking). Time: " + elapsedMs + " ms");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            downLabel.setText("Error during solving: " + ex.getMessage());
                        } finally {
                            setSolvingState(false);
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ne) {
                JOptionPane.showMessageDialog(frame, "Enter only integer numbers !\nRe-enter :( ");
                refreshBoard();
            }
        } else if (ae.getSource() == dlxSolveButton) {
            // DLX solver path
            try {
                int[][] grid = readGridFromUI();

                // Validate input before solving
                String validationError = validateGrid(grid);
                if (validationError != null) {
                    JOptionPane.showMessageDialog(frame, "Invalid puzzle: " + validationError,
                            "Input validation error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // record which cells were originally filled so we can color them black later
                final boolean[][] isFixed = new boolean[9][9];
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        String s = t[r][c].getText().trim();
                        isFixed[r][c] = !s.isEmpty();
                    }
                }

                downLabel.setText("Solving with DLX...");
                // disable buttons while solving
                setSolvingState(true);
                final long start = System.nanoTime();

                // run DLX in background
                SwingWorker<int[][], Void> worker = new SwingWorker<>() {
                    @Override
                    protected int[][] doInBackground() {
                        DLXSolver solver = new DLXSolver();
                        return solver.solve(grid); // null if unsolvable
                    }

                    @Override
                    protected void done() {
                        try {
                            int[][] solved = get();
                            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                            if (solved != null) {
                                // copy into checkboard and display
                                for (int r = 0; r < 9; r++) System.arraycopy(solved[r], 0, checkboard[r], 0, 9);
                                display();
                                downLabel.setText("Solved with DLX. Time: " + elapsedMs + " ms");
                                // mark only solver-filled numbers red, keep original clues black
                                for (int r = 0; r < 9; r++) {
                                    for (int c = 0; c < 9; c++) {
                                        if (isFixed[r][c]) {
                                            t[r][c].setForeground(Color.BLACK);   // original clue
                                            t[r][c].setBackground(Color.WHITE);
                                            t[r][c].setEditable(false);
                                        } else {
                                            t[r][c].setForeground(Color.RED);     // solver-filled
                                            t[r][c].setBackground(new Color(245, 245, 245)); // light gray optional
                                            t[r][c].setEditable(false);
                                        }
                                    }
                                }
                            } else {
                                downLabel.setText("No solution found (DLX). Time: " + elapsedMs + " ms");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            downLabel.setText("Error during DLX: " + ex.getMessage());
                        } finally {
                            setSolvingState(false);
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ne) {
                JOptionPane.showMessageDialog(frame, "Enter only integer numbers !\nRe-enter :( ");
                refreshBoard();
            }
        } else if (ae.getSource() == refreshButton) {
            // enable all fields for editing again and clear
            refreshBoard();
            downLabel.setText("");
        } else if (ae.getSource() == loadPreviousState) {
            char x;
            try {
                BufferedReader br = new BufferedReader(new FileReader("Sudoku.txt"));
                for (i = 0; i < 9; i++) {
                    for (j = 0; j < 9; j++) {
                        if ((x = (char) br.read()) == '0') {
                            t[i][j].setText("");
                        } else {
                            t[i][j].setText(x + "");
                        }
                        br.read(); // space
                    }
                    br.read(); // newline
                }
                br.close();
                downLabel.setText("Previous State Loaded...");
            } catch (IOException ie) {
                ie.printStackTrace();
                downLabel.setText("Error loading previous state.");
            }
        } else if (ae.getSource() == saveCurrentState) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter("Sudoku.txt"));
                for (i = 0; i < 9; i++) {
                    for (j = 0; j < 9; j++) {
                        if (t[i][j].getText().equals("")) {
                            checkboard[i][j] = 0;
                            bw.write(checkboard[i][j] + " ");
                        } else {
                            checkboard[i][j] = Integer.parseInt(t[i][j].getText());
                            bw.write(checkboard[i][j] + " ");
                        }
                    }
                    bw.write("\n");
                }
                bw.close();
                downLabel.setText("Current State Saved...");
            } catch (IOException ie) {
                ie.printStackTrace();
                downLabel.setText("Error saving current state.");
            }
        }
    }

    // Method to set UI state while solving: disables/enables controls
    private void setSolvingState(boolean solving) {
        solveButton.setEnabled(!solving);
        dlxSolveButton.setEnabled(!solving);
        refreshButton.setEnabled(!solving);
        loadPreviousState.setEnabled(!solving);
        saveCurrentState.setEnabled(!solving);
    }

    // Method to refresh the board
    private void refreshBoard() {
        for (i = 0; i < 9; i++) {
            for (j = 0; j < 9; j++) {
                t[i][j].setText("");
                t[i][j].setBackground(Color.WHITE);
                t[i][j].setForeground(Color.BLACK);
                t[i][j].setEditable(true);
            }
        }
    }

    // Method to display the Solved Sudoku
    public static void display() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                t[i][j].setText(String.valueOf(checkboard[i][j]));
            }
        }
    }

    // Read grid from UI and validate numbers (throws NumberFormatException if invalid)
    private int[][] readGridFromUI() throws NumberFormatException {
        int[][] grid = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String s = t[r][c].getText().trim();
                if (s.isEmpty()) grid[r][c] = 0;
                else {
                    int val = Integer.parseInt(s);
                    if (val < 0 || val > 9) throw new NumberFormatException("Digits must be 0-9");
                    grid[r][c] = val;
                }
            }
        }
        return grid;
    }

    /**
     * Validate the input grid for conflicts.
     * Returns null if valid, otherwise returns a human-readable error message.
     */
    private String validateGrid(int[][] grid) {
        // check rows
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = grid[r][c];
                if (v == 0) continue;
                if (seen[v]) return "Duplicate value " + v + " in row " + (r + 1);
                seen[v] = true;
            }
        }
        // check cols
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int v = grid[r][c];
                if (v == 0) continue;
                if (seen[v]) return "Duplicate value " + v + " in column " + (c + 1);
                seen[v] = true;
            }
        }
        // check boxes
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                boolean[] seen = new boolean[10];
                for (int r = br * 3; r < br * 3 + 3; r++) {
                    for (int c = bc * 3; c < bc * 3 + 3; c++) {
                        int v = grid[r][c];
                        if (v == 0) continue;
                        if (seen[v]) return "Duplicate value " + v + " in box starting at row " + (br * 3 + 1) + ", col " + (bc * 3 + 1);
                        seen[v] = true;
                    }
                }
            }
        }
        return null; // valid
    }

    // Method to check whether a number is safe to place in that particular cell or not.
    private static boolean isSafe(int[][] board, int row, int col, int number) {
        for (int i = 0; i < board.length; i++) {
            if (board[i][col] == number) {
                return false;
            }
        }
        for (int j = 0; j < board.length; j++) {
            if (board[row][j] == number) {
                return false;
            }
        }
        int sr = 3 * (row / 3);
        int sc = 3 * (col / 3);
        for (int i = sr; i < sr + 3; i++) {
            for (int j = sc; j < sc + 3; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }
        return true;
    }

    // Program to recursively solve sudoku using backtracking concept
    private static boolean helper(int[][] board, int row, int col) {
        if (row == board.length) {
            return true;
        }

        int nrow = 0;
        int ncol = 0;
        if (col == board.length - 1) {
            nrow = row + 1;
            ncol = 0;
        } else {
            nrow = row;
            ncol = col + 1;
        }

        if (board[row][col] != 0) {
            if (helper(board, nrow, ncol)) {
                return true;
            }
        } else {
            for (int i = 1; i <= 9; i++) {
                if (isSafe(board, row, col, i)) {
                    board[row][col] = i;
                    // Only color during interactive/manual step visualization; keep solver fast
                    if (helper(board, nrow, ncol))
                        return true;
                    else
                        board[row][col] = 0;
                }
            }
        }
        return false;
    } // End of helper method

} // End of class
