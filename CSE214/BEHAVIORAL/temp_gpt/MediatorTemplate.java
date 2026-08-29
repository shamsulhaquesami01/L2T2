// MEDIATOR PATTERN - EXAM TEMPLATE
// Components do NOT communicate with each other directly.
// They notify the mediator; the mediator decides what happens next.

interface Mediator {
    void notify(Component sender, String event);
}

abstract class Component {
    protected Mediator mediator;

    public Component(Mediator mediator) {
        this.mediator = mediator;
    }
}

class ComponentA extends Component {
    public ComponentA(Mediator mediator) {
        super(mediator);
    }

    public void action() {
        System.out.println("ComponentA did something.");
        mediator.notify(this, "A_EVENT");
    }

    public void receiveCommand() {
        System.out.println("ComponentA received a command.");
    }
}

class ComponentB extends Component {
    public ComponentB(Mediator mediator) {
        super(mediator);
    }

    public void action() {
        System.out.println("ComponentB did something.");
        mediator.notify(this, "B_EVENT");
    }

    public void receiveCommand() {
        System.out.println("ComponentB received a command.");
    }
}

class ComponentC extends Component {
    public ComponentC(Mediator mediator) {
        super(mediator);
    }

    public void receiveCommand() {
        System.out.println("ComponentC received a command.");
    }
}

class ConcreteMediator implements Mediator {
    private ComponentA a;
    private ComponentB b;
    private ComponentC c;

    public void setComponents(ComponentA a, ComponentB b, ComponentC c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public void notify(Component sender, String event) {
        if ("A_EVENT".equals(event)) {
            System.out.println("Mediator: A_EVENT -> tell B to react.");
            b.receiveCommand();
        } else if ("B_EVENT".equals(event)) {
            System.out.println("Mediator: B_EVENT -> tell C to react.");
            c.receiveCommand();
        } else {
            System.out.println("Mediator: unknown event.");
        }
    }
}

public class MediatorTemplate {
    public static void main(String[] args) {
        ConcreteMediator mediator = new ConcreteMediator();

        ComponentA a = new ComponentA(mediator);
        ComponentB b = new ComponentB(mediator);
        ComponentC c = new ComponentC(mediator);

        mediator.setComponents(a, b, c);

        a.action();
        System.out.println();
        b.action();
    }
}
