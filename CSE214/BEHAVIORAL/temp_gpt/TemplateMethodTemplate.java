// TEMPLATE METHOD PATTERN - EXAM TEMPLATE
// Rename Process and the steps to match the question.
// Keep the overall algorithm order inside the final template method.

abstract class Process {

    // THE TEMPLATE METHOD: subclasses cannot change the skeleton/order.
    public final void runProcess(String name) {
        commonStep1(name);
        variableStep1(name);
        commonStep2(name);

        // Hook controls optional behavior.
        if (shouldRunOptionalStep()) {
            optionalStep(name);
        }

        variableStep2(name);
        commonStep3(name);
    }

    private void commonStep1(String name) {
        System.out.println("Common step 1 for " + name);
    }

    protected abstract void variableStep1(String name);

    private void commonStep2(String name) {
        System.out.println("Common step 2 for " + name);
    }

    protected void optionalStep(String name) {
        System.out.println("Optional step for " + name);
    }

    // Hook: subclasses override only if needed.
    protected boolean shouldRunOptionalStep() {
        return false;
    }

    protected abstract void variableStep2(String name);

    private void commonStep3(String name) {
        System.out.println("Common final step for " + name);
    }
}

class ProcessA extends Process {
    protected void variableStep1(String name) {
        System.out.println("ProcessA-specific step 1 for " + name);
    }

    protected void variableStep2(String name) {
        System.out.println("ProcessA-specific step 2 for " + name);
    }
}

class ProcessB extends Process {
    protected void variableStep1(String name) {
        System.out.println("ProcessB-specific step 1 for " + name);
    }

    protected boolean shouldRunOptionalStep() {
        return true;
    }

    protected void variableStep2(String name) {
        System.out.println("ProcessB-specific step 2 for " + name);
    }
}

public class TemplateMethodTemplate {
    public static void main(String[] args) {
        Process a = new ProcessA();
        Process b = new ProcessB();

        a.runProcess("Alice");
        System.out.println();
        b.runProcess("Bob");
    }
}
