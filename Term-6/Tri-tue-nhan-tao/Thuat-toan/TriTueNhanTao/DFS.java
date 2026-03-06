import java.util.*;

public class DFS {

    public static Node TimKiemSau(Problem problem) {

        Deque<Node> L = new ArrayDeque<>(); // danh sách L (dùng như stack)
        Set<State> Q = new HashSet<>();     // (tuỳ chọn) tránh lặp

        Node u0 = new Node(problem.getInitialState(), null, 0);
        L.addFirst(u0);
        Q.add(u0.state);

        while (true) {
            if (L.isEmpty()) return null;           // thất bại

            Node u = L.removeFirst();               // loại u ở đầu L

            if (problem.isGoal(u.state)) return u;  // thành công

            for (State vState : problem.getSuccessors(u.state)) {
                if (!Q.contains(vState)) {
                    Node v = new Node(vState, u, u.depth + 1);    // father(v)=u
                    L.addFirst(v);                                 // đặt v vào đầu L
                    Q.add(vState);
                }
            }
        }
    }
}