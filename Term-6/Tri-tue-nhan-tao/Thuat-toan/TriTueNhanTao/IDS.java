public class IDS {

    // TKSL
    public static Node TKSL(Problem problem, int maxDepth) {

        for (int d = 0; d <= maxDepth; d++) {
            Node result = DLS.TKSHC(problem, d);
            if (result != null) return result; // thành công thì exit
        }

        return null; // thất bại sau khi thử tới maxDepth
    }
}