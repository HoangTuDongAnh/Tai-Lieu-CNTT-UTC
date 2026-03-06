import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Puzzle8Problem implements Problem {

    private final Puzzle8State initial;
    private final Puzzle8State goal;

    public Puzzle8Problem(int[] initialBoard, int[] goalBoard) {
        this.initial = new Puzzle8State(initialBoard);
        this.goal = new Puzzle8State(goalBoard);
    }

    @Override
    public State getInitialState() {
        return initial;
    }

    @Override
    public boolean isGoal(State state) {
        return goal.equals(state);
    }

    @Override
    public List<State> getSuccessors(State state) {
        Puzzle8State u = (Puzzle8State) state;
        List<State> next = new ArrayList<>();

        int z = u.indexOfZero();
        int r = z / 3, c = z % 3;

        // 4 hướng: lên, xuống, trái, phải
        trySwapAdd(u, next, r, c, r - 1, c); // up
        trySwapAdd(u, next, r, c, r + 1, c); // down
        trySwapAdd(u, next, r, c, r, c - 1); // left
        trySwapAdd(u, next, r, c, r, c + 1); // right

        return next;
    }

    private void trySwapAdd(Puzzle8State u, List<State> next, int r1, int c1, int r2, int c2) {
        if (r2 < 0 || r2 > 2 || c2 < 0 || c2 > 2) return;

        int i1 = r1 * 3 + c1;
        int i2 = r2 * 3 + c2;

        int[] b = Arrays.copyOf(u.a, 9);
        int tmp = b[i1];
        b[i1] = b[i2];
        b[i2] = tmp;

        next.add(new Puzzle8State(b));
    }
}