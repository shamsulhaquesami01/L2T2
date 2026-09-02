/*
 * MEDIATOR PATTERN — GENERIC TEMPLATE (domain-neutral on purpose)
 * ---------------------------------------------------------------------
 * The slide's UI Dialog (button/checkbox/textfield) is one illustration
 * of this, not the pattern itself — an exam problem is far more likely
 * to be about sensors/devices, or users in a chat room, or services in a
 * pipeline. What repeats is the SHAPE below. Copy this file, then rename:
 *
 *   Mediator            -> keep (or rename to match the problem's noun,
 *                           e.g. Hub, Dialog, ChatRoom, Coordinator)
 *   Component            -> the base type every collaborator shares
 *   ConcreteComponentA..C -> the actual collaborators named in the problem
 *   "eventA"/"eventB"     -> the actual events/actions the problem describes
 *
 * Definition (slide): Mediator pattern restricts direct communications
 * between objects and forces them to collaborate only via a mediator.
 * Lets you reduce chaotic dependencies between objects.
 *
 * Core rules that never change:
 *   1. Mediator interface: one method components call to report an event
 *      (notify(sender, event) below).
 *   2. Component base type: every collaborator holds a reference to the
 *      mediator and, instead of talking to other components directly,
 *      just calls mediator.notify(this, "someEvent").
 *   3. Concrete Mediator: the ONLY class that knows about every
 *      component and contains the actual coordination logic. This is
 *      the ONLY class that changes when the coordination rules change.
 *
 * ----- How this maps onto real exam wording (rename, don't rewrite) -----
 *   Smart Home Hub  : components = LightSensor, Blinds, AirConditioner
 *                      events     = "High Brightness", "closed"
 *   Chat room        : components = User (many instances)
 *                      events     = "message sent"
 *   Form validation   : components = TextField, Checkbox, SubmitButton
 *                      events     = "changed", "checked", "clicked"
 * Whatever the domain, it's still: components report an event, the
 * mediator decides what happens next. Components never call each other.
 */

// ===== Mediator interface: components report events through this =====
interface Mediator {
    void notify(Component sender, String event);
}

// ===== Component base type: every collaborator holds a mediator ref =====
abstract class Component {
    protected Mediator mediator;
    public Component(Mediator mediator) { this.mediator = mediator; }
    // useful when two components must be wired up before the mediator's
    // own constructor can hand out "this" - see ConcreteMediator below
    public void setMediator(Mediator mediator) { this.mediator = mediator; }
}

// ===== Concrete components: rename + add as many as the problem needs =====
class ConcreteComponentA extends Component {
    public ConcreteComponentA(Mediator mediator) { super(mediator); }

    public void doSomething() {
        System.out.println("ComponentA: doing its own thing");
        mediator.notify(this, "eventA");     // report the event; never call B or C directly
    }
}

class ConcreteComponentB extends Component {
    private boolean flag = false;
    public ConcreteComponentB(Mediator mediator) { super(mediator); }

    public void toggle() {
        flag = !flag;
        mediator.notify(this, "eventB");
    }
    public boolean isFlagSet() { return flag; }
}

class ConcreteComponentC extends Component {
    public ConcreteComponentC(Mediator mediator) { super(mediator); }

    public void act() { System.out.println("ComponentC: reacting to being told to"); }
}

// ===== Concrete mediator: the ONLY class that knows how it all fits together =====
class ConcreteMediator implements Mediator {
    private final ConcreteComponentA a;
    private final ConcreteComponentB b;
    private final ConcreteComponentC c;

    public ConcreteMediator() {
        a = new ConcreteComponentA(this);
        b = new ConcreteComponentB(this);
        c = new ConcreteComponentC(this);
    }

    public void notify(Component sender, String event) {
        if (sender == a && event.equals("eventA")) {
            System.out.println("Mediator: A fired eventA -> telling C to react");
            c.act();
        } else if (sender == b && event.equals("eventB")) {
            System.out.println("Mediator: B toggled, flag is now " + b.isFlagSet());
        }
        // add one branch per (sender, event) combination the problem describes
    }

    public ConcreteComponentA getA() { return a; }
    public ConcreteComponentB getB() { return b; }
    public ConcreteComponentC getC() { return c; }
}

/*
 * ----- Alternative dispatch idiom: instanceof instead of named refs -----
 * Handy when the mediator is more of a "hub" than a "dialog" (components
 * report up, hub decides which OTHER kind of component to act on), and
 * you don't want to store/expose a getter for every single component.
 * Same interface, same Component base class - just swap the body of
 * notify(...):
 *
 *     public void notify(Component sender, String event) {
 *         if (sender instanceof ConcreteComponentA && event.equals("High Brightness")) {
 *             blinds.close();              // still needs a reference to at least the target
 *         } else if (sender instanceof ConcreteComponentB && event.equals("closed")) {
 *             ac.turnOn();
 *         }
 *     }
 *
 * If TWO components need each other's mediator reference before either
 * can be safely constructed (a genuine circular dependency), build one
 * with a temporary null/placeholder mediator and wire it up afterwards
 * with setMediator(...)/a setter on the mediator itself - don't fight
 * the constructors, just finish the wiring in an extra couple of lines.
 */

// ===== Demo =====
public class MediatorPatternTemplate {
    public static void main(String[] args) {
        ConcreteMediator mediator = new ConcreteMediator();

        mediator.getA().doSomething();   // A -> mediator -> C
        mediator.getB().toggle();        // B -> mediator (no other component involved)
        mediator.getB().toggle();        // toggle back, shows the mediator reacting each time
    }
}