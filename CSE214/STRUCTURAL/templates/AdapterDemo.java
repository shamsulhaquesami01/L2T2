package CSE214.STRUCTURAL.templates;
// ==========================================
// 1. Target Interface & Concrete Target
// ==========================================
interface PaymentProcessor {
    void processPayment(double amountInUSD);
}

class StandardUsdProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amountInUSD) {
        System.out.println("Processing standard USD payment of $" + amountInUSD);
    }
}

// ==========================================
// 2. Adaptee Interface & Concrete Adaptees
// ==========================================
interface ForeignPaymentGateway {
    void executeForeignTransaction(double amount, String currency);
}

class EuroGateway implements ForeignPaymentGateway {
    @Override
    public void executeForeignTransaction(double amount, String currency) {
        System.out.println("EuroGateway routing " + amount + " " + currency + " through European banking network.");
    }
}

class YenGateway implements ForeignPaymentGateway {
    @Override
    public void executeForeignTransaction(double amount, String currency) {
        System.out.println("YenGateway routing " + amount + " " + currency + " through Asian banking network.");
    }
}

// ==========================================
// 3. The Adapter
// ==========================================
class UniversalPaymentAdapter implements PaymentProcessor {
    private ForeignPaymentGateway foreignGateway;
    private String targetCurrency;
    private double conversionRate;

    public UniversalPaymentAdapter(ForeignPaymentGateway gateway, String currency, double rate) {
        this.foreignGateway = gateway;
        this.targetCurrency = currency;
        this.conversionRate = rate;
    }

    @Override
    public void processPayment(double amountInUSD) {
        // Translate the data
        double convertedAmount = amountInUSD * conversionRate;
        System.out.println("Adapter: Converting $" + amountInUSD + " to " + convertedAmount + " " + targetCurrency);
        
        // Delegate to the wrapped adaptee
        foreignGateway.executeForeignTransaction(convertedAmount, targetCurrency);
    }
}

// ==========================================
// 4. Main / Client
// ==========================================
public class AdapterDemo {
    public static void main(String[] args) {
        System.out.println("--- Native Target ---");
        PaymentProcessor nativeProcessor = new StandardUsdProcessor();
        nativeProcessor.processPayment(100.0);

        System.out.println("\n--- Adapted Euro Target ---");
        ForeignPaymentGateway euroNode = new EuroGateway();
        PaymentProcessor euroAdapter = new UniversalPaymentAdapter(euroNode, "EUR", 0.92);
        euroAdapter.processPayment(100.0);

        System.out.println("\n--- Adapted Yen Target ---");
        ForeignPaymentGateway yenNode = new YenGateway();
        PaymentProcessor yenAdapter = new UniversalPaymentAdapter(yenNode, "JPY", 150.50);
        yenAdapter.processPayment(100.0);
    }
}