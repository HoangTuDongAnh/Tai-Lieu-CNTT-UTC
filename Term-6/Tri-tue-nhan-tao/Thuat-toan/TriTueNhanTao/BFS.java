import java.util.*;

public class BFS {

    public static Node TimKiemRong(Problem problem) {

        Deque<Node> L = new ArrayDeque<>(); // danh sách L
        Set<State> Q = new HashSet<>();     // (tuỳ chọn) tập đã sinh/đã duyệt

        Node u0 = new Node(problem.getInitialState(), null, 0);
        L.addLast(u0);          // chỉ chứa trạng thái ban đầu
        Q.add(u0.state);

        while (true) {
            if (L.isEmpty()) return null;          // thất bại

            Node u = L.removeFirst();              // loại u ở đầu L

            if (problem.isGoal(u.state)) return u; // thành công

            for (State vState : problem.getSuccessors(u.state)) { // v kề u
                if (!Q.contains(vState)) {
                    Node v = new Node(vState, u, u.depth + 1);    // father(v)=u
                    L.addLast(v);                                  // đặt v vào cuối L
                    Q.add(vState);
                }
            }
        }
    }
}