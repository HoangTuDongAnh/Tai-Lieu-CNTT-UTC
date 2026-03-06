public class Node {
    State state;
    Node parent;
    int depth;

    public Node(State state, Node parent, int depth) {
        this.state = state;
        this.parent = parent;
        this.depth = depth;
    }
}