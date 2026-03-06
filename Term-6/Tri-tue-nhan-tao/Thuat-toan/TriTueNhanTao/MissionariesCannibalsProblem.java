import java.util.ArrayList;
import java.util.List;

public class MissionariesCannibalsProblem implements Problem {

    private final int M; // tổng triệu phú
    private final int C; // tổng kẻ cướp
    private final int boatCap; // sức chứa thuyền (thường = 2)

    public MissionariesCannibalsProblem(int M, int C, int boatCap) {
        this.M = M;
        this.C = C;
        this.boatCap = boatCap;
    }

    @Override
    public State getInitialState() {
        return new MCState(M, C, true); // tất cả ở trái, thuyền ở trái
    }

    @Override
    public boolean isGoal(State state) {
        MCState s = (MCState) state;
        return s.MLeft == 0 && s.CLeft == 0 && !s.boatLeft; // tất cả sang phải
    }

    @Override
    public List<State> getSuccessors(State state) {
        MCState u = (MCState) state;
        List<State> next = new ArrayList<>();

        // Sinh các cách chở: (m,c) với 1..boatCap người, không chở 0 người
        for (int m = 0; m <= boatCap; m++) {
            for (int c = 0; c <= boatCap; c++) {
                int people = m + c;
                if (people == 0 || people > boatCap) continue;

                // (tuỳ biến thể) thường không cho phép "thuyền tự chạy" nên ok
                // Tạo trạng thái v từ u
                MCState v = move(u, m, c);
                if (v != null && isValid(v)) {
                    next.add(v);
                }
            }
        }

        return next;
    }

    private MCState move(MCState u, int mMove, int cMove) {
        // Nếu thuyền ở trái -> chở sang phải: trừ bên trái
        if (u.boatLeft) {
            if (u.MLeft < mMove || u.CLeft < cMove) return null;
            return new MCState(u.MLeft - mMove, u.CLeft - cMove, false);
        } else {
            // thuyền ở phải -> chở về trái: cộng bên trái
            int MRight = M - u.MLeft;
            int CRight = C - u.CLeft;
            if (MRight < mMove || CRight < cMove) return null;
            return new MCState(u.MLeft + mMove, u.CLeft + cMove, true);
        }
    }

    private boolean isValid(MCState s) {
        // Ràng buộc biên
        if (s.MLeft < 0 || s.CLeft < 0 || s.MLeft > M || s.CLeft > C) return false;

        int MRight = M - s.MLeft;
        int CRight = C - s.CLeft;

        // Điều kiện an toàn: nếu có triệu phú ở 1 bờ, thì không được ít hơn kẻ cướp
        boolean leftSafe = (s.MLeft == 0) || (s.MLeft >= s.CLeft);
        boolean rightSafe = (MRight == 0) || (MRight >= CRight);

        return leftSafe && rightSafe;
    }
}