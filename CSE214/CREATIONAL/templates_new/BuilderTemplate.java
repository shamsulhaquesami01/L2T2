/* ============================================================================
 * CSE 214 - CREATIONAL PATTERNS - TEMPLATE 4 of 4 :  B U I L D E R
 * ----------------------------------------------------------------------------
 * RUN IT:  java BuilderTemplate.java
 * ============================================================================
 *
 * USE THIS TEMPLATE WHEN THE QUESTION SAYS:
 *   "step by step"                     "the construction process is complex"
 *   "consists of three main components / parts"
 *   "separate the construction of the object from its representation"
 *   "a class that DIRECTS the construction process"      -> the Director
 *   "different representations using the SAME construction process"
 *   "two standard models/packages: A includes ..., B includes ..."
 *
 * THE TELL: ONE final object made of SEVERAL PARTS, assembled in steps.
 *   Compare with Abstract Factory: there you return several SEPARATE objects
 *   of a matching family. Here you return ONE assembled object.
 *
 * MAPPING THE QUESTION ONTO THE CODE (this is where people lose marks):
 *   "three components: Flight, Hotel, Activity"  -> the 3 builder STEPS
 *   "two standard packages: Relaxation, Adventure" -> two DIRECTOR METHODS
 *                                                     (NOT two builders!)
 *   "different representations"                  -> the second CONCRETE BUILDER
 *                                                    (product vs brochure/manual)
 *
 * 60-SECOND ADAPTATION MAP (holiday -> bicycle example):
 *   HolidayPackage        -> Bicycle
 *   setFlight/Hotel/Activity -> setFrame/setGears/setTires
 *   constructRelaxation() -> constructCommuter()
 *   constructAdventure()  -> constructMountainBeast()
 * ========================================================================== */

package templates_new;

public class BuilderTemplate {

    public static void main(String[] args) {

        Director director = new Director();

        System.out.println("===== Model 1, built by the director =====");
        HolidayPackageBuilder builder1 = new HolidayPackageBuilder();
        director.constructRelaxationPackage(builder1);
        HolidayPackage relaxation = builder1.getResult();   // result comes FROM THE BUILDER
        System.out.println(relaxation);

        System.out.println();
        System.out.println("===== Model 2, SAME construction process =====");
        HolidayPackageBuilder builder2 = new HolidayPackageBuilder();
        director.constructAdventurePackage(builder2);
        System.out.println(builder2.getResult());

        System.out.println();
        System.out.println("===== Different REPRESENTATION, same director call =====");
        // Second concrete builder: identical steps, completely different output.
        BrochureBuilder builder3 = new BrochureBuilder();
        director.constructRelaxationPackage(builder3);
        System.out.println(builder3.getResult());

        System.out.println();
        System.out.println("===== Custom object, no director (client drives the steps) =====");
        HolidayPackageBuilder custom = new HolidayPackageBuilder();
        custom.setFlight("Economy Flight");
        custom.setHotel("City Hostel");
        // an optional step can simply be skipped - that is a Builder selling point
        System.out.println(custom.getResult());

        System.out.println();
        System.out.println("===== OPTIONAL: fluent / method-chaining style =====");
        Bicycle bike = new Bicycle.Builder()
                .frame("Carbon Fiber Frame")
                .gears("12-Speed Gear")
                .tires("Off-road Grip Tires")
                .build();
        System.out.println(bike);
    }
}

/* ===========================================================================
 * 1. THE PRODUCT - a plain object made of several parts.
 *    Strings are fine; the question usually says so explicitly.
 * ======================================================================== */
class HolidayPackage {

    private String flight;
    private String hotel;
    private String activity;

    public void setFlight(String flight)     { this.flight = flight; }
    public void setHotel(String hotel)       { this.hotel = hotel; }
    public void setActivity(String activity) { this.activity = activity; }

    @Override
    public String toString() {
        return "HolidayPackage {"
             + "\n   Flight   : " + flight
             + "\n   Hotel    : " + hotel
             + "\n   Activity : " + activity
             + "\n}";
    }
}

/* ===========================================================================
 * 2. THE BUILDER INTERFACE - one method per construction STEP.
 *    getResult() is deliberately NOT here: different builders return
 *    different types (a package vs a text brochure).
 * ======================================================================== */
interface Builder {
    void reset();
    void setFlight(String flight);
    void setHotel(String hotel);
    void setActivity(String activity);
}

/* ===========================================================================
 * 3. CONCRETE BUILDER #1 - assembles the real product.
 * ======================================================================== */
class HolidayPackageBuilder implements Builder {

    private HolidayPackage pkg;

    public HolidayPackageBuilder() { reset(); }

    public void reset() { pkg = new HolidayPackage(); }

    public void setFlight(String flight)     { pkg.setFlight(flight); }
    public void setHotel(String hotel)       { pkg.setHotel(hotel); }
    public void setActivity(String activity) { pkg.setActivity(activity); }

    // Result is fetched from the BUILDER, not from the director.
    public HolidayPackage getResult() {
        HolidayPackage result = pkg;
        reset();                    // ready to build the next one
        return result;
    }
}

/* ===========================================================================
 * 4. CONCRETE BUILDER #2 - same steps, DIFFERENT REPRESENTATION.
 *    This is the class that proves "different representations using the same
 *    construction process". Delete it if you are short on time - but it is
 *    already typed, so keep it.
 * ======================================================================== */
class BrochureBuilder implements Builder {

    private StringBuilder text;

    public BrochureBuilder() { reset(); }

    public void reset() { text = new StringBuilder("Brochure:\n"); }

    public void setFlight(String flight)     { text.append("   Fly with: ").append(flight).append("\n"); }
    public void setHotel(String hotel)       { text.append("   Stay at : ").append(hotel).append("\n"); }
    public void setActivity(String activity) { text.append("   Enjoy   : ").append(activity).append("\n"); }

    public String getResult() {
        String result = text.toString();
        reset();
        return result;
    }
}

/* ===========================================================================
 * 5. THE DIRECTOR - knows the RECIPES. One method per standard model.
 *    It only ever talks to the Builder interface, so it can drive any builder.
 * ======================================================================== */
class Director {

    public void constructRelaxationPackage(Builder builder) {
        builder.reset();
        builder.setFlight("Business Class Flight");
        builder.setHotel("5-Star Resort");
        builder.setActivity("Spa Treatment");
    }

    public void constructAdventurePackage(Builder builder) {
        builder.reset();
        builder.setFlight("Economy Flight");
        builder.setHotel("Mountain Cabin");
        builder.setActivity("Hiking Tour");
    }

    // Copy-paste for a third model if the question asks for one.
}

/* ===========================================================================
 * OPTIONAL: FLUENT BUILDER (static nested class, the everyday Java style).
 * No director, no interface - the client chains the steps itself.
 * Use it only if the question does NOT ask for a class that "directs" the
 * construction. Otherwise the Director version above is what earns the marks.
 * ======================================================================== */
class Bicycle {

    private final String frame;
    private final String gears;
    private final String tires;

    private Bicycle(Builder b) {          // private: only the Builder can create
        this.frame = b.frame;
        this.gears = b.gears;
        this.tires = b.tires;
    }

    @Override
    public String toString() {
        return "Bicycle { frame=" + frame + ", gears=" + gears + ", tires=" + tires + " }";
    }

    public static class Builder {
        private String frame;
        private String gears;
        private String tires;

        public Builder frame(String f) { this.frame = f; return this; }
        public Builder gears(String g) { this.gears = g; return this; }
        public Builder tires(String t) { this.tires = t; return this; }

        public Bicycle build() { return new Bicycle(this); }
    }
}
