package temp_others;

import java.util.*;
import java.util.function.Predicate;

enum LoadType {
    RESIDENTIAL, COMMERCIAL, INDUSTRIAL
}

class GridEvent {
    private final String gridId;
    private final long timestamp;
    private final double voltageLevel;
    private final LoadType loadType;

    public GridEvent(String gridId, long timestamp, double voltageLevel, LoadType loadType) {
        this.gridId = gridId;
        this.timestamp = timestamp;
        this.voltageLevel = voltageLevel;
        this.loadType = loadType;
    }

    public String getGridId() { return gridId; }
    public long getTimestamp() { return timestamp; }
    public double getVoltageLevel() { return voltageLevel; }
    public LoadType getLoadType() { return loadType; }

    @Override
    public String toString() {
        return String.format("[Grid: %s | Time: %d | Voltage: %.1fkV | Load: %s]",
                gridId, timestamp, voltageLevel, loadType);
    }
}

// --- Observer Interface ---
interface GridSubscriber {
    void onGridEvent(GridEvent event);
    void onSurgeAlert(GridEvent event, String alertMessage);
    String getName();
}

// --- Concrete Observers ---

class ControlRoomDisplay implements GridSubscriber {
    @Override
    public void onGridEvent(GridEvent event) {
        System.out.println("[Display Console] Logged normal update: " + event);
    }

    @Override
    public void onSurgeAlert(GridEvent event, String alertMessage) {
        System.out.println("[Display Console] !!! RED ALERT DISPLAYED: " + alertMessage + " on " + event);
    }

    @Override
    public String getName() { return "Control Room Display"; }
}

class FailurePreventionUnit implements GridSubscriber {
    @Override
    public void onGridEvent(GridEvent event) {
        System.out.println("[Safety Unit] Monitoring high voltage: " + event.getVoltageLevel() + "kV");
    }

    @Override
    public void onSurgeAlert(GridEvent event, String alertMessage) {
        System.out.println("[Safety Unit] !!! ISOLATING CIRCUITS FOR: " + event.getGridId() + " Reason: " + alertMessage);
    }

    @Override
    public String getName() { return "Failure Prevention Unit"; }
}

class BillingUnit implements GridSubscriber {
    @Override
    public void onGridEvent(GridEvent event) {
        System.out.println("[Billing Unit] Calculating tariff adjustment for Industrial Load on " + event.getGridId());
    }

    @Override
    public void onSurgeAlert(GridEvent event, String alertMessage) {
        // Billing unit is unaffected by raw surge alerts
    }

    @Override
    public String getName() { return "Billing Unit"; }
}

// --- Subject: PowerGridStation ---

class PowerGridStation {
    // Maps subscribers to their individual filter criteria
    private final Map<GridSubscriber, Predicate<GridEvent>> subscriptions = new HashMap<>();

    public void subscribe(GridSubscriber subscriber, Predicate<GridEvent> filter) {
        subscriptions.put(subscriber, filter);
        System.out.printf("Subscribed [%s] to Grid Station.%n", subscriber.getName());
    }

    public void unsubscribe(GridSubscriber subscriber) {
        subscriptions.remove(subscriber);
        System.out.printf("Unsubscribed [%s] from Grid Station.%n", subscriber.getName());
    }

    public void recordEvent(GridEvent event) {
        System.out.println("\n--- Event Detected: " + event + " ---");

        // Rule 1: Voltage > 400 kV triggers Urgent Overvoltage Alert directly to Safety & Display
        if (event.getVoltageLevel() > 400.0) {
            String alert = "DANGEROUS OVERVOLTAGE (" + event.getVoltageLevel() + "kV)";
            for (GridSubscriber sub : subscriptions.keySet()) {
                if (sub instanceof FailurePreventionUnit || sub instanceof ControlRoomDisplay) {
                    sub.onSurgeAlert(event, alert);
                }
            }
            return;
        }

        // Rule 2: Normal Selective Notification Filtering
        notifySubscribers(event);
    }

    private void notifySubscribers(GridEvent event) {
        for (Map.Entry<GridSubscriber, Predicate<GridEvent>> entry : subscriptions.entrySet()) {
            GridSubscriber sub = entry.getKey();
            Predicate<GridEvent> filter = entry.getValue();

            if (filter == null || filter.test(event)) {
                sub.onGridEvent(event);
            }
        }
    }
}

public class SmartGridMonitor {
    public static void main(String[] args) {
        PowerGridStation station = new PowerGridStation();

        GridSubscriber display = new ControlRoomDisplay();
        GridSubscriber safety = new FailurePreventionUnit();
        GridSubscriber billing = new BillingUnit();

        // Control Room Display listens to all events
        station.subscribe(display, event -> true);

        // Safety Unit listens only to events with Voltage > 250 kV
        station.subscribe(safety, event -> event.getVoltageLevel() > 250.0);

        // Billing Unit listens only to INDUSTRIAL loads
        station.subscribe(billing, event -> event.getLoadType() == LoadType.INDUSTRIAL);

        // Event 1: Normal residential load below 250 kV
        station.recordEvent(new GridEvent("GRID-101", 1000L, 220.0, LoadType.RESIDENTIAL));

        // Event 2: High industrial load (matches display, safety, and billing)
        station.recordEvent(new GridEvent("GRID-102", 1005L, 280.0, LoadType.INDUSTRIAL));

        // Event 3: Urgent Surge (> 400 kV)
        station.recordEvent(new GridEvent("GRID-103", 1010L, 450.0, LoadType.COMMERCIAL));
    }
} 
