import java.util.Objects;

public class MCState implements State {
    public final int MLeft;      // số triệu phú bên trái
    public final int CLeft;      // số kẻ cướp bên trái
    public final boolean boatLeft; // thuyền ở bên trái?

    public MCState(int MLeft, int CLeft, boolean boatLeft) {
        this.MLeft = MLeft;
        this.CLeft = CLeft;
        this.boatLeft = boatLeft;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MCState)) return false;
        MCState that = (MCState) o;
        return MLeft == that.MLeft && CLeft == that.CLeft && boatLeft == that.boatLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hash(MLeft, CLeft, boatLeft);
    }

    @Override
    public String toString() {
        return "Left(M=" + MLeft + ",C=" + CLeft + ",Boat=" + (boatLeft ? "L" : "R") + ")";
    }
}