/*
 * MEDIATOR PATTERN — Template based on the slide's UI Dialog example
 * ---------------------------------------------------------------------
 * Definition (from slide): Mediator pattern restricts direct
 * communications between objects and forces them to collaborate only via
 * a mediator. Lets you reduce chaotic dependencies between objects.
 *
 * Core shape (this is what to reuse in the exam):
 *   1. Mediator interface: one method components call to report an event
 *      (notify(sender, event) on the slide).
 *   2. Component base class: every UI element/device holds a reference to
 *      the mediator (passed in through the constructor) and, instead of
 *      talking to other components directly, just calls
 *      mediator.notify(this, "someEvent").
 *   3. Concrete Mediator: knows about ALL the components and contains the
 *      actual coordination logic (an if/else chain checking sender+event).
 *      This is the ONLY class that has to change when the coordination
 *      rules change.
 *
 * Exam pattern-matching tip: "component A should not talk to component B
 * directly, everything goes through a central controller/hub/dialog" is
 * Mediator. (E.g. Smart Home Hub: sensor -> hub -> blinds -> hub -> AC.)
 */

// ===== Part 1: the slide's UI Dialog example (extended with 3 components) =====

interface Mediator {
    void notify(Component sender, String event);
}

abstract class Component {
    protected Mediator mediator;
    public Component(Mediator mediator) { this.mediator = mediator; }
    public void setMediator(Mediator mediator) { this.mediator = mediator; }
}

class Button extends Component {
    public Button(Mediator mediator) { super(mediator); }
    public void click() { mediator.notify(this, "click"); }
}

class Checkbox extends Component {
    private boolean checked = false;
    public Checkbox(Mediator mediator) { super(mediator); }
    public void toggle() {
        checked = !checked;
        mediator.notify(this, "check");
    }
    public boolean isChecked() { return checked; }
}

class Textbox extends Component {
    private String text = "";
    public Textbox(Mediator mediator) { super(mediator); }
    public void type(String text) {
        this.text = text;
        mediator.notify(this, "change");
    }
    public String getText() { return text; }
}

// Concrete Mediator — the ONLY class that knows how everything fits together
class AuthenticationDialog implements Mediator {
    private final Button okBtn;
    private final Checkbox businessCheckbox;
    private final Textbox companyNameField;

    public AuthenticationDialog() {
        okBtn = new Button(this);
        businessCheckbox = new Checkbox(this);
        companyNameField = new Textbox(this);
    }

    public void notify(Component sender, String event) {
        if (sender == businessCheckbox && event.equals("check")) {
            System.out.println("Dialog: company-name field is now "
                    + (businessCheckbox.isChecked() ? "enabled" : "disabled"));
        } else if (sender == companyNameField && event.equals("change")) {
            System.out.println("Dialog: company name changed to \"" + companyNameField.getText() + "\"");
        } else if (sender == okBtn && event.equals("click")) {
            System.out.println("Dialog: validating everything before closing...");
            if (businessCheckbox.isChecked() && companyNameField.getText().isEmpty()) {
                System.out.println("Dialog: please enter a company name!");
            } else {
                System.out.println("Dialog: all good, closing dialog.");
            }
        }
    }

    public Button getOkButton()          { return okBtn; }
    public Checkbox getBusinessCheckbox(){ return businessCheckbox; }
    public Textbox getCompanyNameField() { return companyNameField; }
}

// ===== Part 2: generic device/hub variant (Smart-Home-Hub style problems) =====

interface HubMediator {
    void notify(Object sender, String event);
}

class LightSensor {
    private final HubMediator hub;
    public LightSensor(HubMediator hub) { this.hub = hub; }
    public void detect(String level) {
        System.out.println("LightSensor: detected " + level);
        hub.notify(this, level);
    }
}

class Blinds {
    private final HubMediator hub;
    public Blinds(HubMediator hub) { this.hub = hub; }
    public void close() {
        System.out.println("Blinds: closing...");
        hub.notify(this, "closed");
    }
}

class AirConditioner {
    public void turnOn() { System.out.println("AirConditioner: turning ON (room will get stuffy)"); }
}

class CentralHub implements HubMediator {
    private Blinds blinds;
    private final AirConditioner ac;

    public CentralHub(AirConditioner ac) {
        this.ac = ac;
    }

    // set after construction to avoid a hub<->blinds circular-constructor problem
    public void setBlinds(Blinds blinds) { this.blinds = blinds; }

    public void notify(Object sender, String event) {
        if (sender instanceof LightSensor && event.equals("High Brightness")) {
            blinds.close();
        } else if (sender instanceof Blinds && event.equals("closed")) {
            ac.turnOn();
        }
    }
}

// ===== Demo =====
public class MediatorPatternTemplate {
    public static void main(String[] args) {
        System.out.println("=== UI Dialog mediator ===");
        AuthenticationDialog dialog = new AuthenticationDialog();
        dialog.getBusinessCheckbox().toggle();           // enables company field
        dialog.getCompanyNameField().type("Acme Corp");
        dialog.getOkButton().click();                    // validates & closes

        System.out.println("\n=== Smart Home Hub mediator ===");
        AirConditioner ac = new AirConditioner();
        CentralHub hub = new CentralHub(ac);
        Blinds blinds = new Blinds(hub);       // hub already exists, so this is safe
        hub.setBlinds(blinds);                 // complete the wiring
        LightSensor sensor = new LightSensor(hub);

        sensor.detect("High Brightness");       // sensor -> hub -> blinds.close()
        blinds.close();                         // blinds -> hub -> ac.turnOn()
    }
}
