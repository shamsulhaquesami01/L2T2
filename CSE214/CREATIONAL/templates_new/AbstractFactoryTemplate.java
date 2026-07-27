/* ============================================================================
 * CSE 214 - CREATIONAL PATTERNS - TEMPLATE 3 of 4 :  A B S T R A C T   F A C T O R Y
 * ----------------------------------------------------------------------------
 * RUN IT:  java AbstractFactoryTemplate.java
 * ============================================================================
 *
 * USE THIS TEMPLATE WHEN THE QUESTION SAYS:
 *   "family of related products"          "themes / skins / variants / styles"
 *   "must NOT mix components"             "must match each other"
 *   "each theme contains three related components"
 *   "the client selects a theme and receives the appropriate family"
 *   "change the whole family without changing the client's main logic"
 *
 * THE TELL: a GRID.  Several product TYPES x several VARIANTS.
 *     |            | Button       | TextField       | Dialog       |
 *     | Light      | LightButton  | LightTextField  | LightDialog  |
 *     | Dark       | DarkButton   | DarkTextField   | DarkDialog   |
 *   If the question gives you a grid -> Abstract Factory. If it gives you one
 *   column only (one product, many types) -> Factory (Template 2).
 *
 * THIS TEMPLATE IS BUILT AS 3 PRODUCTS x 3 FAMILIES so that you almost always
 * only have to DELETE, never add. Deleting is 10 seconds; typing is 5 minutes.
 *   - Only 2 products asked?  delete the TextField (or Dialog) rows.
 *   - Only 2 themes asked?    delete the whole HighContrast block + its
 *                             factory + its line in ThemeFactoryProvider.
 *
 * 60-SECOND ADAPTATION MAP (UI -> furniture example):
 *   Button/TextField/Dialog  -> Chair/Sofa/CoffeeTable
 *   Light/Dark/HighContrast  -> Modern/Victorian/ArtDeco
 *   GUIFactory               -> FurnitureFactory
 *   render()                 -> sitOn() / whatever method they named
 * ========================================================================== */
package templates_new;
public class AbstractFactoryTemplate {

    public static void main(String[] args) {

        // The client picks a family ONCE, at start-up. Everything after this
        // point is written against interfaces only.
        String selectedTheme = "Light";              // could come from args/input

        GUIFactory factory = ThemeFactoryProvider.getFactory(selectedTheme);
        Application app = new Application(factory);
        app.renderUI();

        System.out.println();

        // Switching the whole family = ONE line changes. Application is untouched.
        Application darkApp = new Application(ThemeFactoryProvider.getFactory("Dark"));
        darkApp.renderUI();

        System.out.println();

        Application hcApp = new Application(ThemeFactoryProvider.getFactory("HighContrast"));
        hcApp.renderUI();
    }
}

/* ===========================================================================
 * 1. ABSTRACT PRODUCTS - one interface per product TYPE (per column).
 * ======================================================================== */
interface Button {
    void render();
    void onClick();
}

interface TextField {
    void render();
}

interface Dialog {
    void render();
}

/* ===========================================================================
 * 2. CONCRETE PRODUCTS - grouped by FAMILY (per row). One row per variant.
 * ======================================================================== */

// ---------------- LIGHT FAMILY ----------------
class LightButton implements Button {
    public void render()  { System.out.println("   [Light] Button rendered with a white background."); }
    public void onClick() { System.out.println("   [Light] Button clicked."); }
}

class LightTextField implements TextField {
    public void render()  { System.out.println("   [Light] TextField rendered with a dark caret."); }
}

class LightDialog implements Dialog {
    public void render()  { System.out.println("   [Light] Dialog box rendered in light style."); }
}

// ---------------- DARK FAMILY ----------------
class DarkButton implements Button {
    public void render()  { System.out.println("   [Dark]  Button rendered with a black background."); }
    public void onClick() { System.out.println("   [Dark]  Button clicked."); }
}

class DarkTextField implements TextField {
    public void render()  { System.out.println("   [Dark]  TextField rendered with a light caret."); }
}

class DarkDialog implements Dialog {
    public void render()  { System.out.println("   [Dark]  Dialog box rendered in dark style."); }
}

// ---------------- THIRD FAMILY (delete if only two themes are asked) --------
class HighContrastButton implements Button {
    public void render()  { System.out.println("   [HC]    Button rendered in high contrast."); }
    public void onClick() { System.out.println("   [HC]    Button clicked."); }
}

class HighContrastTextField implements TextField {
    public void render()  { System.out.println("   [HC]    TextField rendered in high contrast."); }
}

class HighContrastDialog implements Dialog {
    public void render()  { System.out.println("   [HC]    Dialog box rendered in high contrast."); }
}

/* ===========================================================================
 * 3. THE ABSTRACT FACTORY - one create method per product TYPE.
 *    Return types are the INTERFACES, never the concrete classes.
 * ======================================================================== */
interface GUIFactory {
    Button    createButton();
    TextField createTextField();
    Dialog    createDialog();
}

/* ===========================================================================
 * 4. CONCRETE FACTORIES - exactly one per family. Each one can only ever
 *    produce parts of its own family, which is what makes mixing impossible.
 * ======================================================================== */
class LightThemeFactory implements GUIFactory {
    public Button    createButton()    { return new LightButton(); }
    public TextField createTextField() { return new LightTextField(); }
    public Dialog    createDialog()    { return new LightDialog(); }
}

class DarkThemeFactory implements GUIFactory {
    public Button    createButton()    { return new DarkButton(); }
    public TextField createTextField() { return new DarkTextField(); }
    public Dialog    createDialog()    { return new DarkDialog(); }
}

class HighContrastThemeFactory implements GUIFactory {
    public Button    createButton()    { return new HighContrastButton(); }
    public TextField createTextField() { return new HighContrastTextField(); }
    public Dialog    createDialog()    { return new HighContrastDialog(); }
}

/* ===========================================================================
 * 5. THE CLIENT - receives a factory, then talks to interfaces only.
 *    Notice: no `new LightButton()` anywhere here. That is the whole point,
 *    and it is the line the grader looks for.
 * ======================================================================== */
class Application {

    private final Button    button;
    private final TextField textField;
    private final Dialog    dialog;

    public Application(GUIFactory factory) {
        this.button    = factory.createButton();
        this.textField = factory.createTextField();
        this.dialog    = factory.createDialog();
    }

    public void renderUI() {
        System.out.println("Rendering the user interface:");
        button.render();
        textField.render();
        dialog.render();
        button.onClick();
    }
}

/* ---- Optional helper: maps the user's choice to a concrete factory --------
 * Use it when the question says "the client SELECTS a theme".
 * ------------------------------------------------------------------------ */
class ThemeFactoryProvider {

    public static GUIFactory getFactory(String theme) {
        switch (theme.toLowerCase()) {
            case "light":         return new LightThemeFactory();
            case "dark":          return new DarkThemeFactory();
            case "highcontrast":  return new HighContrastThemeFactory();
            default:
                throw new IllegalArgumentException("Unknown theme: " + theme);
        }
    }
}
