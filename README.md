# Sudoku (Java Swing)

A simple Sudoku game with a built-in puzzle and an animated backtracking solver.

## Run

```bash
javac Sudoku.java game.java
java game
```

## How it works

- Select a number (1-9), then click a tile to place it. Wrong guesses increase the error count.
- Given cells are gray and locked.
- **Solve** button animates the backtracking solver: green = placed, red = backtrack.

## Files

- `Sudoku.java` — UI + solver logic
- `game.java` — entry point

## Notes

- Puzzle/solution are hardcoded in `Sudoku.java`.
- Manual entry checks against the solution array, not Sudoku rules directly.
