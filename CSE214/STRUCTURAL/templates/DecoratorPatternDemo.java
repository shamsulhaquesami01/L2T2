package CSE214.STRUCTURAL.templates;

abstract class Beverage {
    String description = "Unknown Beverage";

    public String getDescription() {
        return description;
    }

    public abstract double cost();
}

class Espresso extends Beverage {
    public Espresso() {
        // Sets the description inherited from Beverage
        description = "Espresso";
    }

    public double cost() {
        // Returns the base cost without worrying about condiments
        return 1.99;
    }
}

class DarkRoast extends Beverage {
    public DarkRoast() {
        description = "Dark Roast Coffee";
    }

    public double cost() {
        return 0.99;
    }
}

abstract class CondimentDecorator extends Beverage {
    public abstract String getDescription();
}

class Mocha extends CondimentDecorator {
    // 1. An instance variable to hold the beverage we are wrapping
    Beverage beverage;

    // 2. Pass the beverage we are wrapping to the constructor
    public Mocha(Beverage beverage) {
        this.beverage = beverage;
    }

    public String getDescription() {
        // Delegate to the wrapped object, then append this decorator's detail
        return beverage.getDescription() + ", Mocha";
    }

    public double cost() {
        // Add the cost of the Mocha (.20) to the cost of the wrapped beverage
        return .20 + beverage.cost();
    }
}

class Whip extends CondimentDecorator {
    Beverage beverage;

    public Whip(Beverage beverage) {
        this.beverage = beverage;
    }

    public String getDescription() {
        return beverage.getDescription() + ", Whip";
    }

    public double cost() {
        return .10 + beverage.cost();
    }
}

public class DecoratorPatternDemo {
    public static void main(String args[]) {
        // Order up an espresso, no condiments
        Beverage beverage = new Espresso();
        System.out.println(beverage.getDescription() + " $" + beverage.cost());

        // Make a DarkRoast object
        Beverage beverage2 = new DarkRoast();
        // Wrap it with a Mocha
        beverage2 = new Mocha(beverage2);
        // Wrap it in a second Mocha (Handles the double quantity issue seamlessly!)
        beverage2 = new Mocha(beverage2);
        // Wrap it in a Whip
        beverage2 = new Whip(beverage2);

        System.out.println(beverage2.getDescription() + " $" + beverage2.cost());
        // Prints: Dark Roast Coffee, Mocha, Mocha, Whip $1.49
    }
}