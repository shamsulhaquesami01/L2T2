/*
 * STRATEGY PATTERN — Template based on the slide's Duck Simulator
 * ------------------------------------------------------------------
 * Definition (from slide): Define a family of algorithms, encapsulate each
 * one, and make them interchangeable. Strategy lets the algorithm vary
 * independently from the clients that use it.
 *
 * Core shape (this is what to reuse in the exam):
 *   1. One interface PER independent behavior/algorithm family
 *      (here: FlyBehavior, QuackBehavior).
 *   2. Several concrete strategies implementing each interface.
 *   3. A Context (Duck) that HOLDS a reference to each strategy
 *      (composition, NOT inheritance) and delegates to it.
 *   4. A setter (setFlyBehavior/setQuackBehavior) so the algorithm can be
 *      swapped at RUNTIME — this is the whole point of the pattern.
 *
 * Exam pattern-matching tip: whenever a problem says things like
 * "different tiers/plans/modes should compute or behave differently, and
 * the client should be able to switch between them at runtime without
 * touching the surrounding code", that's Strategy. (E.g. "map app lets you
 * change routing strategy in real time" from the slide, or a
 * PaymentStrategy / SortStrategy / DiscountStrategy problem.)
 */

import java.util.*;

// ===== One interface per independent behavior family =====
interface FlyBehavior {
    void fly();
}

interface QuackBehavior {
    void quack();
}

// ===== Concrete strategies: flying =====
class FlyWithWings implements FlyBehavior {
    public void fly() { System.out.println("I'm flying!!"); }
}

class FlyNoWay implements FlyBehavior {
    public void fly() { System.out.println("I can't fly"); }
}

class FlyRocketPowered implements FlyBehavior {
    public void fly() { System.out.println("I'm flying with a rocket!"); }
}

// ===== Concrete strategies: quacking =====
class Quack implements QuackBehavior {
    public void quack() { System.out.println("Quack"); }
}

class Squeak implements QuackBehavior {
    public void quack() { System.out.println("Squeak"); }
}

class MuteQuack implements QuackBehavior {
    public void quack() { System.out.println("<< Silence >>"); }
}

// ===== Context: composes behaviors instead of inheriting/overriding them =====
abstract class Duck {
    protected FlyBehavior flyBehavior;
    protected QuackBehavior quackBehavior;

    // delegate to whichever strategy object is currently plugged in
    public void performFly()   { flyBehavior.fly(); }
    public void performQuack() { quackBehavior.quack(); }

    // swap the algorithm at RUNTIME
    public void setFlyBehavior(FlyBehavior fb)     { this.flyBehavior = fb; }
    public void setQuackBehavior(QuackBehavior qb) { this.quackBehavior = qb; }

    public void swim() { System.out.println("All ducks float, even decoys!"); }

    public abstract void display();
}

// ===== Concrete contexts =====
class MallardDuck extends Duck {
    public MallardDuck() {
        quackBehavior = new Quack();
        flyBehavior = new FlyWithWings();
    }
    public void display() { System.out.println("I'm a real Mallard duck"); }
}

class RedheadDuck extends Duck {
    public RedheadDuck() {
        quackBehavior = new Quack();
        flyBehavior = new FlyWithWings();
    }
    public void display() { System.out.println("I'm a real Redhead duck"); }
}

class RubberDuck extends Duck {
    public RubberDuck() {
        quackBehavior = new Squeak();
        flyBehavior = new FlyNoWay();
    }
    public void display() { System.out.println("I'm a rubber duck"); }
}

class DecoyDuck extends Duck {
    public DecoyDuck() {
        quackBehavior = new MuteQuack();
        flyBehavior = new FlyNoWay();
    }
    public void display() { System.out.println("I'm a decoy duck"); }
}

class ModelDuck extends Duck {
    public ModelDuck() {
        flyBehavior = new FlyNoWay();
        quackBehavior = new Quack();
    }
    public void display() { System.out.println("I'm a model duck"); }
}

// ===== Demo =====
public class StrategyPatternTemplate {
    public static void main(String[] args) {
        Duck mallard = new MallardDuck();
        mallard.performQuack();
        mallard.performFly();

        System.out.println();

        // the key trick: swapping a strategy object at runtime
        Duck model = new ModelDuck();
        model.performFly();                             // I can't fly
        model.setFlyBehavior(new FlyRocketPowered());    // swap the algorithm
        model.performFly();                              // I'm flying with a rocket!

        System.out.println();

        List<Duck> pond = List.of(new RedheadDuck(), new RubberDuck(), new DecoyDuck());
        for (Duck d : pond) {
            d.display();
            d.performFly();
            d.performQuack();
            System.out.println();
        }
    }
}
