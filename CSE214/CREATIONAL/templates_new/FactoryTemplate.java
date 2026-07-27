/* ============================================================================
 * CSE 214 - CREATIONAL PATTERNS - TEMPLATE 2 of 4 :  F A C T O R Y
 *                                (Simple Factory  +  Factory Method)
 * ----------------------------------------------------------------------------
 * RUN IT:  java FactoryTemplate.java
 * ============================================================================
 *
 * USE THIS TEMPLATE WHEN THE QUESTION SAYS:
 *   "the client provides a type / mode as a String"     e.g. "Road", "SMS"
 *   "the system decides which class to instantiate"
 *   "the client should not know the concrete class names"
 *   "adding a new type later should need minimal changes"
 *   "instantiate the correct object without modifying client code"
 *   ONE product interface, MANY interchangeable implementations. <- key signal
 *
 * THIS FILE CONTAINS TWO ANSWERS. Keep the one the question fits, delete
 * the other (or keep both and say "Way 2 is the GoF Factory Method" - that
 * is usually worth extra credit and costs you nothing, it is already typed).
 *
 *   WAY 1 - SIMPLE FACTORY : one class with a static create(String) + switch.
 *           Shortest correct answer when the wording is "client passes a
 *           string, factory returns the object".
 *   WAY 2 - FACTORY METHOD (GoF) : abstract Creator declares the factory
 *           method; each subclass overrides it. Use when the question says
 *           "subclasses decide", or when the creator has business logic of
 *           its own, or when the course explicitly names "Factory Method".
 *
 * 60-SECOND ADAPTATION MAP (transport -> notification example):
 *   Transport   -> Notification            deliver()      -> notifyUser()
 *   Truck/Ship  -> SMSNotification/Email   TransportFactory -> NotificationFactory
 *   "Road"/"Sea"-> "SMS"/"Email"/"Push"    Logistics      -> NotificationService
 * ========================================================================== */
package templates_new;

public class FactoryTemplate {

    public static void main(String[] args) {

        System.out.println("===== WAY 1 : SIMPLE FACTORY (client passes a String) =====");

        Transport t1 = TransportFactory.create("Road");
        t1.deliver();

        Transport t2 = TransportFactory.create("Sea");
        t2.deliver();

        Transport t3 = TransportFactory.create("Air");
        t3.deliver();

        // The client never wrote `new Truck()` - it only knows the interface.

        System.out.println();
        System.out.println("===== WAY 2 : FACTORY METHOD (GoF, subclass decides) =====");

        // The client still passes a String; a tiny selector turns it into the
        // right CREATOR, and the creator's factory method makes the product.
        Logistics road = LogisticsFactory.getLogistics("Road");
        road.planDelivery();

        Logistics sea = LogisticsFactory.getLogistics("Sea");
        sea.planDelivery();

        System.out.println();
        System.out.println("===== Adding a new type = add 1 class, change nothing else =====");
        Logistics rail = LogisticsFactory.getLogistics("Rail");
        rail.planDelivery();
    }
}

/*
 * ===========================================================================
 * PRODUCT INTERFACE - what every created object has in common.
 * ========================================================================
 */
interface Transport {
    void deliver();
}

/*
 * ---- CONCRETE PRODUCTS ---------------------------------------------------
 * Keep as many as the question needs, delete the rest, copy one to add more.
 * ------------------------------------------------------------------------
 */
class Truck implements Transport {
    public void deliver() {
        System.out.println("   Truck   : delivering cargo by ROAD.");
    }
}

class Ship implements Transport {
    public void deliver() {
        System.out.println("   Ship    : delivering cargo by SEA.");
    }
}

class Airplane implements Transport {
    public void deliver() {
        System.out.println("   Airplane: delivering cargo by AIR.");
    }
}

class Train implements Transport {
    public void deliver() {
        System.out.println("   Train   : delivering cargo by RAIL.");
    }
}

/*
 * ===========================================================================
 * WAY 1 : SIMPLE FACTORY
 * One place in the whole program that knows the concrete class names.
 * ========================================================================
 */
class TransportFactory {

    public static Transport create(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Transport type must not be null");
        }
        switch (type.toLowerCase()) {
            case "road":
            case "truck":
                return new Truck();
            case "sea":
            case "ship":
                return new Ship();
            case "air":
            case "airplane":
                return new Airplane();
            case "rail":
            case "train":
                return new Train();
            default:
                throw new IllegalArgumentException("Unknown transport type: " + type);
        }
    }
}

/*
 * ===========================================================================
 * WAY 2 : FACTORY METHOD (GoF)
 * The Creator has real business logic (planDelivery) and delegates the
 * "which class do I instantiate?" decision to its subclasses.
 * ========================================================================
 */
abstract class Logistics {

    // THE FACTORY METHOD. Return type = the PRODUCT INTERFACE, never a
    // concrete class. Subclasses supply the body.
    public abstract Transport createTransport();

    // The creator's real job. Note it works only with the interface.
    public void planDelivery() {
        Transport transport = createTransport(); // <- factory method call
        System.out.println("[Logistics] route planned, handing over to carrier:");
        transport.deliver();
    }
}

/*
 * ---- CONCRETE CREATORS : one per product, each overrides the factory method
 */
class RoadLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Truck();
    }
}

class SeaLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Ship();
    }
}

class AirLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Airplane();
    }
}

class RailLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Train();
    }
}

/*
 * ---- Selector: turns the client's String into the right CREATOR ----------
 * This is what lets you satisfy BOTH "client passes a string" AND
 * "Factory Method pattern" in the same answer.
 * ------------------------------------------------------------------------
 */
class LogisticsFactory {

    public static Logistics getLogistics(String mode) {
        switch (mode.toLowerCase()) {
            case "road":
                return new RoadLogistics();
            case "sea":
                return new SeaLogistics();
            case "air":
                return new AirLogistics();
            case "rail":
                return new RailLogistics();
            default:
                throw new IllegalArgumentException("Unknown delivery mode: " + mode);
        }
    }
}
