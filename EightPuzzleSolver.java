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
    static void printState(int[][] state) {
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
                //Uniform Cost Search: h(n) = 0
                return 0;
            case 2: 
                //Misplaced Tile: count the number of tiles not in their correct positions
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
                //Manhattam Distance: calculate distance from goal state
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

    /* 
     * Implements the “general-search” driver:
     * nodes = MAKE-QUEUE(MAKE-NODE(problem.INITIAL-STATE))
     * loop do
     *   if EMPTY(nodes) then return failure
     *   node = REMOVE-FRONT(nodes)
     *   if GOAL-TEST then return node
     *   nodes = QUEUEING-FN(nodes, EXPAND(node))
     * end
     */
    static void generalSearch(int[][] initialState, int[][] goal, int method) {
        // Min-heap ordered by f = g + h
        PriorityQueue<Puzzle> nodes = new PriorityQueue<>((p1, p2) -> p1.f - p2.f);
        // stateMap used to track best g for states
        Map<String, Puzzle> stateMap = new HashMap<>();
        Set<String> explored = new HashSet<>();

        // Initialize search
        int[] blankPos = findBlank(initialState);
        int startH = getHeuristic(initialState, goal, method);
        Puzzle first = new Puzzle(initialState, null, 0, startH, blankPos[0], blankPos[1]);
        String startKey = stateToString(initialState);
        nodes.add(first);
        stateMap.put(startKey, first);

        int nodesExpanded = 0; 
        int maxQueueSize = 1;

        // Expand nodes
        while (!nodes.isEmpty()) {
            Puzzle currentNode = nodes.poll();
            String curKey = stateToString(currentNode.state);
            stateMap.remove(curKey); // Remove from stateMap as it's being expanded
            explored.add(curKey);
            nodesExpanded++;

            // Print expanding node
            System.out.println("The best state to expand with g(n)=" + currentNode.g + ", h(n)=" + currentNode.h + ":");
            printState(currentNode.state);
            System.out.println();

            // Check if goal state is reached
            if (isGoal(currentNode.state, goal)) {
                System.out.println("Goal reached!");
                System.out.println("Solution depth: " + currentNode.g);
                System.out.println("Nodes expanded: " + nodesExpanded);
                System.out.println("Max queue size: " + maxQueueSize);
                System.out.println("Solution path:");
                showSolution(currentNode);
                return;
            }

            // Move blank in each direction
            for (int[] dir : DIRECTIONS) {
                // Get next state after moving blank
                Puzzle nextNode = moveBlank(currentNode, dir, goal, method);
                if (nextNode != null) {
                    String nextKey = stateToString(nextNode.state);
                    if (explored.contains(nextKey)) {
                        continue; // Skip if already explored
                    }
                    Puzzle existing = stateMap.get(nextKey);
                    if (existing == null) {
                        // New state: add to stateMap and queue
                        nodes.add(nextNode);
                        stateMap.put(nextKey, nextNode);
                        maxQueueSize = Math.max(maxQueueSize, nodes.size());
                    } else if (nextNode.g < existing.g) {
                        // Better path found: update stateMap
                        nodes.remove(existing);
                        nodes.add(nextNode);
                        stateMap.put(nextKey, nextNode);
                    }
                }
            }
        }
        System.out.println("No solution found.");
    }

    public static void main(String[] args) {
        // Goal State
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
                {1, 3, 6},
                {5, 0, 2},
                {4, 7, 8}
            };
            System.out.println("Using default puzzle:");
            printState(startState);
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

        // Measure execution time in seconds
        long startTime = System.nanoTime();
        generalSearch(startState, goalState, method);
        long endTime = System.nanoTime();
        double elapsedSec = (endTime - startTime) / 1000000000.0;
        System.out.println("Time taken (s): " + elapsedSec);

        sc.close();
    }
}
