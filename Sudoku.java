import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Sudoku {
    class Tile extends JButton {
        int r;
        int c;
        Tile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    class Step {
        int r;
        int c;
        int num; // digit tried (0 when clearing on backtrack)
        boolean place; // true = placing a digit, false = backtracking (removing it)
        Step(int r, int c, int num, boolean place) {
            this.r = r;
            this.c = c;
            this.num = num;
            this.place = place;
        }
    }

    int boardWidth = 600;
    int boardHeight = 650;

    String[] puzzle = {
        "--74916-5",
        "2---6-3-9",
        "-----7-1-",
        "-586----4",
        "--3----9-",
        "--62--187",
        "9-4-7---2",
        "67-83----",
        "81--45---"
    };

    String[] solution = {
        "387491625",
        "241568379",
        "569327418",
        "758619234",
        "123784596",
        "496253187",
        "934176852",
        "675832941",
        "812945763"
    };

    JFrame frame = new JFrame("Sudoku");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();

    JButton numSelected = null;
    int errors = 0;

    Tile[][] tiles = new Tile[9][9];
    boolean[][] isGiven = new boolean[9][9];

    List<Step> solveSteps;
    int stepIndex = 0;
    Timer solveTimer;
    JButton solveButton;

    Sudoku() {
        frame.setSize(boardWidth, boardHeight);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 30));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Sudoku: 0");

        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(9, 9));
        setupTiles();
        frame.add(boardPanel, BorderLayout.CENTER);

        buttonsPanel.setLayout(new GridLayout(1, 10));
        setupButtons();
        setupSolveButton();
        frame.add(buttonsPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    void setupTiles() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = new Tile(r, c);
                tiles[r][c] = tile;
                char tileChar = puzzle[r].charAt(c);
                if (tileChar != '-') {
                    tile.setFont(new Font("Arial", Font.BOLD, 20));
                    tile.setText(String.valueOf(tileChar));
                    tile.setBackground(Color.lightGray);
                    isGiven[r][c] = true;
                }
                else {
                    tile.setFont(new Font("Arial", Font.PLAIN, 20));
                    tile.setBackground(Color.white);
                }
                if ((r == 2 && c == 2) || (r == 2 && c == 5) || (r == 5 && c == 2) || (r == 5 && c == 5)) {
                    tile.setBorder(BorderFactory.createMatteBorder(1, 1, 5, 5, Color.black));
                }
                else if (r == 2 || r == 5) {
                    tile.setBorder(BorderFactory.createMatteBorder(1, 1, 5, 1, Color.black));
                }
                else if (c == 2 || c == 5) {
                    tile.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 5, Color.black));
                }
                else {
                    tile.setBorder(BorderFactory.createLineBorder(Color.black));
                }
                tile.setFocusable(false);
                boardPanel.add(tile);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Tile tile = (Tile) e.getSource();
                        int r = tile.r;
                        int c = tile.c;
                        if (numSelected != null) {
                            if (tile.getText() != "") {
                                return;
                            }
                            String numSelectedText = numSelected.getText();
                            String tileSolution = String.valueOf(solution[r].charAt(c));
                            if (tileSolution.equals(numSelectedText)) {
                                tile.setText(numSelectedText);
                            }
                            else {
                                errors += 1;
                                textLabel.setText("Sudoku: " + String.valueOf(errors));
                            }

                        }
                    }
                });
            }
        }
    }

    void setupButtons() {
        for (int i = 1; i < 10; i++) {
            JButton button = new JButton();
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setText(String.valueOf(i));
            button.setFocusable(false);
            button.setBackground(Color.white);
            buttonsPanel.add(button);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton) e.getSource();
                    if (numSelected != null) {
                        numSelected.setBackground(Color.white);
                    }
                    numSelected = button;
                    numSelected.setBackground(Color.lightGray);
                }
            });
        }
    }

    void setupSolveButton() {
        solveButton = new JButton();
        solveButton.setFont(new Font("Arial", Font.BOLD, 16));
        solveButton.setText("Solve");
        solveButton.setFocusable(false);
        solveButton.setBackground(Color.white);
        buttonsPanel.add(solveButton);

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnimatedSolve();
            }
        });
    }

    void startAnimatedSolve() {
        if (solveTimer != null && solveTimer.isRunning()) {
            return;
        }

        int[][] grid = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                char ch = puzzle[r].charAt(c);
                grid[r][c] = (ch == '-') ? 0 : (ch - '0');
            }
        }

        solveSteps = new ArrayList<Step>();
        boolean solvable = solve(grid, solveSteps);

        if (!solvable) {
            JOptionPane.showMessageDialog(frame, "No solution exists for this puzzle.");
            return;
        }

        if (numSelected != null) {
            numSelected.setBackground(Color.white);
            numSelected = null;
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = tiles[r][c];
                if (!isGiven[r][c]) {
                    tile.setText("");
                    tile.setBackground(Color.white);
                }
            }
        }

        stepIndex = 0;
        solveButton.setEnabled(false);

        solveTimer = new Timer(25, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (stepIndex >= solveSteps.size()) {
                    solveTimer.stop();
                    solveButton.setEnabled(true);
                    return;
                }

                Step step = solveSteps.get(stepIndex);
                Tile tile = tiles[step.r][step.c];

                if (step.place) {
                    tile.setText(String.valueOf(step.num));
                    tile.setBackground(new Color(198, 239, 206)); // light green
                }
                else {
                    tile.setText("");
                    tile.setBackground(new Color(255, 205, 205)); // light red flash
                }

                stepIndex++;
            }
        });
        solveTimer.start();
    }

    // Backtracking that records every placement and backtrack
    boolean solve(int[][] grid, List<Step> steps) {
        int row = -1;
        int col = -1;
        boolean found = false;

        for (int r = 0; r < 9 && !found; r++) {
            for (int c = 0; c < 9 && !found; c++) {
                if (grid[r][c] == 0) {
                    row = r;
                    col = c;
                    found = true;
                }
            }
        }

        // No empty cells left puzzle is solved.
        if (!found) {
            return true;
        }

        for (int num = 1; num <= 9; num++) {
            if (isValidPlacement(grid, row, col, num)) {
                grid[row][col] = num;
                steps.add(new Step(row, col, num, true));

                if (solve(grid, steps)) {
                    return true;
                }

                grid[row][col] = 0; // backtrack
                steps.add(new Step(row, col, 0, false));
            }
        }

        return false;
    }

    boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        // Check row and column.
        for (int i = 0; i < 9; i++) {
            if (grid[row][i] == num || grid[i][col] == num) {
                return false;
            }
        }

        // Check 3x3 box.
        int boxRowStart = (row / 3) * 3;
        int boxColStart = (col / 3) * 3;
        for (int r = boxRowStart; r < boxRowStart + 3; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (grid[r][c] == num) {
                    return false;
                }
            }
        }

        return true;
    }
}