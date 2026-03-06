import java.util.*;

public class DLS {

    // TKSHC(d)
    public static Node TKSHC(Problem problem, int d) {

        Deque<Node> L = new ArrayDeque<>();
        Set<State> Q = new HashSet<>(); // tuỳ chọn: tránh lặp

        Node u0 = new Node(problem.getInitialState(), null, 0);
        L.addFirst(u0);
        Q.add(u0.state);

        while (true) {

            if (L.isEmpty()) return null; // thất bại

            Node u = L.removeFirst(); // loại u ở đầu L

            if (problem.isGoal(u.state)) return u; // thành công

            for (State vState : problem.getSuccessors(u.state)) {

                // depth(v) = depth(u) + 1
                int depthV = u.depth + 1;

                if (depthV <= d) {
                    // (tuỳ chọn) tránh lặp
                    if (!Q.contains(vState)) {
                        Node v = new Node(vState, u, depthV); // father(v)=u
                        L.addFirst(v);                         // đặt v vào đầu L
                        Q.add(vState);
                    }
                }
            }
        }
    }
}