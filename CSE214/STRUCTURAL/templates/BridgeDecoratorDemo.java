package CSE214.STRUCTURAL.templates;

// ==========================================
// BRIDGE + DECORATOR
// Two hierarchies that must pair freely (Bridge: Region x Mode), PLUS an
// optional stackable extra on top of the result (Decorator: gift wrap).
// This models the delivery part of your real A1 exam question -- region
// and mode were explicitly required to "extend independently," which is
// textbook Bridge, not plain Decorator.
// ==========================================

// Shared interface -- implemented by BOTH the Bridge Abstraction and the Decorator
interface Priceable {
    double getCost();
    String getSummary();
}

// ---- Bridge: Implementor (the delivery REGION) ----
interface DeliveryRegion {
    double costForDistance(double miles);
    String getRegionName();
}

class LocalRegion implements DeliveryRegion {
    public double costForDistance(double miles) { return miles * 1.0; }
    public String getRegionName() { return "Local"; }
}

class NationalRegion implements DeliveryRegion {
    public double costForDistance(double miles) { return miles * 1.0 + 20; }
    public String getRegionName() { return "National"; }
}

class InternationalRegion implements DeliveryRegion {
    public double costForDistance(double miles) { return 500; } // flat, distance ignored
    public String getRegionName() { return "International"; }
}

// ---- Bridge: Abstraction (the delivery MODE) ----
abstract class DeliveryMode implements Priceable {
    protected DeliveryRegion region;
    protected double miles;

    public DeliveryMode(DeliveryRegion region, double miles) {
        this.region = region;
        this.miles = miles;
    }
}

class StandardMode extends DeliveryMode {
    public StandardMode(DeliveryRegion region, double miles) { super(region, miles); }
    public double getCost() { return region.costForDistance(miles); }
    public String getSummary() { return region.getRegionName() + " Standard delivery"; }
}

class ExpressMode extends DeliveryMode {
    public ExpressMode(DeliveryRegion region, double miles) { super(region, miles); }
    public double getCost() { return region.costForDistance(miles) + 10; }
    public String getSummary() { return region.getRegionName() + " Express delivery"; }
}

class PriorityMode extends DeliveryMode {
    public PriorityMode(DeliveryRegion region, double miles) { super(region, miles); }
    public double getCost() { return region.costForDistance(miles) + 25; }
    public String getSummary() { return region.getRegionName() + " Priority delivery"; }
}

// ---- Decorator: wraps ANY Priceable (a DeliveryMode, or another decorator) ----
abstract class DeliveryDecorator implements Priceable {
    protected Priceable wrapped;
    public DeliveryDecorator(Priceable wrapped) { this.wrapped = wrapped; }
}

class GiftWrapDecorator extends DeliveryDecorator {
    public GiftWrapDecorator(Priceable wrapped) { super(wrapped); }
    public double getCost() { return wrapped.getCost() + 2; }
    public String getSummary() { return wrapped.getSummary() + " + gift wrap"; }
}

// ---- Demo ----
public class BridgeDecoratorDemo {
    public static void main(String[] args) {
        // Re-creating A1's Case 2: wooden souvenir $60, 50 miles, National,
        // gift wrap, Express delivery. Real answer key total was $142.
        double itemPrice = 60.0;
        Priceable delivery = new ExpressMode(new NationalRegion(), 50);
        Priceable withWrap = new GiftWrapDecorator(delivery);

        System.out.println(withWrap.getSummary());
        System.out.printf("Delivery + wrap charge: $%.2f%n", withWrap.getCost());
        System.out.printf("Grand total (item + delivery + wrap): $%.2f%n", itemPrice + withWrap.getCost());
        // Expected: 60 + 2 + (50*1 + 20) + 10 = $142.00  <-- matches A1 Case 2 exactly
    }
}
