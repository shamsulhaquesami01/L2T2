import java.util.*;

/* ============================================================================
 *  Smart Home Automation Hub  —  Refactored implementation
 *  CSE 213 : Assignment on Structural Design Patterns
 * ----------------------------------------------------------------------------
 *  PATTERNS USED
 *
 *  1. COMPOSITE  — one uniform contract (SmartDevice) for a single device, a
 *     Room, and the whole Home. Rooms and the Home are containers that hold a
 *     List<SmartDevice> and delegate every operation to their children. A leaf
 *     and a group are handled through the exact same interface, so a Room can
 *     contain plain devices, upgraded devices, and even other Rooms with no
 *     special-casing.
 *
 *  2. DECORATOR  — every "upgrade" and every "mode" (AccessRestricted,
 *     TimerControlled, PowerThrottled, EcoMode, GuestMode) is a wrapper that
 *     implements SmartDevice and holds ONE SmartDevice inside it. Because a
 *     decorator wraps the interface (not a concrete class), decorators stack
 *     freely, apply to a device OR a whole room, and stay order-sensitive.
 *
 *  Together these give the four properties the task demands:
 *    (1) Uniform handling  — everything is a SmartDevice.
 *    (2) Composability     — decorators wrap decorators wrap composites.
 *    (3) No special-casing — Room/Home loop over SmartDevice; no instanceof.
 *    (4) Order sensitivity — throttle-then-eco differs from raw-then-eco,
 *                            because wrapping order = evaluation order.
 *
 *  SOLID notes:
 *    - SRP : each device, each upgrade, each mode is one small class.
 *    - OCP : new device type or new upgrade = new class, no edits to old code.
 *    - LSP : every wrapper/composite is substitutable for a plain SmartDevice.
 *    - ISP : SmartDevice holds only the operations everyone shares.
 *    - DIP : Room/Home/decorators depend on the SmartDevice abstraction.
 * ========================================================================== */

/* ============================================================================
 *  COMPONENT  (Composite pattern)
 *  The single contract shared by leaves, composites, and every decorator.
 * ========================================================================== */
interface SmartDevice {
    void activate();

    void deactivate();
    

    double getPowerUsage(); // watts; 0 when inactive

    String getStatus();

    /*
     * Ordered view of children, used by container-level modes (EcoMode /
     * GuestMode) to reach the devices they govern WITHOUT any instanceof
     * checks. A leaf has no children, so it returns an empty list. This keeps
     * the whole system uniform: modes ask any SmartDevice for its children and
     * get a sensible answer regardless of what they are wrapping.
     */
    default List<SmartDevice> getChildren() {
        return Collections.emptyList();
    }
}

/*
 * ============================================================================
 * LEAVES (Composite pattern)
 * The three concrete device types. Each is a small, self-contained class that
 * knows only its own on/off state and its own active wattage.
 * ==========================================================================
 */

class SmartLight implements SmartDevice {
    private boolean on = false;

    public void activate() {
        on = true;
    }

    public void deactivate() {
        on = false;
    }

    public double getPowerUsage() {
        return on ? 10.0 : 0.0;
    }

    public String getStatus() {
        return "Light: " + (on ? "ON" : "OFF");
    }
}

class SmartThermostat implements SmartDevice {
    private boolean on = false;

    public void activate() {
        on = true;
    }

    public void deactivate() {
        on = false;
    }

    public double getPowerUsage() {
        return on ? 150.0 : 0.0;
    }

    public String getStatus() {
        return "Thermostat: " + (on ? "ON" : "OFF");
    }
}

class SmartSpeaker implements SmartDevice {
    private boolean on = false;

    public void activate() {
        on = true;
    }

    public void deactivate() {
        on = false;
    }

    public double getPowerUsage() {
        return on ? 5.0 : 0.0;
    }

    public String getStatus() {
        return "Speaker: " + (on ? "Playing" : "Idle");
    }
}

/*
 * ============================================================================
 * COMPOSITES (Composite pattern)
 * Room and Home are containers. They hold children through the SmartDevice
 * interface and delegate every operation, "summing up" the results. Neither
 * contains a single "if" about what kind of child it holds.
 * ==========================================================================
 */

class Room implements SmartDevice {
    private final String name;
    private final List<SmartDevice> devices = new ArrayList<>(); // interface type!

    public Room(String name) {
        this.name = name;
    }

    public void addDevice(SmartDevice d) {
        devices.add(d);
    }

    public void removeDevice(SmartDevice d) {
        devices.remove(d);
    }

    public void activate() {
        for (SmartDevice d : devices)
            d.activate(); // cascade down
    }

    public void deactivate() {
        for (SmartDevice d : devices)
            d.deactivate();
    }

    public double getPowerUsage() {
        double total = 0;
        for (SmartDevice d : devices)
            total += d.getPowerUsage(); // sum children
        return total;
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder("[" + name + "]");
        for (SmartDevice d : devices)
            sb.append("\n  ").append(d.getStatus());
        return sb.toString();
    }

    /*
     * Expose children so container-level decorators (EcoMode/GuestMode) can
     * govern them uniformly. Unmodifiable so callers can read order but not
     * corrupt the room's internal list.
     */
    public List<SmartDevice> getChildren() {
        return Collections.unmodifiableList(devices);
    }
}

class Home implements SmartDevice {
    private final String name;
    private final List<SmartDevice> rooms = new ArrayList<>();

    public Home(String name) {
        this.name = name;
    }

    /*
     * Accepts a SmartDevice, not just a Room, so an UPGRADED room (a Room
     * wrapped in decorators) can be added to the Home unchanged. This is what
     * makes "an AccessRestricted room added to a Home" work.
     */
    public void addRoom(SmartDevice room) {
        rooms.add(room);
    }

    public void activate() {
        for (SmartDevice r : rooms)
            r.activate();
    }

    public void deactivate() {
        for (SmartDevice r : rooms)
            r.deactivate();
    }

    public double getPowerUsage() {
        double total = 0;
        for (SmartDevice r : rooms)
            total += r.getPowerUsage();
        return total;
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder("=== " + name + " ===");
        for (SmartDevice r : rooms)
            sb.append("\n").append(r.getStatus());
        return sb.toString();
    }

    public List<SmartDevice> getChildren() {
        return Collections.unmodifiableList(rooms);
    }
}

/*
 * ============================================================================
 * BASE DECORATOR (Decorator pattern)
 * Holds ONE wrapped SmartDevice and delegates everything to it by default.
 * Concrete decorators override only the parts they change. The wrapped field
 * is typed as the interface, so a decorator can wrap a leaf, a composite, or
 * another decorator interchangeably — this is what enables free stacking.
 * ==========================================================================
 */
abstract class DeviceDecorator implements SmartDevice {
    protected final SmartDevice wrappee;

    protected DeviceDecorator(SmartDevice wrappee) {
        this.wrappee = wrappee;
    }

    public void activate() {
        wrappee.activate();
    }

    public void deactivate() {
        wrappee.deactivate();
    }

    public double getPowerUsage() {
        return wrappee.getPowerUsage();
    }

    public String getStatus() {
        return wrappee.getStatus();
    }

    /*
     * Pass the children of whatever is inside straight through, so container
     * modes can be stacked on top of other decorators and still find the
     * governed devices.
     */
    public List<SmartDevice> getChildren() {
        return wrappee.getChildren();
    }
}

/*
 * ============================================================================
 * DEVICE-LEVEL DECORATORS (the three "upgrades")
 * ==========================================================================
 */

/*
 * --- AccessRestricted --------------------------------------------------------
 * PIN-protects the wrapped entity. While locked, activate/deactivate are
 * silently ignored. Power is NOT touched: a device already running before it
 * was locked keeps drawing power (locking blocks control, not electricity).
 */
class AccessRestricted extends DeviceDecorator {
    private final int pin;
    private boolean locked = true; // starts locked; must be unlocked first

    public AccessRestricted(SmartDevice wrappee, int pin) {
        super(wrappee);
        this.pin = pin;
    }

    public void unlock(int attempt) {
        if (attempt == pin)
            locked = false;
    }

    public void lock() {
        locked = true;
    }

    @Override
    public void activate() {
        if (locked)
            return; // block control while locked
        super.activate();
    }

    @Override
    public void deactivate() {
        if (locked)
            return;
        super.deactivate();
    }

    // getPowerUsage() is inherited unchanged -> already-running device still
    // reports its real power even while locked.

    @Override
    public String getStatus() {
        return super.getStatus() + (locked ? " [LOCKED]" : "");
    }
}

/*
 * --- TimerControlled ---------------------------------------------------------
 * Adds an auto-shutoff timer. Activating starts the countdown; simulating
 * expiry deactivates the wrapped entity. A manual deactivate cancels the timer
 * so a later expiry does nothing. Works on a device OR a whole room.
 */
class TimerControlled extends DeviceDecorator {
    private final int seconds;
    private boolean running = false;

    public TimerControlled(SmartDevice wrappee, int seconds) {
        super(wrappee);
        this.seconds = seconds;
    }

    @Override
    public void activate() {
        super.activate();
        running = true; // start the countdown
    }

    @Override
    public void deactivate() {
        super.deactivate();
        running = false; // manual off cancels the timer
    }

    /* Test hook standing in for a real timer firing. */
    public void simulateTimerExpiry() {
        if (running) {
            super.deactivate();
            running = false;
        }
    }

    @Override
    public String getStatus() {
        return super.getStatus() + (running ? " (auto-off in " + seconds + "s)" : "");
    }
}

/*
 * --- PowerThrottled ----------------------------------------------------------
 * Caps the reported power of the wrapped entity at a maximum. If the entity
 * draws less than the cap, nothing changes; if more, it is reduced to the cap.
 * This is a real reduction of the reported figure, per the spec.
 */
class PowerThrottled extends DeviceDecorator {
    private final double cap;

    public PowerThrottled(SmartDevice wrappee, double cap) {
        super(wrappee);
        this.cap = cap;
    }

    @Override
    public double getPowerUsage() {
        double actual = super.getPowerUsage();
        return Math.min(actual, cap);
    }

    @Override
    public String getStatus() {
        double actual = wrappee.getPowerUsage();
        String s = super.getStatus();
        if (actual > cap)
            s += " [throttled to " + cap + "W]";
        return s;
    }
}

/*
 * ============================================================================
 * CONTAINER-LEVEL DECORATORS (the two Premium "modes")
 * These wrap a whole entity (normally a Room or Home) and impose an aggregate
 * constraint. They reach the governed devices through getChildren(), so they
 * never need to know the concrete class of what they wrap.
 * ==========================================================================
 */

/*
 * --- EcoMode -----------------------------------------------------------------
 * Enforces a TOTAL power budget on the wrapped entity. On activation, if the
 * aggregate exceeds the budget, the most-recently-added devices are shed first
 * (reverse install order) until the total fits. This is a constraint on the
 * SUM, not a per-device throttle — devices are switched off whole, never
 * reduced.
 */
class EcoMode extends DeviceDecorator {
    private final double budget;

    public EcoMode(SmartDevice wrappee, double budget) {
        super(wrappee);
        this.budget = budget;
    }

    @Override
    public void activate() {
        super.activate(); // turn everything on first
        enforceBudget(); // then shed until we fit
    }

    private void enforceBudget() {
        List<SmartDevice> children = wrappee.getChildren();
        // Walk from the last-installed child backwards, shedding whole devices
        // until the running total is within budget.
        for (int i = children.size() - 1; i >= 0 && wrappee.getPowerUsage() > budget; i--) {
            children.get(i).deactivate();
        }
    }

    @Override
    public String getStatus() {
        return "[ECO: " + budget + "W budget]\n" + super.getStatus();
    }
}

/*
 * --- GuestMode ---------------------------------------------------------------
 * Restricts which device TYPES may operate. On activation only allowed types
 * are turned on; disallowed types are deactivated and skipped. Power reporting
 * and status only count/annotate accordingly. Type membership is decided by
 * the concrete class of each child.
 */
class GuestMode extends DeviceDecorator {
    private final Set<Class<?>> allowedTypes;

    public GuestMode(SmartDevice wrappee, Set<Class<?>> allowedTypes) {
        super(wrappee);
        this.allowedTypes = allowedTypes;
    }

    @Override
    public void activate() {
        for (SmartDevice child : wrappee.getChildren()) {
            if (isAllowed(child))
                child.activate();
            else
                child.deactivate(); // ensure disallowed stay off
        }
    }

    @Override
    public double getPowerUsage() {
        double total = 0;
        for (SmartDevice child : wrappee.getChildren()) {
            if (isAllowed(child))
                total += child.getPowerUsage();
        }
        return total;
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder("[GUEST MODE]");
        for (SmartDevice child : wrappee.getChildren()) {
            sb.append("\n  ").append(child.getStatus());
            if (!isAllowed(child))
                sb.append(" [guest-restricted]");
        }
        return sb.toString();
    }

    /*
     * A child is allowed if its concrete class is in the allowed set. We unwrap
     * decorators so that, e.g., a TimerControlled(SmartLight) is judged by the
     * SmartLight inside it, matching how the client specifies allowed types.
     */
    private boolean isAllowed(SmartDevice child) {
        return allowedTypes.contains(underlyingType(child));
    }

    private Class<?> underlyingType(SmartDevice d) {
        SmartDevice current = d;
        while (current instanceof DeviceDecorator) {
            current = ((DeviceDecorator) current).wrappee;
        }
        return current.getClass();
    }
}
