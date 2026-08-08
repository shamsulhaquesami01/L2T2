package templates;

// 1. The Mediator Interface
// This declares the method used by components to notify the mediator.
 interface Mediator {
    void notify(Component sender, String event);
}

// 2. The Component Base Class
// All UI elements will extend this. They hold a reference to the Mediator.
 abstract class Component {
    protected Mediator dialog;

    public Component(Mediator dialog) {
        this.dialog = dialog;
    }
}

// 3. Concrete Components (Dumb, Reusable objects)
 class Button extends Component {
    public Button(Mediator dialog) {
        super(dialog);
    }

    public void click() {
        System.out.println("Button was clicked.");
        // The component ONLY notifies the mediator. It doesn't do anything else.
        dialog.notify(this, "click");
    }
}

 class RadioButton extends Component {
    private boolean isSelected = false;

    public RadioButton(Mediator dialog) {
        super(dialog);
    }

    public void select() {
        this.isSelected = true;
        System.out.println("RadioButton was selected.");
        dialog.notify(this, "select");
    }
}

 class TextBox extends Component {
    private boolean isEnabled = true;
    
    public TextBox(Mediator dialog) {
        super(dialog);
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        System.out.println("TextBox enabled state changed to: " + enabled);
    }
}

// 4. The Concrete Mediator
// This acts as the Control Tower. It knows about all components and handles all logic.
public class UserProfileDialog implements Mediator {
    // The mediator holds references to all the components it manages.
    private RadioButton businessRadioBtn;
    private TextBox companyNameTextBox;
    private Button applyBtn;

    public UserProfileDialog() {
        // Create components and pass 'this' (the mediator) into them.
        this.businessRadioBtn = new RadioButton(this);
        this.companyNameTextBox = new TextBox(this);
        this.applyBtn = new Button(this);
        
        // Initially, the company text box might be disabled
        this.companyNameTextBox.setEnabled(false);
    }

    // The single, centralized communication hub.
    @Override
    public void notify(Component sender, String event) {
        // The Mediator identifies WHO sent the message, and WHAT happened.
        if (sender == businessRadioBtn && event.equals("select")) {
            System.out.println("Mediator reacting: Business radio selected. Enabling Company Name Textbox.");
            // The mediator executes the interaction logic!
            companyNameTextBox.setEnabled(true);
        } 
        else if (sender == applyBtn && event.equals("click")) {
            System.out.println("Mediator reacting: Apply clicked. Validating all forms...");
            reactOnApply();
        }
    }

    private void reactOnApply() {
        System.out.println("Running validation checks...");
        // Logic to validate emails, phone numbers, etc., goes here.
    }
    
    // Simulating user interaction
    public void simulateUserActions() {
        businessRadioBtn.select();
        applyBtn.click();
    }
}
