package CSE214.STRUCTURAL.templates;

// 1. Target Interface & Concrete Target
//the thing client uses; if its the legacy then the new class should be
//adaptee; if its new interface then the legacy class will be adaptee

interface ModernSystem {
    void newmethod(double amountInUSD);
}

class ModernSystemConcrete implements ModernSystem {
    @Override
    public void newmethod(double amountInUSD) {
        System.out.println("Processing standard USD payment of $" + amountInUSD);
    }
}


// 2. Adaptee Interface & Concrete Adaptees
//adaptee can be a direct class or an abstraction; doesnt matter; it will
//stay as a object inside adapter

interface Legacysystem {
    void oldmethod(double amount, String currency);
}

class LegacyConcrete implements Legacysystem {
    @Override
    public void oldmethod(double amount, String currency) {
        System.out.println("LegacyConcrete  " + amount + " " + currency + " through European banking network.");
    }
}

class LegacyConcrete2 implements Legacysystem {
    @Override
    public void oldmethod(double amount, String currency) {
        System.out.println("LegacyConcrete2  " + amount + " " + currency + " through Asian banking network.");
    }
}


// 3. The Adapter
// here legacy system is adapted; can be reverse

class LegacytoModernAdapter implements ModernSystem {
    private Legacysystem old;
    private String targetCurrency;

    public LegacytoModernAdapter(Legacysystem gateway, String currency) {
        this.old = gateway;
        this.targetCurrency = currency;
    }

    @Override
    public void newmethod(double amountInUSD) {
        // Translate the data
        double convertedAmount = helper(amountInUSD);
        System.out.println("Adapter: Converting $" + amountInUSD + " to " + convertedAmount + " " + targetCurrency);
        
        // Delegate to the wrapped adaptee
        old.oldmethod(convertedAmount, targetCurrency);
    }
    private double helper(double amount){
        //do something
        return amount;
    }
}

// 4. Main / Client

public class AdapterDemo {
    public static void main(String[] args) {
        System.out.println("--- Native Target ---");
        ModernSystem nativeProcessor = new ModernSystemConcrete();
        nativeProcessor.newmethod(100.0);

        System.out.println("\n--- Adapted Euro Target ---");
        Legacysystem euroNode = new LegacyConcrete();
        ModernSystem euroAdapter = new LegacytoModernAdapter(euroNode, "EUR");
        euroAdapter.newmethod(100.0);

        System.out.println("\n--- Adapted Yen Target ---");
        Legacysystem yenNode = new LegacyConcrete2();
        ModernSystem yenAdapter = new LegacytoModernAdapter(yenNode, "JPY");
        yenAdapter.newmethod(100.0);
    }
}