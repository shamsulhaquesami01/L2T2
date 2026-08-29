// STATE PATTERN - EXAM TEMPLATE
// Put ALL operations from the question in State.
// Each concrete state decides which operations are valid.
// Context stores shared data and delegates behavior to current state.

interface State {
    void operationA();
    void operationB();
    void operationC();
}

class Context {
    private State state;
    private String sharedData;

    public Context() {
        state = new StateA(this); // Initial state
    }

    public void setState(State state) {
        this.state = state;
        System.out.println("State changed to: " +
                state.getClass().getSimpleName());
    }

    public State getState() {
        return state;
    }

    // Shared data belongs to Context, not individual State objects.
    public void setSharedData(String sharedData) {
        this.sharedData = sharedData;
    }

    public String getSharedData() {
        return sharedData;
    }

    // Context delegates every operation.
    public void operationA() { state.operationA(); }
    public void operationB() { state.operationB(); }
    public void operationC() { state.operationC(); }
}

class StateA implements State {
    private final Context context;

    public StateA(Context context) {
        this.context = context;
    }

    public void operationA() {
        System.out.println("OperationA is valid in StateA.");
        context.setSharedData("Data changed in StateA");
    }

    public void operationB() {
        System.out.println("StateA -> StateB");
        context.setState(new StateB(context));
    }

    public void operationC() {
        System.out.println("OperationC is NOT allowed in StateA.");
    }
}

class StateB implements State {
    private final Context context;

    public StateB(Context context) {
        this.context = context;
    }

    public void operationA() {
        System.out.println("OperationA is NOT allowed in StateB.");
    }

    public void operationB() {
        System.out.println("OperationB is valid in StateB.");
    }

    public void operationC() {
        System.out.println("StateB -> FinalState");
        context.setState(new FinalState(context));
    }
}

// Final state: no operation changes the context anymore.
class FinalState implements State {
    private final Context context;

    public FinalState(Context context) {
        this.context = context;
    }

    public void operationA() {
        System.out.println("Final state: operation not allowed.");
    }

    public void operationB() {
        System.out.println("Final state: operation not allowed.");
    }

    public void operationC() {
        System.out.println("Final state: operation not allowed.");
    }
}

public class StateTemplate {
    public static void main(String[] args) {
        Context context = new Context();

        context.operationA();
        context.operationB();
        context.operationA();
        context.operationC();
        context.operationB(); // invalid because FinalState
    }
}
