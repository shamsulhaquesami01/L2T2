// STRATEGY PATTERN - EXAM TEMPLATE
// Rename Strategy/Context/concrete classes to match the question.

interface Strategy {
    void execute(String input);
}

class StrategyA implements Strategy {
    public void execute(String input) {
        System.out.println("StrategyA handling: " + input);
    }
}

class StrategyB implements Strategy {
    public void execute(String input) {
        System.out.println("StrategyB handling: " + input);
    }
}

class StrategyC implements Strategy {
    public void execute(String input) {
        System.out.println("StrategyC handling: " + input);
    }
}

class Context {
    private Strategy strategy;

    public Context(Strategy strategy) {
        this.strategy = strategy;
    }

    // Runtime behavior change
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void perform(String input) {
        if (strategy == null) {
            System.out.println("No strategy selected.");
            return;
        }
        strategy.execute(input);
    }
}

public class StrategyTemplate {
    public static void main(String[] args) {
        Context context = new Context(new StrategyA());
        context.perform("first request");

        context.setStrategy(new StrategyB());
        context.perform("second request");

        context.setStrategy(new StrategyC());
        context.perform("third request");
    }
}
