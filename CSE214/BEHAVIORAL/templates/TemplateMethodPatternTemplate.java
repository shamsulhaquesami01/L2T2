/*
 * TEMPLATE METHOD PATTERN — Template based on the slide's CaffeineBeverage
 * ------------------------------------------------------------------------
 * Definition (from slide): Template Method pattern defines the skeleton of
 * an algorithm in the superclass but lets subclasses override specific
 * steps of the algorithm without changing its structure.
 *
 * Core shape (this is what to reuse in the exam):
 *   1. ONE method in the abstract class holds the fixed SEQUENCE of steps,
 *      and is marked `final` so subclasses can never change the order.
 *   2. Steps that are the SAME for everyone -> concrete methods in the base
 *      class (boilWater, pourInCup).
 *   3. Steps that MUST differ per subclass -> abstract methods (brew,
 *      addCondiments) that each subclass is forced to implement.
 *   4. Steps that MAY optionally differ -> "hook" methods: concrete methods
 *      with a default/do-nothing-ish body that a subclass can override if
 *      it cares (customerWantsCondiments()).
 *
 * Exam pattern-matching tip: any "every X follows the same overall flow,
 * but a few steps are customised per type" problem is Template Method.
 * E.g. the hospital-visit-simulator style question (Check-In -> Record
 * Vitals -> Assessment -> Treatment -> Discharge Summary, where only
 * Assessment/Treatment differ per department) maps directly onto this
 * skeleton: prepareRecipe() <-> visit(), brew()/addCondiments() <->
 * assessment()/treatment().
 */

import java.util.Scanner;

// ===== Abstract class holding the template method =====
abstract class CaffeineBeverage {

    // THE TEMPLATE METHOD — fixed skeleton, final so it can't be overridden
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        if (customerWantsCondiments()) { // hook controls an optional step
            addCondiments();
        }
    }

    // steps identical for every subclass
    private void boilWater() {
        System.out.println("Boiling water");
    }

    private void pourInCup() {
        System.out.println("Pouring into cup");
    }

    // steps that MUST be supplied by each subclass
    protected abstract void brew();

    protected abstract void addCondiments();

    // HOOK: default behaviour, subclasses may override it if they need to
     boolean customerWantsCondiments() {
        return true;
    }
}

// ===== Concrete classes: each overrides only the steps that differ =====
class Tea extends CaffeineBeverage {
    public void brew() {
        System.out.println("Steeping the tea");
    }

    public void addCondiments() {
        System.out.println("Adding lemon");
    }
}

class Coffee extends CaffeineBeverage {
    public void brew() {
        System.out.println("Dripping coffee through filter");
    }

    public void addCondiments() {
        System.out.println("Adding sugar and milk");
    }

    // this subclass overrides the hook to actually ask the user
    @Override
    boolean customerWantsCondiments() {
        Scanner in = new Scanner(System.in);
        System.out.print("Would you like milk and sugar with your coffee (y/n)? ");
        String answer = in.hasNextLine() ? in.nextLine() : "n";
        return answer.trim().equalsIgnoreCase("y");
    }
}

class HotChocolate extends CaffeineBeverage {
    public void brew() {
        System.out.println("Mixing in the cocoa powder");
    }

   public  void addCondiments() {
        System.out.println("Adding whipped cream and marshmallows");
    }

    // a second hook overridden differently — shows the skeleton scales
    @Override
    boolean customerWantsCondiments() {
        System.out.println("(Kids always want the toppings.)");
        return true;
    }
}

// ===== Demo =====
public class TemplateMethodPatternTemplate {
    public static void main(String[] args) {
        CaffeineBeverage tea = new Tea();
        System.out.println("--- Making tea ---");
        tea.prepareRecipe();

        System.out.println();

        CaffeineBeverage hotChocolate = new HotChocolate();
        System.out.println("--- Making hot chocolate ---");
        hotChocolate.prepareRecipe();

        System.out.println();

        // Uncomment to try the interactive hook (Coffee asks the console):
        // CaffeineBeverage coffee = new Coffee();
        // System.out.println("--- Making coffee ---");
        // coffee.prepareRecipe();
    }
}
