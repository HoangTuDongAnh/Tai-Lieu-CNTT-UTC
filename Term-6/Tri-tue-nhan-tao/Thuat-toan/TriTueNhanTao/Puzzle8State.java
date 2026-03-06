import java.util.Arrays;

public class Puzzle8State implements State {
    // board theo hàng: index 0..8, 0 là ô trống
    public final int[] a;

    public Puzzle8State(int[] a) {
        if (a.length != 9) throw new IllegalArgumentException("Board must have length 9");
        this.a = Arrays.copyOf(a, 9);
    }

    public int indexOfZero() {
        for (int i = 0; i < 9; i++) if (a[i] == 0) return i;
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Puzzle8State)) return false;
        Puzzle8State that = (Puzzle8State) o;
        return Arrays.equals(a, that.a);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(a);
    }

    @Override
    public String toString() {
        // in dạng 3x3
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(a[i] == 0 ? "_" : a[i]).append((i % 3 == 2) ? "\n" : " ");
        }
        return sb.toString();
    }
}