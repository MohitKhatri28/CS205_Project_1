import java.util.*;

public class EightPuzzleSolver{
    static final int GRID_SIZE = 3;
    static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right

    static class Puzzle {
        int[][] state;
        Puzzle parent;
        int g; 
        int h; 
        int f; 
    
        Puzzle(int[][] state, Puzzle parent, int g, int h) {
            this.state = state;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }

    static int[][] moveBlank(int[][] state, int[] direction) {
        int[] blankPos = findBlank(state);
        int blankRow = blankPos[0];
        int blankCol = blankPos[1];
        int newRow = blankRow + direction[0];
        int newCol = blankCol + direction[1];

        if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE) {
            int[][] newState = new int[GRID_SIZE][GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                newState[i] = state[i].clone();
            }
            newState[blankRow][blankCol] = newState[newRow][newCol];
            newState[newRow][newCol] = 0;
            return newState;
        }
        return null;
    }

    // Finds the position of the blank tile (0)
    static int[] findBlank(int[][] state) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null; 
    }

    // Computes the heuristic based on the method parameter
    static int getHeuristic(int[][] state, int[][] goal, int method) {
        switch (method) {
            case 1: 
                return 0;
            case 2: 
                int misplaced = 0;
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        if (state[i][j] != goal[i][j] && state[i][j] != 0) {
                            misplaced++;
                        }
                    }
                }
                return misplaced;
            default: 
                return 0;
        }
    }

    // Converts state to string for visited set
    static String stateToString(int[][] state) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : state) {
            for (int val : row) {
                sb.append(val);
            }
        }
        return sb.toString();
    }

    // Checks if current state matches the goal
    static boolean isGoal(int[][] state, int[][] goal) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j] != goal[i][j]) return false;
            }
        }
        return true;
    }

    // Prints the solution path
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

    // Solves the puzzle using A* search
    static void solve(int[][] start, int[][] goal, int method) {
        PriorityQueue<Puzzle> pq = new PriorityQueue<>((p1, p2) -> p1.f - p2.f);
        Set<String> visited = new HashSet<>();

        Puzzle initial = new Puzzle(start, null, 0, getHeuristic(start, goal, method));
        pq.add(initial);
        visited.add(stateToString(start));

        while (!pq.isEmpty()) {
            Puzzle current = pq.remove();

            if (isGoal(current.state, goal)) {
                System.out.println("Solution found at depth: " + current.g);
                showSolution(current);
                return;
            }

            for (int[] dir : DIRECTIONS) {
                int[][] nextState = moveBlank(current.state, dir);
                if (nextState != null && !visited.contains(stateToString(nextState))) {
                    int newG = current.g + 1;
                    int newH = getHeuristic(nextState, goal, method);
                    Puzzle nextPuzzle = new Puzzle(nextState, current, newG, newH);
                    pq.add(nextPuzzle);
                    visited.add(stateToString(nextState));
                }
            }
        }
        System.out.println("No solution found.");
    }

    public static void main(String[] args) {
        int[][] start = {{1, 2, 3}, {4, 0, 6}, {7, 5, 8}};
        int[][] goal = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
        int method = 2; 
        solve(start, goal, method);
    }
}