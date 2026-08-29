package other_section;
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
 *      system supports (updatereason, approve, reject, cancel).
 *   2. Context class (Product): holds a reference to the CURRENT
 *      state object and a field per possible state; every public
 *      operation just DELEGATES to the current state
 *      (state.updatereason(String reason), etc.) — the context itself contains NO
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

// ===== State interface: one method per possible action =====
interface State {
    void updatereason(String reason);

    void approve();

    void reject();

    void cancel();

    void itemDelivered();

    void inspect(boolean eligible);

    void refundSuccessful();

    void refundFailed();
}

// ===== Context: holds current state, delegates every call to it =====
class Product {
    private final State Procesing;
    private final State Requested;
    private final State Approved;
    private final State Delivered;
    private final State Rejected;
    private final State Refunded;
    private final State Cancelled;

    private State state;
    private String reason;
    private boolean delivered = false;

    public Product() {
        Procesing = new Procesing(this);
        Requested = new Requested(this);
        Approved = new Approved(this);
        Delivered = new Delivered(this);
        Rejected = new Rejected(this);
        Refunded = new Refunded(this);
        Cancelled = new Cancelled(this);

        this.state = Requested;
    }

    // every public action just delegates — NO if/else on state here
    public void updatereason(String reason) {
        state.updatereason(reason);
    }

    public void approve() {
        state.approve();
    }

    public void reject() {
        state.reject();
    }

    public void cancel() {
        state.cancel();
    };

    public void itemDelivered() {
        state.itemDelivered();
    };

    public void inspect(boolean eligible) {
        state.inspect(eligible);
    };

public void refundSuccessful(){state.refundSuccessful();};

    public void refundFailed() {
        state.refundFailed();
    };

    void setState(State state) {
        this.state = state;
    }

    public void setReason(String r) {
        this.reason = r;
    }

    public boolean getdeliverstatus() {
        return this.delivered;
    }

    State getProcesing() {
        return Procesing;
    }

    State getRequested() {
        return Requested;
    }

    State getApproved() {
        return Approved;
    }

    State getDelivered() {
        return Delivered;
    }

    State getRejected() {
        return Rejected;
    }

    State getCancelled() {
        return Cancelled;
    }

    State getRefunded() {
        return Refunded;
    }
}

// ===== Concrete states =====
class Requested implements State {
    private final Product machine;

    public Requested(Product machine) {
        this.machine = machine;
    }

    public void updatereason(String reason) {
        machine.setReason(reason);
    }

    public void approve() {
        machine.setState(machine.getApproved());
    } // invalid op

    public void reject() {
        machine.setState(machine.getRejected());
    } // invalid op

    public void cancel() {
        machine.setState(machine.getCancelled());
    }
    // invalid op

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

}

class Approved implements State {
    private final Product machine;

    public Approved(Product machine) {
        this.machine = machine;
    }

    public void updatereason(String reason) {
        System.out.println("You can't update reason anymore");
    } // invalid op

    public void approve() {
        System.out.println("already approved");
    }

    public void reject() {
        System.out.println("error");
    }

    public void cancel() {
        if (!machine.getdeliverstatus())
            machine.setState(machine.getCancelled());
    }

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        machine.setState(machine.getDelivered());

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

}

class Delivered implements State {
    private final Product machine;

    public Delivered(Product machine) {
        this.machine = machine;
    }

    public void updatereason(String reason) {
        System.out.println("error");
    }

    public void approve() {
        System.out.println("error");
    }

    public void reject() {
        System.out.println("error");
    }

    public void cancel() {
        System.out.println("error");
    }

    @Override
    public void inspect(boolean eligible) {
        if (eligible)
            machine.setState(machine.getProcesing());
        else
            machine.setState(machine.getRejected());

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

}

class Procesing implements State {
    private final Product machine;

    public Procesing(Product machine) {
        this.machine = machine;
    }

    @Override
    public void approve() {
        System.out.println("error");

    }

    @Override
    public void cancel() {
        System.out.println("error");

    }

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        machine.setState(machine.getProcesing());

    }

    @Override
    public void refundSuccessful() {
        machine.setState(machine.getRefunded());

    }

    @Override
    public void reject() {
        System.out.println("error");

    }

    @Override
    public void updatereason(String reason) {
        System.out.println("error");

    }

    // these are all "final" conditions in spirit: nothing here ever calls
    // setState()
}

class Rejected implements State {
    private final Product machine;

    public Rejected(Product machine) {
        this.machine = machine;
    }

    @Override
    public void approve() {
        System.out.println("error");

    }

    @Override
    public void cancel() {
        System.out.println("error");

    }

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

    @Override
    public void reject() {
        System.out.println("error");

    }

    @Override
    public void updatereason(String reason) {
        System.out.println("error");

    }

}

class Cancelled implements State {
    private final Product machine;

    public Cancelled(Product machine) {
        this.machine = machine;
    }

    @Override
    public void approve() {
        System.out.println("error");

    }

    @Override
    public void cancel() {
        System.out.println("error");

    }

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

    @Override
    public void reject() {
        System.out.println("error");

    }

    @Override
    public void updatereason(String reason) {
        System.out.println("error");

    }
}

class Refunded implements State {
    private final Product machine;

    public Refunded(Product machine) {
        this.machine = machine;
    }

    @Override
    public void approve() {
        System.out.println("error");

    }

    @Override
    public void cancel() {
        System.out.println("error");

    }

    @Override
    public void inspect(boolean eligible) {
        System.out.println("error");

    }

    @Override
    public void itemDelivered() {
        System.out.println("error");

    }

    @Override
    public void refundFailed() {
        System.out.println("error");

    }

    @Override
    public void refundSuccessful() {
        System.out.println("error");

    }

    @Override
    public void reject() {
        System.out.println("error");

    }

    @Override
    public void updatereason(String reason) {
        System.out.println("error");

    }
}

// ===== Demo =====
public class B1_online {
public static void main(String[] args) {

    // ==================================================
    // TEST 1: Requested -> update reason -> Approved
    // ==================================================
    System.out.println("========== TEST 1 ==========");

    Product p1 = new Product();

    p1.updatereason("Product damaged");
    p1.approve();

    // Reason update should now be invalid
    p1.updatereason("Changed reason");

    // Item can now be delivered
    p1.itemDelivered();

    // ==================================================
    // TEST 2: Delivered -> eligible -> Processing
    //          -> refund success -> Refunded
    // ==================================================
    System.out.println("\n========== TEST 2 ==========");

    Product p2 = new Product();

    p2.updatereason("Wrong product received");
    p2.approve();
    p2.itemDelivered();

    // Eligible for refund
    p2.inspect(true);

    // Refund succeeds
    p2.refundSuccessful();

    // Final state: this should be invalid
    p2.cancel();


    // ==================================================
    // TEST 3: Delivered -> not eligible -> Rejected
    // ==================================================
    System.out.println("\n========== TEST 3 ==========");

    Product p3 = new Product();

    p3.updatereason("Not satisfied");
    p3.approve();
    p3.itemDelivered();

    // Not eligible
    p3.inspect(false);

    // Final state: should be invalid
    p3.approve();


    // ==================================================
    // TEST 4: Refund fails -> retry -> success
    // ==================================================
    System.out.println("\n========== TEST 4 ==========");

    Product p4 = new Product();

    p4.updatereason("Defective product");
    p4.approve();
    p4.itemDelivered();
    p4.inspect(true);

    // Refund fails, should stay in Processing
    p4.refundFailed();

    // Try again later
    p4.refundSuccessful();


    // ==================================================
    // TEST 5: Requested -> Cancelled
    // ==================================================
    System.out.println("\n========== TEST 5 ==========");

    Product p5 = new Product();

    p5.updatereason("Changed my mind");
    p5.cancel();

    // Final state: should be invalid
    p5.approve();


    // ==================================================
    // TEST 6: Requested -> Rejected
    // ==================================================
    System.out.println("\n========== TEST 6 ==========");

    Product p6 = new Product();

    p6.reject();

    // Final state: should be invalid
    p6.cancel();


    // ==================================================
    // TEST 7: Approved -> Cancelled
    // ==================================================
    System.out.println("\n========== TEST 7 ==========");

    Product p7 = new Product();

    p7.approve();

    // Valid because item is not delivered yet
    p7.cancel();

    // Final state: should be invalid
    p7.itemDelivered();
}

}
