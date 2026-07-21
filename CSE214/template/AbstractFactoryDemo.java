package CSE214.template;

// --- ABSTRACT FACTORY ---
// 1. Abstract Products (The components)
interface Button { void render(); }
interface TextField { void input(); }
interface DialogBox { void show(); }

// 2. Concrete Products for Family 1 (e.g., Light Theme)
class LightButton implements Button { 
    public void render() { System.out.println("Light Button Rendered"); } 
}
class LightTextField implements TextField { 
    public void input() { System.out.println("Light TextField Input"); } 
}
class LightDialogBox implements DialogBox { 
    public void show() { System.out.println("Light Dialog Box Shown"); } 
}

// 3. Concrete Products for Family 2 (e.g., Dark Theme)
class DarkButton implements Button { 
    public void render() { System.out.println("Dark Button Rendered"); } 
}
class DarkTextField implements TextField { 
    public void input() { System.out.println("Dark TextField Input"); } 
}
class DarkDialogBox implements DialogBox { 
    public void show() { System.out.println("Dark Dialog Box Shown"); } 
}

// 4. Abstract Factory Interface
interface GUIFactory {
    Button createButton();
    TextField createTextField();
    DialogBox createDialogBox();
}

// 5. Concrete Factories
class LightThemeFactory implements GUIFactory {
    public Button createButton() { return new LightButton(); }
    public TextField createTextField() { return new LightTextField(); }
    public DialogBox createDialogBox() { return new LightDialogBox(); }
}

class DarkThemeFactory implements GUIFactory {
    public Button createButton() { return new DarkButton(); }
    public TextField createTextField() { return new DarkTextField(); }
    public DialogBox createDialogBox() { return new DarkDialogBox(); }
}

// 6. Client Code
public class AbstractFactoryDemo {
    public static void main(String[] args) {
        // Change this instantiation to switch the entire application family
        GUIFactory factory = new DarkThemeFactory(); 
        
        Button btn = factory.createButton();
        TextField tf = factory.createTextField();
        DialogBox dialog = factory.createDialogBox();
        
        btn.render();
        tf.input();
        dialog.show();
    }
}