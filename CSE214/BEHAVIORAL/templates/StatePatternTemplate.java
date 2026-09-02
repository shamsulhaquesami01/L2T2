/*
 * STATE PATTERN — GENERIC TEMPLATE (domain-neutral on purpose)
 * ------------------------------------------------------------------
 * The slide's Gumball Machine is a fine illustration, but it's a bad
 * thing to memorize verbatim — no exam problem is going to be about a
 * literal vending machine. What actually repeats is the SHAPE below.
 * Copy this file, then just rename things:
 *
 *   State            -> keep (or rename to match the problem's noun)
 *   Context          -> the object whose behavior changes (Order, Return,
 *                        Subscription, Connection, ...)
 *   ConcreteStateA..E -> one per named condition in the problem
 *   action1..4()      -> the operations the problem actually lists
 *
 * Definition (slide): allow an object to alter its behavior when its
 * internal state changes. The object will appear to change its class.
 *
 * Core rules that never change:
 *   1. State interface: ONE method per possible operation.
 *   2. Context: holds the CURRENT state + one field per possible state;
 *      every public operation just DELEGATES to the current state.
 *      The Context itself contains NO if/else about what state it's in.
 *   3. Concrete state: implements EVERY operation.
 *        - valid here   -> do the real work, then ctx.setState(next)
 *        - invalid here -> just print/return a message; no crash, no
 *          transition (this is what "produce an appropriate message
 *          instead of performing the operation" is asking for).
 *   4. A brand-new condition later = one new class. Nothing else changes.
 *
 * ----- How this maps onto real exam wording (rename, don't rewrite) -----
 *   Return/Refund workflow : states  = Requested/Approved/Delivered/
 *                                       ProcessingRefund/Refunded/Rejected/Cancelled
 *                             actions = approve(), reject(), cancel(),
 *                                       itemDelivered(), inspect(boolean),
 *                                       refundSuccessful(), refundFailed()
 *   Subscription tiers     : states  = Common/Plus/Lux
 *                             actions = promote(), demote(), travelCheck(km)
 *   Order pipeline         : states  = Placed/Shipped/Delivered/Cancelled
 *                             actions = ship(), deliver(), cancel()
 * Whatever the domain, it's still: interface + delegating context + one
 * class per condition. The demo below deliberately exercises every kind
 * of transition you're likely to need: a plain linear move, an early
 * exit straight to a final state, a conditional branch driven by a
 * boolean parameter (like inspect(boolean eligible)), and a retry
 * self-loop (like refundFailed() letting you try again).
 */

// ===== State interface: one method per possible operation =====
interface State {
    void action1();            // e.g. "start/approve" - the normal forward step
    void action2();            // e.g. "cancel" - an early exit
    void action3(boolean outcome);  // e.g. "inspect(eligible)" - branches on a parameter
    void action4();            // e.g. "retry" - may just re-attempt the current step
}

// ===== Context: holds current state, delegates every call to it =====
class Context {
    private final State stateA;
    private final State stateB;
    private final State stateC; // final: "success" branch
    private final State stateD; // final: "cancelled" branch
    // stateC doubles as the "processing" state that can self-loop on failure

    private State current;

    public Context() {
        stateA = new ConcreteStateA(this);
        stateB = new ConcreteStateB(this);
        stateC = new ConcreteStateC(this);
        stateD = new ConcreteStateD();
        current = stateA;   // whichever condition the problem says things start in
    }

    // every public operation just delegates — NO if/else on state here
    public void action1()               { current.action1(); }
    public void action2()               { current.action2(); }
    public void action3(boolean outcome){ current.action3(outcome); }
    public void action4()               { current.action4(); }

    void setState(State s) { this.current = s; }
    State getStateA() { return stateA; }
    State getStateB() { return stateB; }
    State getStateC() { return stateC; }
    State getStateD() { return stateD; }
}

// ===== Concrete states =====

// starting condition
class ConcreteStateA implements State {
    private final Context ctx;
    public ConcreteStateA(Context ctx) { this.ctx = ctx; }

    public void action1() {
        System.out.println("[A] action1: accepted, moving to B");
        ctx.setState(ctx.getStateB());                 // plain linear transition
    }
    public void action2() {
        System.out.println("[A] action2: cancelled before it even started");
        ctx.setState(ctx.getStateD());                 // early exit straight to a final state
    }
    public void action3(boolean outcome) { System.out.println("[A] action3: not valid yet"); }
    public void action4()                { System.out.println("[A] action4: not valid yet"); }
}

// in-progress condition, awaiting a decision
class ConcreteStateB implements State {
    private final Context ctx;
    public ConcreteStateB(Context ctx) { this.ctx = ctx; }

    public void action1() { System.out.println("[B] action1: already past this step"); }
    public void action2() {
        System.out.println("[B] action2: cancelled mid-way");
        ctx.setState(ctx.getStateD());
    }
    public void action3(boolean outcome) {
        // BRANCHING TRANSITION - exactly the shape of inspect(boolean eligible)
        if (outcome) {
            System.out.println("[B] action3(true): condition met, moving to C");
            ctx.setState(ctx.getStateC());
        } else {
            System.out.println("[B] action3(false): condition failed, moving to D");
            ctx.setState(ctx.getStateD());
        }
    }
    public void action4() { System.out.println("[B] action4: nothing to retry yet"); }
}

// "processing" condition that can retry on failure before finishing
class ConcreteStateC implements State {
    private final Context ctx;
    private boolean finished = false;
    public ConcreteStateC(Context ctx) { this.ctx = ctx; }

    public void action1() { System.out.println("[C] action1: already processed"); }
    public void action2() { System.out.println("[C] action2: too late to cancel now"); }
    public void action3(boolean outcome) { System.out.println("[C] action3: not applicable here"); }
    public void action4() {
        // RETRY SELF-LOOP - exactly the shape of refundFailed() letting you try again
        if (!finished) {
            System.out.println("[C] action4: retrying... (stays in the same state until it works)");
        } else {
            System.out.println("[C] action4: already finished, nothing to retry");
        }
    }
}

// a FINAL condition — every method just reports that nothing more can happen
class ConcreteStateD implements State {
    public void action1()                { System.out.println("[D] final state: no further action"); }
    public void action2()                { System.out.println("[D] final state: no further action"); }
    public void action3(boolean outcome) { System.out.println("[D] final state: no further action"); }
    public void action4()                { System.out.println("[D] final state: no further action"); }
}

// ===== Demo — exercises every transition style described above =====
public class StatePatternTemplate {
    public static void main(String[] args) {
        Context ctx = new Context();

        ctx.action1();          // A -> B  (linear)
        ctx.action1();          // invalid now, already past this step

        ctx.action3(false);     // B -> D  (branch: failed)
        ctx.action2();          // invalid now, D is final

        System.out.println();

        Context ctx2 = new Context();
        ctx2.action1();         // A -> B
        ctx2.action3(true);     // B -> C  (branch: succeeded)
        ctx2.action4();         // retry inside C
        ctx2.action4();         // retry again

        System.out.println();

        Context ctx3 = new Context();
        ctx3.action2();         // A -> D  (early exit / cancel from the very start)
        ctx3.action1();         // invalid now, D is final
    }
}