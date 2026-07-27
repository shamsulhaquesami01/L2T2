import java.util.*;

interface SmartDevice {
    void activate();

    void deactivate();

    double getPowerUsage();

    String getStatus();

    default List<SmartDevice> getChildren() {
        return Collections.emptyList();
    }
}

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
            d.activate();
    }

    public void deactivate() {
        for (SmartDevice d : devices)
            d.deactivate();
    }

    public double getPowerUsage() {
        double total = 0;
        for (SmartDevice d : devices)
            total += d.getPowerUsage();
        return total;
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder("[" + name + "]");
        for (SmartDevice d : devices)
            sb.append("\n  ").append(d.getStatus());
        return sb.toString();
    }

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

    public List<SmartDevice> getChildren() {
        return wrappee.getChildren();
    }
}

class AccessRestricted extends DeviceDecorator {
    private final int pin;
    private boolean locked = true;

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
            return;
        super.activate();
    }

    @Override
    public void deactivate() {
        if (locked)
            return;
        super.deactivate();
    }

    @Override
    public String getStatus() {
        return super.getStatus() + (locked ? " [LOCKED]" : "");
    }
}

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
        running = true;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        running = false;
    }

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

class EcoMode extends DeviceDecorator {
    private final double budget;

    public EcoMode(SmartDevice wrappee, double budget) {
        super(wrappee);
        this.budget = budget;
    }

    @Override
    public void activate() {
        super.activate();
        enforceBudget();
    }

    private void enforceBudget() {
        List<SmartDevice> children = wrappee.getChildren();

        for (int i = children.size() - 1; i >= 0 && wrappee.getPowerUsage() > budget; i--) {
            children.get(i).deactivate();
        }
    }

    @Override
    public String getStatus() {
        return "[ECO: " + budget + "W budget]\n" + super.getStatus();
    }
}

class GuestMode extends DeviceDecorator {
    private final Set<Class<?>> allowedTypes;

    public GuestMode(SmartDevice wrappee, Set<Class<?>> allowedTypes) {
        super(wrappee);
        this.allowedTypes = allowedTypes;
    }

    @Override
    public void activate() {
        applyActivate(wrappee);
    }

    private void applyActivate(SmartDevice node) {
        for (SmartDevice child : node.getChildren()) {
            if (!child.getChildren().isEmpty()) {
                applyActivate(child);
            } else if (isAllowed(child)) {
                child.activate();
            }
        }
    }

    @Override
    public double getPowerUsage() {
        return sumAllowed(wrappee);
    }

    private double sumAllowed(SmartDevice node) {
        double total = 0;
        for (SmartDevice child : node.getChildren()) {
            if (!child.getChildren().isEmpty()) {
                total += sumAllowed(child);
            } else if (isAllowed(child)) {
                total += child.getPowerUsage();
            }
        }
        return total;
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder("[GUEST MODE]");
        appendStatus(wrappee, sb, "  ");
        return sb.toString();
    }

    private void appendStatus(SmartDevice node, StringBuilder sb, String indent) {
        for (SmartDevice child : node.getChildren()) {
            if (!child.getChildren().isEmpty()) {
                sb.append("\n").append(indent).append(child.getStatus());
                appendStatus(child, sb, indent + "  ");
            } else {
                sb.append("\n").append(indent).append(child.getStatus());
                if (!isAllowed(child))
                    sb.append(" [guest-restricted]");
            }
        }
    }

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
