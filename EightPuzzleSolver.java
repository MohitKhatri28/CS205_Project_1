import java.util.*;

public class EightPuzzleSolver {

    static final int GRID_SIZE = 3;  // Dimension of the puzzle (3x3)
    // Row/col offsets for moving the blank: up, down, left, right
    static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    static class Puzzle {
        int[][] state;    // 2D grid of current puzzle
        Puzzle parent;    // Reference to parent Puzzle node in path
        int g;            // Cost from start (number of moves)
        int h;            // Heuristic 
        int f;            // Total estimated cost (g + h)
        int blankRow;     // Row index of the blank tile (0)
        int blankCol;     // Column index of the blank tile (0)

        Puzzle(int[][] state, Puzzle parent, int g, int h, int blankRow, int blankCol) {
            this.state = state;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.blankRow = blankRow;
            this.blankCol = blankCol;
        }
    }

    // Generates a new Puzzle by moving the blank in the given direction
    // Returns null if the move is out of bounds
    static Puzzle moveBlank(Puzzle current, int[] direction, int[][] goal, int method) {
        int newRow = current.blankRow + direction[0];
        int newCol = current.blankCol + direction[1];

        // Check boundaries
        if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE) {
            // Deep copy current state
            int[][] newState = new int[GRID_SIZE][GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                newState[i] = current.state[i].clone();
            }
            // Swap blank with adjacent tile
            newState[current.blankRow][current.blankCol] = newState[newRow][newCol];
            newState[newRow][newCol] = 0;

            // Compute new g, h, and blank position
            int newG = current.g + 1;
            int newH = getHeuristic(newState, goal, method);
            return new Puzzle(newState, current, newG, newH, newRow, newCol);
        }
        return null;
    }

    // Check if goal state is reached
    static boolean isGoal(int[][] state, int[][] goal) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j] != goal[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Prints the puzzle grid
    static void printPuzzle(int[][] state) {
        for (int[] row : state) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
    }

    // Prints the solution path from start to goal
    static void showSolution(Puzzle puzzle) {
        List<int[][]> path = new ArrayList<>();
        Puzzle current = puzzle;
        while (current != null) {
            path.add(current.state);
            current = current.parent;
        }
        for (int i = path.size() - 1; i >= 0; i--) {
            int[][] state = path.get(i);
            for (int[] row : state) {
                System.out.println(Arrays.toString(row));
            }
            System.out.println();
        }
    }

    // Serializes a 2D state to a String for tracking visited configurations
    static String stateToString(int[][] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                sb.append(state[i][j]).append(',');
            }
        }
        return sb.toString();
    }

    // Computes heuristic h based on the method:
    // 1 => Uniform Cost (zero heuristic)
    // 2 => Misplaced Tiles count
    // 3 => Manhattan Distance sum
    static int getHeuristic(int[][] state, int[][] goal, int method) {
        switch (method) {
            case 1: 
                return 0;
            case 2: 
                int misplaced = 0;
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        if (state[i][j] != 0 && state[i][j] != goal[i][j]) {
                            misplaced++;
                        }
                    }
                }
                return misplaced;
            case 3:
                int dist = 0;
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        int tile = state[i][j];
                        if (tile != 0) {
                            // Find tile in goal and calculate distance
                            for (int gi = 0; gi < GRID_SIZE; gi++) {
                                for (int gj = 0; gj < GRID_SIZE; gj++) {
                                    if (goal[gi][gj] == tile) {
                                        dist += Math.abs(i - gi) + Math.abs(j - gj);
                                    }
                                }
                            }
                        }
                    }
                }
                return dist;
            default: 
                return 0;
        }
    }

    // Finds the blank (0) position
    static int[] findBlank(int[][] state) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{0, 0};
    }

    // Core A* (or UCS) search driver
    static void solve(int[][] start, int[][] goal, int method) {
        // Min-heap ordered by f = g + h
        PriorityQueue<Puzzle> min_heap = new PriorityQueue<>((p1, p2) -> p1.f - p2.f);
        Set<String> visited = new HashSet<>();

        // Initialize search
        int[] blankPos = findBlank(start);
        int startH = getHeuristic(start, goal, method);
        Puzzle firstPuzzle = new Puzzle(start, null, 0, startH, blankPos[0], blankPos[1]);
        min_heap.add(firstPuzzle);
        visited.add(stateToString(start));

        int nodesExpanded = 0;
        int maxQueueSize = 1;

        // Expand nodes
        while (!min_heap.isEmpty()) {
            Puzzle current = min_heap.remove();
            nodesExpanded++;

            // Print expanding node
            System.out.println("The best state to expand with g(n)=" + current.g + ", h(n)=" + current.h + ":");
            printPuzzle(current.state);
            System.out.println();

            // Check if goal state is reached
            if (isGoal(current.state, goal)) {
                System.out.println("Goal reached!");
                System.out.println("Solution depth: " + current.g);
                System.out.println("Nodes expanded: " + nodesExpanded);
                System.out.println("Max queue size: " + maxQueueSize);
                System.out.println("Solution path:");
                showSolution(current);
                return;
            }

            // Moves blank in each direction
            for (int[] dir : DIRECTIONS) {
                Puzzle nextPuzzle = moveBlank(current, dir, goal, method);
                if (nextPuzzle != null && !visited.contains(stateToString(nextPuzzle.state))) {
                    min_heap.add(nextPuzzle);
                    visited.add(stateToString(nextPuzzle.state));
                    maxQueueSize = Math.max(maxQueueSize, min_heap.size());
                }
            }
        }

        System.out.println("No solution found.");
    }
    public static void main(String[] args) {
        int[][] goalState = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 0}
        };

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the 8-Puzzle Solver!");
        System.out.println("Enter 1 for default puzzle, or 2 to enter your own:");
        int option = sc.nextInt();

        int[][] startState;
        if (option == 1) {
            // Default example
            startState = new int[][]{
                {1, 6, 7},
                {5, 0, 3},
                {4, 8, 2}
            };
            System.out.println("Using default puzzle:");
            printPuzzle(startState);
        } else {
            // Read puzzle from user row by row
            startState = new int[GRID_SIZE][GRID_SIZE];
            System.out.println("Enter a valid puzzle row by row (use 0 for blank):");
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    startState[i][j] = sc.nextInt();
                }
            }
        }

        System.out.println("Select search method: \n1: Uniform Cost \n2: Misplaced Tiles \n3: Manhattan Distance");
        int method = sc.nextInt();

        solve(startState, goalState, method);
        sc.close();
    }
}
