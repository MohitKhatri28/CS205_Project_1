import java.util.*;

public class EightPuzzleSolver{

    static final int GRID_SIZE = 3;
    static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    static class Puzzle {
        int[][] state;
        Puzzle parent;
        int depth;

        Puzzle(int[][] state, Puzzle parent, int depth) {
            this.state = state;
            this.parent = parent;
            this.depth = depth;
        }
    }

    static Puzzle moveBlank(Puzzle current, int[] direction) {
        int blankRow = -1, blankCol = -1;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (current.state[i][j] == 0) {
                    blankRow = i;
                    blankCol = j;
                    break;
                }
            }
        }

        int newRow = blankRow + direction[0];
        int newCol = blankCol + direction[1];

        if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE) {
            int[][] newState = new int[GRID_SIZE][GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                newState[i] = current.state[i].clone();
            }
            newState[blankRow][blankCol] = newState[newRow][newCol];
            newState[newRow][newCol] = 0;
            return new Puzzle(newState, current, current.depth + 1);
        }
        return null;
    }

    static boolean isGoal(int[][] state, int[][] goal) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (state[i][j] != goal[i][j]) return false;
            }
        }
        return true;
    }

    static void showPuzzle(int[][] state) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(state[i][j] + " ");
            }
            System.out.println();
        }
    }

    static void showSolution(Puzzle finalPuzzle) {
        List<int[][]> steps = new ArrayList<>();
        Puzzle current = finalPuzzle;
        while (current != null) {
            steps.add(current.state);
            current = current.parent;
        }
        Collections.reverse(steps);
        for (int[][] step : steps) {
            showPuzzle(step);
            System.out.println();
        }
    }

    static String stateToString(int[][] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                sb.append(state[i][j]);
            }
        }
        return sb.toString();
    }

    static void solve(int[][] start, int[][] goal) {
        Queue<Puzzle> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        Puzzle initial = new Puzzle(start, null, 0);
        queue.add(initial);
        visited.add(stateToString(start));

        while (!queue.isEmpty()) {
            Puzzle current = queue.poll();

            if (isGoal(current.state, goal)) {
                System.out.println("Solution found at depth: " + current.depth);
                showSolution(current);
                return;
            }

            for (int[] dir : DIRECTIONS) {
                Puzzle next = moveBlank(current, dir);
                if (next != null && !visited.contains(stateToString(next.state))) {
                    queue.add(next);
                    visited.add(stateToString(next.state));
                }
            }
        }
        System.out.println("No solution found.");
    }

    public static void main(String[] args) {
        int[][] startState = {{1, 2, 3}, {4, 0, 6}, {7, 5, 8}};
        int[][] goalState = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
        solve(startState, goalState);
    }
}