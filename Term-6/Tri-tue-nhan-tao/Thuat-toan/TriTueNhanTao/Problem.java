import java.util.List;

public interface Problem {

    State getInitialState();

    boolean isGoal(State state);

    List<State> getSuccessors(State state);
}