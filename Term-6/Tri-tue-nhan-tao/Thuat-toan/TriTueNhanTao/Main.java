import java.util.*;

public class Main {

    public static void main(String[] args) {
        testMissionariesCannibals_BFS_DFS();
        testPuzzle8_BFS_DFS();
    }

    // =========================
    // TEST 1: Triệu phú & Kẻ cướp
    // =========================
    public static void testMissionariesCannibals_BFS_DFS() {
        System.out.println("=======================================");
        System.out.println("TEST: Triệu phú & Kẻ cướp (M=3, C=3, thuyền=2)");
        System.out.println("=======================================");

        Problem p = new MissionariesCannibalsProblem(3, 3, 2);

        // BFS
        System.out.println("\n--- BFS ---");
        Node bfs = BFS.search(p);
        if (bfs == null) System.out.println("BFS: Không tìm thấy lời giải");
        else printPath(bfs);

        // DFS
        System.out.println("\n--- DFS ---");
        Node dfs = DFS.search(p);
        if (dfs == null) System.out.println("DFS: Không tìm thấy lời giải");
        else printPath(dfs);
    }

    // =========================
    // TEST 2: 8-puzzle
    // =========================
    public static void testPuzzle8_BFS_DFS() {
        System.out.println("\n=======================================");
        System.out.println("TEST: 8-puzzle");
        System.out.println("=======================================");

        // Chọn start "dễ" để DFS không chạy quá lâu
        int[] start = {
                1, 2, 3,
                4, 0, 6,
                7, 5, 8
        };

        int[] goal = {
                1, 2, 3,
                4, 5, 6,
                7, 8, 0
        };

        Problem p = new Puzzle8Problem(start, goal);

        // BFS
        System.out.println("\n--- BFS ---");
        Node bfs = BFS.search(p);
        if (bfs == null) System.out.println("BFS: Không tìm thấy lời giải");
        else printPath(bfs);

        // DFS
        System.out.println("\n--- DFS ---");
        Node dfs = DFS.search(p);
        if (dfs == null) System.out.println("DFS: Không tìm thấy lời giải");
        else printPath(dfs);
    }

    // =========================
    // UTIL: In đường đi
    // =========================
    public static void printPath(Node goalNode) {
        List<Node> path = new ArrayList<>();
        Node cur = goalNode;

        while (cur != null) {
            path.add(cur);
            cur = cur.parent;
        }
        Collections.reverse(path);

        System.out.println("So buoc = " + (path.size() - 1));
        for (int i = 0; i < path.size(); i++) {
            System.out.println("Buoc " + i + ":");
            System.out.println(path.get(i).state);
        }
    }
}