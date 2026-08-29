/*
 * STATE PATTERN — Template based on the slide's Gumball Machine
 * ------------------------------------------------------------------
 * Definition (from slide): Allow an object to alter its behavior when its
 * internal state changes. The object will appear to change its class.
 *
 * Core shape (this is what to reuse in the exam — this is the MOST common
 * shape for "workflow/lifecycle" problems like a return/refund workflow,
 * a subscription tier machine, an order status pipeline, etc.):
 *   1. State interface: ONE method per possible action/operation the
 *      system supports (insertQuarter, ejectQuarter, turnCrank, dispense).
 *   2. Context class (GumballMachine): holds a reference to the CURRENT
 *      state object and a field per possible state; every public
 *      operation just DELEGATES to the current state
 *      (state.insertQuarter(), etc.) — the context itself contains NO
 *      if/else on what state it's in.
 *   3. Concrete State classes: one per condition in the workflow. Each
 *      implements every operation:
 *        - if the operation is VALID here, do the real work and call
 *          machine.setState(...) to transition.
 *        - if the operation is NOT valid here, just print/return an
 *          appropriate message instead of performing it — this satisfies
 *          "prevent operations that are not valid at this point" directly.
 *   4. Adding a brand-new state later = add one new class; NOTHING else
 *      needs to change (Open/Closed Principle) — this satisfies "allow
 *      new conditions to be incorporated later with minimal modification".
 */

import java.util.Random;

// ===== State interface: one method per possible action =====
interface State {
    void insertQuarter();
    void ejectQuarter();
    void turnCrank();
    void dispense();
}

// ===== Context: holds current state, delegates every call to it =====
class GumballMachine {
    private final State soldOutState;
    private final State noQuarterState;
    private final State hasQuarterState;
    private final State soldState;
    private final State winnerState;

    private State state;
    private int count;

    public GumballMachine(int numberGumballs) {
        soldOutState    = new SoldOutState(this);
        noQuarterState  = new NoQuarterState(this);
        hasQuarterState = new HasQuarterState(this);
        soldState       = new SoldState(this);
        winnerState     = new WinnerState(this);

        this.count = numberGumballs;
        this.state = (count > 0) ? noQuarterState : soldOutState;
    }

    // every public action just delegates — NO if/else on state here
    public void insertQuarter() { state.insertQuarter(); }
    public void ejectQuarter()  { state.ejectQuarter(); }
    public void turnCrank() {
        state.turnCrank();
        state.dispense();
    }

    void releaseBall() {
        System.out.println("A gumball comes rolling out the slot...");
        if (count > 0) count--;
    }

    void setState(State state) { this.state = state; }
    int getCount() { return count; }

    State getSoldOutState()    { return soldOutState; }
    State getNoQuarterState()  { return noQuarterState; }
    State getHasQuarterState() { return hasQuarterState; }
    State getSoldState()       { return soldState; }
    State getWinnerState()     { return winnerState; }
}

// ===== Concrete states =====
class NoQuarterState implements State {
    private final GumballMachine machine;
    public NoQuarterState(GumballMachine machine) { this.machine = machine; }

    public void insertQuarter() {
        System.out.println("You inserted a quarter");
        machine.setState(machine.getHasQuarterState());
    }
    public void ejectQuarter() { System.out.println("You haven't inserted a quarter"); }   // invalid op
    public void turnCrank()    { System.out.println("You turned, but there's no quarter"); } // invalid op
    public void dispense()     { System.out.println("You need to pay first"); }             // invalid op
}

class HasQuarterState implements State {
    private final GumballMachine machine;
    private final Random randomWinner = new Random();
    public HasQuarterState(GumballMachine machine) { this.machine = machine; }

    public void insertQuarter() { System.out.println("You can't insert another quarter"); } // invalid op
    public void ejectQuarter() {
        System.out.println("Quarter returned");
        machine.setState(machine.getNoQuarterState());
    }
    public void turnCrank() {
        System.out.println("You turned...");
        int winner = randomWinner.nextInt(10);
        if (winner == 0 && machine.getCount() > 1) {
            machine.setState(machine.getWinnerState());
        } else {
            machine.setState(machine.getSoldState());
        }
    }
    public void dispense() { System.out.println("No gumball dispensed"); }                  // invalid op
}

class SoldState implements State {
    private final GumballMachine machine;
    public SoldState(GumballMachine machine) { this.machine = machine; }

    public void insertQuarter() { System.out.println("Please wait, we're already giving you a gumball"); }
    public void ejectQuarter()  { System.out.println("Sorry, you already turned the crank"); }
    public void turnCrank()     { System.out.println("Turning twice doesn't get you another gumball"); }
    public void dispense() {
        machine.releaseBall();
        if (machine.getCount() > 0) {
            machine.setState(machine.getNoQuarterState());
        } else {
            System.out.println("Oops, out of gumballs!");
            machine.setState(machine.getSoldOutState());
        }
    }
}

class SoldOutState implements State {
    private final GumballMachine machine;
    public SoldOutState(GumballMachine machine) { this.machine = machine; }

    public void insertQuarter() { System.out.println("You can't insert a quarter, the machine is sold out"); }
    public void ejectQuarter()  { System.out.println("You can't eject, you haven't inserted a quarter yet"); }
    public void turnCrank()     { System.out.println("You turned, but there are no gumballs"); }
    public void dispense()      { System.out.println("No gumball dispensed"); }
    // these are all "final" conditions in spirit: nothing here ever calls setState()
}

class WinnerState implements State {
    private final GumballMachine machine;
    public WinnerState(GumballMachine machine) { this.machine = machine; }

    public void insertQuarter() { System.out.println("Please wait, we're already giving you a gumball"); }
    public void ejectQuarter()  { System.out.println("Sorry, you already turned the crank"); }
    public void turnCrank()     { System.out.println("Turning again? No fair, only one turn!"); }
    public void dispense() {
        System.out.println("YOU'RE A WINNER! You get two gumballs for your quarter");
        machine.releaseBall();
        if (machine.getCount() == 0) {
            machine.setState(machine.getSoldOutState());
        } else {
            machine.releaseBall();
            machine.setState(machine.getCount() > 0 ? machine.getNoQuarterState() : machine.getSoldOutState());
        }
    }
}

// ===== Demo =====
public class StatePatternTemplate {
    public static void main(String[] args) {
        GumballMachine machine = new GumballMachine(5);

        machine.insertQuarter();
        machine.turnCrank();

        System.out.println();
        machine.ejectQuarter();   // invalid here (already sold) -> just prints a message, no crash
        machine.insertQuarter();
        machine.turnCrank();

        System.out.println();
        machine.insertQuarter();
        machine.turnCrank();
    }
}
