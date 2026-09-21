// ==========================================
// 1. DECORATOR PATTERN: Gift Items & Wrapping
// ==========================================
package CSE214.STRUCTURAL.my_onlines;
// Component
interface GiftItem {
    double getPrice();
    String getDescription();
}

// Concrete Components
class Showpiece implements GiftItem {
    private double basePrice;
    public Showpiece(double price) { this.basePrice = price; }
    
    @Override
    public double getPrice() { return basePrice; }
    @Override
    public String getDescription() { return "Showpiece"; }
}

class Souvenir implements GiftItem {
    private double basePrice;
    public Souvenir(double price) { this.basePrice = price; }
    
    @Override
    public double getPrice() { return basePrice; }
    @Override
    public String getDescription() { return "Wooden Souvenir"; }
}

class Ornament implements GiftItem {
    private double basePrice;
    public Ornament(double price) { this.basePrice = price; }
    
    @Override
    public double getPrice() { return basePrice; }
    @Override
    public String getDescription() { return "Decorative Ornament"; }
}

// Base Decorator
abstract class GiftDecorator implements GiftItem {
    protected GiftItem wrappedItem;
    public GiftDecorator(GiftItem item) { this.wrappedItem = item; }
}

// Concrete Decorator (Wrapping)
class GiftWrapDecorator extends GiftDecorator {
    public GiftWrapDecorator(GiftItem item) { super(item); }
    
    @Override
    public double getPrice() { 
        return wrappedItem.getPrice() + 2.0; // Adds $2 wrapping charge
    }
    
    @Override
    public String getDescription() { 
        return wrappedItem.getDescription() + " (with Gift Wrapping)"; 
    }
}

// ==========================================
// 2. BRIDGE PATTERN: Delivery Regions & Modes
// ==========================================

// Implementation (Platform Layer: Regions)
interface DeliveryRegion {
    double getBaseCost(int distanceMiles);
    String getDefaultTime();
    boolean isInternational();
}

// Concrete Implementations
class LocalRegion implements DeliveryRegion {
    @Override
    public double getBaseCost(int distanceMiles) { return distanceMiles * 1.0; }
    @Override
    public String getDefaultTime() { return "1 week"; }
    @Override
    public boolean isInternational() { return false; }
}

class NationalRegion implements DeliveryRegion {
    @Override
    public double getBaseCost(int distanceMiles) { return (distanceMiles * 1.0) + 20.0; }
    @Override
    public String getDefaultTime() { return "1-2 weeks"; }
    @Override
    public boolean isInternational() { return false; }
}

class InternationalRegion implements DeliveryRegion {
    @Override
    public double getBaseCost(int distanceMiles) { return 500.0; } // Fixed surcharge
    @Override
    public String getDefaultTime() { return "2-3 weeks"; }
    @Override
    public boolean isInternational() { return true; }
}

// Abstraction (Control Layer: Modes)
abstract class DeliveryMode {
    protected DeliveryRegion region; // The Bridge
    
    public DeliveryMode(DeliveryRegion region) {
        this.region = region;
    }
    
    public abstract double calculateTotalDeliveryCost(int distanceMiles);
    public abstract String getEstimatedTime();
}

// Refined Abstractions (Standard, Express, Priority)
class StandardMode extends DeliveryMode {
    public StandardMode(DeliveryRegion region) { super(region); }

    @Override
    public double calculateTotalDeliveryCost(int distanceMiles) {
        return region.getBaseCost(distanceMiles);
    }

    @Override
    public String getEstimatedTime() {
        return region.getDefaultTime();
    }
}

class ExpressMode extends DeliveryMode {
    public ExpressMode(DeliveryRegion region) { super(region); }

    @Override
    public double calculateTotalDeliveryCost(int distanceMiles) {
        return region.getBaseCost(distanceMiles) + 10.0; // Adds $10
    }

    @Override
    public String getEstimatedTime() {
        return region.isInternational() ? "1 week" : "2 days";
    }
}

class PriorityMode extends DeliveryMode {
    public PriorityMode(DeliveryRegion region) { super(region); }

    @Override
    public double calculateTotalDeliveryCost(int distanceMiles) {
        return region.getBaseCost(distanceMiles) + 25.0; // Adds $25
    }

    @Override
    public String getEstimatedTime() {
        return region.isInternational() ? "5 days" : "1 day";
    }
}



class Order {
    private GiftItem item;
    private DeliveryMode delivery;
    private int distance;

    public Order(GiftItem item, DeliveryMode delivery, int distance) {
        this.item = item;
        this.delivery = delivery;
        this.distance = distance;
    }

    public void processOrder(String caseName) {
        double itemTotal = item.getPrice();
        double deliveryTotal = delivery.calculateTotalDeliveryCost(distance);
        double finalCost = itemTotal + deliveryTotal;

        System.out.println("--- " + caseName + " ---");
        System.out.println("Item: " + item.getDescription());
        System.out.printf("Total Cost: $%.2f%n", finalCost);
        System.out.println("Estimated Delivery Time: " + delivery.getEstimatedTime());
        System.out.println();
    }
}

public class A1_GiftShop {
    public static void main(String[] args) {
        
        // Case 1: Decorative vase ($40), 10 miles local, gift wrapped, standard delivery
        GiftItem vase = new Showpiece(40.0);
        vase = new GiftWrapDecorator(vase);
        DeliveryMode localStandard = new StandardMode(new LocalRegion());
        Order case1 = new Order(vase, localStandard, 10);
        case1.processOrder("Case 1");

        // Case 2: Wooden souvenir ($60), 50 miles national, gift wrapped, Express Delivery
        GiftItem souvenir = new Souvenir(60.0);
        souvenir = new GiftWrapDecorator(souvenir);
        DeliveryMode nationalExpress = new ExpressMode(new NationalRegion());
        Order case2 = new Order(souvenir, nationalExpress, 50);
        case2.processOrder("Case 2");

        // Case 3: Crystal showpiece ($150), International, Priority Delivery (No wrapping)
        GiftItem crystal = new Showpiece(150.0);
        DeliveryMode intlPriority = new PriorityMode(new InternationalRegion());
        // Distance is irrelevant for international in this setup, passing 0
        Order case3 = new Order(crystal, intlPriority, 0);
        case3.processOrder("Case 3");
    }
}